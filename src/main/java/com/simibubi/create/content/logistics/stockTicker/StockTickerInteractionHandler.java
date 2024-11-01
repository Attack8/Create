package com.simibubi.create.content.logistics.stockTicker;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllPackets;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.displayCloth.ShoppingListItem;
import com.simibubi.create.content.logistics.displayCloth.ShoppingListItem.ShoppingList;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour.RequestType;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.utility.Couple;
import net.createmod.catnip.utility.Iterate;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.PacketDistributor;

@EventBusSubscriber
public class StockTickerInteractionHandler {

	@SubscribeEvent
	public static void interactWithLogisticsManager(EntityInteractSpecific event) {
		Entity entity = event.getTarget();
		Player player = event.getEntity();
		if (player == null || entity == null)
			return;
		if (player.isSpectator())
			return;

		Level level = event.getLevel();
		BlockPos targetPos = getStockTickerPosition(entity);
		if (targetPos == null)
			return;

		ItemStack mainHandItem = player.getMainHandItem();

		if (AllItems.SHOPPING_LIST.isIn(mainHandItem)) {
			interactWithShop(event, player, level, targetPos, mainHandItem);
			return;
		}

		if (level.isClientSide())
			return;
		if (!(level.getBlockEntity(targetPos) instanceof StockTickerBlockEntity stbe))
			return;

		if (!stbe.behaviour.mayInteract(player)) {
			player.displayClientMessage(
				CreateLang.temporaryText("Logistics Network is protected. You can interact using a Shopping list.")
					.style(ChatFormatting.RED)
					.component(),
				true);
			return;
		}

		if (player instanceof ServerPlayer sp) {
			boolean showLockOption =
				stbe.behaviour.mayAdministrate(player) && Create.LOGISTICS.isLockable(stbe.behaviour.freqId);
			boolean isCurrentlyLocked = Create.LOGISTICS.isLocked(stbe.behaviour.freqId);

			AllPackets.getChannel()
				.send(PacketDistributor.PLAYER.with(() -> sp),
					new StockKeeperOpenRequestScreenPacket(targetPos, showLockOption, isCurrentlyLocked));
			stbe.getRecentSummary()
				.divideAndSendTo(sp, targetPos);
		}

		event.setCancellationResult(InteractionResult.SUCCESS);
		event.setCanceled(true);
		return;
	}

	private static void interactWithShop(EntityInteractSpecific event, Player player, Level level, BlockPos targetPos,
		ItemStack mainHandItem) {
		event.setCancellationResult(InteractionResult.SUCCESS);
		event.setCanceled(true);

		if (level.isClientSide())
			return;
		if (!(level.getBlockEntity(targetPos) instanceof StockTickerBlockEntity tickerBE))
			return;

		ShoppingList list = ShoppingListItem.getList(mainHandItem);
		if (list == null)
			return;

		Couple<InventorySummary> bakeEntries = list.bakeEntries(level, null);
		InventorySummary paymentEntries = bakeEntries.getSecond();
		InventorySummary orderEntries = bakeEntries.getFirst();
		PackageOrder order = new PackageOrder(orderEntries.getStacksByCount());

		// Must be up-to-date
		tickerBE.getAccurateSummary();

		// Check stock levels
		InventorySummary recentSummary = tickerBE.getRecentSummary();
		for (BigItemStack entry : order.stacks()) {
			if (recentSummary.getCountOf(entry.stack) >= entry.count)
				continue;

			AllSoundEvents.DENY.playOnServer(level, player.blockPosition());
			CreateLang.temporaryText("Purchase failed: Stock levels lower than expected")
				.style(ChatFormatting.RED)
				.sendStatus(player);
			return;
		}

		// Check space in stock ticker
		int occupiedSlots = 0;
		for (BigItemStack entry : paymentEntries.getStacksByCount())
			occupiedSlots += Mth.ceil(entry.count / (float) entry.stack.getMaxStackSize());
		for (int i = 0; i < tickerBE.receivedPayments.getSlots(); i++)
			if (tickerBE.receivedPayments.getStackInSlot(i)
				.isEmpty())
				occupiedSlots--;

		if (occupiedSlots > 0) {
			AllSoundEvents.DENY.playOnServer(level, player.blockPosition());
			CreateLang.temporaryText("Not enough space in Cash Register")
				.style(ChatFormatting.RED)
				.sendStatus(player);
			return;
		}

		// Transfer payment to stock ticker
		for (boolean simulate : Iterate.trueAndFalse) {
			InventorySummary tally = paymentEntries.copy();
			List<ItemStack> toTransfer = new ArrayList<>();

			for (int i = 0; i < player.getInventory().items.size(); i++) {
				ItemStack item = player.getInventory()
					.getItem(i);
				if (item.isEmpty())
					continue;
				int countOf = tally.getCountOf(item);
				if (countOf == 0)
					continue;
				int toRemove = Math.min(item.getCount(), countOf);
				tally.add(item, -toRemove);

				if (simulate)
					continue;

				int newStackSize = item.getCount() - toRemove;
				player.getInventory()
					.setItem(i, newStackSize == 0 ? ItemStack.EMPTY : item.copyWithCount(newStackSize));
				toTransfer.add(item.copyWithCount(toRemove));
			}

			if (simulate && tally.getTotalCount() != 0) {
				AllSoundEvents.DENY.playOnServer(level, player.blockPosition());
				CreateLang.temporaryText("You cannot afford this purchase")
					.style(ChatFormatting.RED)
					.sendStatus(player);
				return;
			}

			if (simulate)
				continue;

			toTransfer.forEach(s -> ItemHandlerHelper.insertItemStacked(tickerBE.receivedPayments, s, false));
		}

		tickerBE.broadcastPackageRequest(RequestType.PLAYER, order, null, ShoppingListItem.getAddress(mainHandItem));
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
	}

	public static BlockPos getStockTickerPosition(Entity entity) {
		Entity rootVehicle = entity.getRootVehicle();
		if (!(rootVehicle instanceof SeatEntity))
			return null;
		if (!(entity instanceof LivingEntity living))
			return null;
		if (AllEntityTypes.PACKAGE.is(entity))
			return null;

		BlockPos pos = entity.blockPosition();
		int stations = 0;
		BlockPos targetPos = null;

		for (Direction d : Iterate.horizontalDirections) {
			for (int y : Iterate.zeroAndOne) {
				BlockPos workstationPos = pos.relative(d)
					.above(y);
				if (!(entity.level()
					.getBlockState(workstationPos)
					.getBlock() instanceof StockTickerBlock lw))
					continue;
				targetPos = workstationPos;
				stations++;
			}
		}

		if (stations != 1)
			return null;
		return targetPos;
	}

}
