package com.simibubi.create.content.logistics.redstoneRequester;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllMenuTypes;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.foundation.gui.menu.GhostItemMenu;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class RedstoneRequesterMenu extends GhostItemMenu<RedstoneRequesterBlockEntity> {

	public RedstoneRequesterMenu(MenuType<?> type, int id, Inventory inv, RedstoneRequesterBlockEntity contentHolder) {
		super(type, id, inv, contentHolder);
	}

	public RedstoneRequesterMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
		super(type, id, inv, extraData);
	}

	public static RedstoneRequesterMenu create(int id, Inventory inv, RedstoneRequesterBlockEntity be) {
		return new RedstoneRequesterMenu(AllMenuTypes.REDSTONE_REQUESTER.get(), id, inv, be);
	}

	@Override
	protected ItemStackHandler createGhostInventory() {
		ItemStackHandler inventory = new ItemStackHandler(9);
		List<BigItemStack> stacks = contentHolder.encodedRequest.stacks();
		for (int i = 0; i < stacks.size(); i++)
			inventory.setStackInSlot(i, stacks.get(i).stack.copyWithCount(1));
		return inventory;
	}

	@Override
	protected boolean allowRepeats() {
		return true;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected RedstoneRequesterBlockEntity createOnClient(FriendlyByteBuf extraData) {
		BlockPos blockPos = extraData.readBlockPos();
		return AllBlocks.REDSTONE_REQUESTER.get()
			.getBlockEntity(Minecraft.getInstance().level, blockPos);
	}

	@Override
	protected void addSlots() {
		int playerX = 5;
		int playerY = 142;
		int slotX = 27;
		int slotY = 28;

		addPlayerSlots(playerX, playerY);
		for (int i = 0; i < 9; i++)
			addSlot(new SlotItemHandler(ghostInventory, i, slotX + 20 * i, slotY));
	}

	@Override
	protected void saveData(RedstoneRequesterBlockEntity contentHolder) {
		List<BigItemStack> stacks = contentHolder.encodedRequest.stacks();
		ArrayList<BigItemStack> list = new ArrayList<>();
		for (int i = 0; i < ghostInventory.getSlots(); i++)
			list.add(new BigItemStack(ghostInventory.getStackInSlot(i)
				.copyWithCount(1), i < stacks.size() ? stacks.get(i).count : 1));

		contentHolder.encodedRequest = new PackageOrder(list);
		contentHolder.sendData();
	}

}
