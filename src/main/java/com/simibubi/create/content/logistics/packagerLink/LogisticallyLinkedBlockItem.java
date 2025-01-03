package com.simibubi.create.content.logistics.packagerLink;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

public class LogisticallyLinkedBlockItem extends BlockItem {

	public LogisticallyLinkedBlockItem(Block pBlock, Properties pProperties) {
		super(pBlock, pProperties);
	}

	@Override
	public boolean isFoil(@NotNull ItemStack pStack) {
		return isTuned(pStack);
	}

	public static boolean isTuned(ItemStack pStack) {
		return pStack.has(DataComponents.BLOCK_ENTITY_DATA);
	}

	@Nullable
	public static UUID networkFromStack(ItemStack pStack) {
		CompoundTag tag = pStack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();
		if (!tag.hasUUID("Freq"))
			return null;
		return tag.getUUID("Freq");
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext tooltipContext,
								@NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
		super.appendHoverText(stack, tooltipContext, tooltipComponents, tooltipFlag);

		CompoundTag tag = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();
		if (!tag.hasUUID("Freq"))
			return;

		CreateLang.translate("logistically_linked.tooltip")
			.style(ChatFormatting.GOLD)
			.addTo(tooltipComponents);

		CreateLang.translate("logistically_linked.tooltip_clear")
			.style(ChatFormatting.GRAY)
			.addTo(tooltipComponents);
	}

	@Override
	public @NotNull InteractionResult useOn(UseOnContext pContext) {
		ItemStack stack = pContext.getItemInHand();
		BlockPos pos = pContext.getClickedPos();
		Level level = pContext.getLevel();
		Player player = pContext.getPlayer();

		if (player == null)
			return InteractionResult.FAIL;
		if (player.isShiftKeyDown())
			return super.useOn(pContext);

		LogisticallyLinkedBehaviour link = BlockEntityBehaviour.get(level, pos, LogisticallyLinkedBehaviour.TYPE);
		boolean tuned = isTuned(stack);

		if (link != null) {
			if (level.isClientSide)
				return InteractionResult.SUCCESS;
			if (!link.mayInteractMessage(player))
				return InteractionResult.SUCCESS;

			CompoundTag tag = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();
			tag.putUUID("Freq", link.freqId);

			player.displayClientMessage(CreateLang.translateDirect("logistically_linked.tuned"), true);

			BlockEntity.addEntityType(tag, ((IBE<?>) this.getBlock()).getBlockEntityType());
			stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tag));
			return InteractionResult.SUCCESS;
		}

		InteractionResult useOn = super.useOn(pContext);
		if (level.isClientSide || useOn == InteractionResult.FAIL)
			return useOn;

		player.displayClientMessage(tuned ? CreateLang.translateDirect("logistically_linked.connected")
			: CreateLang.translateDirect("logistically_linked.new_network_started"), true);
		return useOn;
	}

}
