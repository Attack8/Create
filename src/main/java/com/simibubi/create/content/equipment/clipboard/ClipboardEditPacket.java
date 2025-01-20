package com.simibubi.create.content.equipment.clipboard;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllPackets;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// TODO - Does this even work?
// Also make sure to filter any data through CreateNBTProcessors.clipboardProcessor
public record ClipboardEditPacket(int hotbarSlot, DataComponentPatch dataComponentPatch, @Nullable BlockPos targetedBlock) implements ServerboundPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClipboardEditPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, ClipboardEditPacket::hotbarSlot,
		DataComponentPatch.STREAM_CODEC, ClipboardEditPacket::dataComponentPatch,
		CatnipStreamCodecBuilders.nullable(BlockPos.STREAM_CODEC), ClipboardEditPacket::targetedBlock,
		ClipboardEditPacket::new
	);

	@Override
	public void handle(ServerPlayer sender) {
		if (targetedBlock != null) {
			Level world = sender.level();
			if (!world.isLoaded(targetedBlock))
				return;
			if (!targetedBlock.closerThan(sender.blockPosition(), 20))
				return;
			if (world.getBlockEntity(targetedBlock) instanceof ClipboardBlockEntity cbe) {
				if (dataComponentPatch.isEmpty()) {
					clearComponents(cbe.dataContainer);
				} else {
					cbe.dataContainer.applyComponents(dataComponentPatch);
				}
				cbe.onEditedBy(sender);
			}
			return;
		}

		ItemStack itemStack = sender.getInventory()
				.getItem(hotbarSlot);
		if (!AllBlocks.CLIPBOARD.isIn(itemStack))
			return;
		if (dataComponentPatch.isEmpty()) {
			clearComponents(itemStack);
		} else {
			itemStack.applyComponents(dataComponentPatch);
		}
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CLIPBOARD_EDIT;
	}

	private static void clearComponents(ItemStack stack) {
		stack.remove(AllDataComponents.CLIPBOARD_TYPE);
		stack.remove(AllDataComponents.CLIPBOARD_PAGES);
		stack.remove(AllDataComponents.CLIPBOARD_READ_ONLY);
		stack.remove(AllDataComponents.CLIPBOARD_COPIED_VALUES);
		stack.remove(AllDataComponents.CLIPBOARD_PREVIOUSLY_OPENED_PAGE);
	}
}
