package com.simibubi.create.content.contraptions.sync;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.items.ItemStackHandler;

public record ContraptionItemPacket(int entityId, BlockPos localPos, List<ItemStack> containedItems) implements ClientboundPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, ContraptionItemPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.INT, ContraptionItemPacket::entityId,
		BlockPos.STREAM_CODEC, ContraptionItemPacket::localPos,
		CatnipStreamCodecBuilders.list(ItemStack.STREAM_CODEC), ContraptionItemPacket::containedItems,
		ContraptionItemPacket::new
	);

	public ContraptionItemPacket(int entityId, BlockPos localPos, ItemStackHandler handler) {
		this(entityId, localPos, convert(handler));
	}

	private static List<ItemStack> convert(ItemStackHandler handler) {
		List<ItemStack> list = new ArrayList<>(handler.getSlots());
		for (int i = 0; i < handler.getSlots(); i++)
			list.add(handler.getStackInSlot(i));
		return list;
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CONTRAPTION_ITEM;
	}

	@Override
	public void handle(LocalPlayer player) {
		Entity entityByID = Minecraft.getInstance().level.getEntity(entityId);
		if (!(entityByID instanceof AbstractContraptionEntity contraptionEntity))
			return;
		contraptionEntity.getContraption().handleContraptionItemPacket(localPos, containedItems);
	}
}
