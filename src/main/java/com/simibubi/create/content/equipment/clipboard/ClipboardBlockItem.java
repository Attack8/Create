package com.simibubi.create.content.equipment.clipboard;

import javax.annotation.Nonnull;

import com.simibubi.create.AllDataComponents;

import com.simibubi.create.foundation.recipe.ItemCopyingRecipe.SupportsItemCopying;

import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ClipboardBlockItem extends BlockItem implements SupportsItemCopying {

	public ClipboardBlockItem(Block pBlock, Properties pProperties) {
		super(pBlock, pProperties);
	}

	@Nonnull
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null)
			return InteractionResult.PASS;
		if (player.isShiftKeyDown())
			return super.useOn(context);
		return use(context.getLevel(), player, context.getHand()).getResult();
	}

	@Override
	protected boolean updateCustomBlockEntityTag(BlockPos pPos, Level pLevel, Player pPlayer, ItemStack pStack,
		BlockState pState) {
		if (pLevel.isClientSide())
			return false;
		if (!(pLevel.getBlockEntity(pPos) instanceof ClipboardBlockEntity cbe))
			return false;
		cbe.dataContainer = pStack.copyWithCount(1);
		cbe.notifyUpdate();
		return true;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack heldItem = player.getItemInHand(hand);
		if (hand == InteractionHand.OFF_HAND)
			return InteractionResultHolder.pass(heldItem);

		player.getCooldowns()
			.addCooldown(heldItem.getItem(), 10);
		if (world.isClientSide)
			CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> openScreen(player, heldItem));
		heldItem.set(AllDataComponents.CLIPBOARD_TYPE, ClipboardOverrides.ClipboardType.EDITING);

		return InteractionResultHolder.success(heldItem);
	}

	@OnlyIn(Dist.CLIENT)
	private void openScreen(Player player, ItemStack stack) {
		if (Minecraft.getInstance().player == player)
			ScreenOpener.open(new ClipboardScreen(player.getInventory().selected, stack, null));
	}

	public void registerModelOverrides() {
		CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> ClipboardOverrides.registerModelOverridesClient(this));
	}

}
