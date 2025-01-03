package com.simibubi.create.content.contraptions.minecart.capability;

import com.simibubi.create.infrastructure.codec.CreateStreamCodecs;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllAttachmentTypes;
import com.simibubi.create.AllPackets;
import net.createmod.catnip.net.base.ClientboundPacketPayload;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MinecartControllerUpdatePacket(int entityId, @Nullable CompoundTag nbt) implements ClientboundPacketPayload {
	public static final StreamCodec<ByteBuf, MinecartControllerUpdatePacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, MinecartControllerUpdatePacket::entityId,
			CatnipStreamCodecBuilders.nullable(ByteBufCodecs.COMPOUND_TAG), MinecartControllerUpdatePacket::nbt,
			MinecartControllerUpdatePacket::new
	);

	public MinecartControllerUpdatePacket(MinecartController controller, @NotNull HolderLookup.Provider registries) {
		this(controller.cart().getId(), controller.isEmpty() ? null : controller.serializeNBT(registries));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(LocalPlayer player) {
		Entity entityByID = player.clientLevel.getEntity(entityId);
		if (entityByID == null)
			return;
		if (entityByID.hasData(AllAttachmentTypes.MINECART_CONTROLLER) && entityByID instanceof AbstractMinecart cart) {
			if (nbt == null) {
				entityByID.setData(AllAttachmentTypes.MINECART_CONTROLLER, MinecartController.EMPTY);
			} else {
				MinecartController controller = new MinecartController(cart);
				controller.deserializeNBT(player.registryAccess(), nbt);
				cart.setData(AllAttachmentTypes.MINECART_CONTROLLER, controller);
			}
		}
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.MINECART_CONTROLLER;
	}
}
