package com.simibubi.create.content.contraptions.sync;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.createmod.catnip.net.base.ClientboundPacketPayload;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ContraptionFluidPacket(int entityId, BlockPos localPos, FluidStack fluid) implements ClientboundPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, ContraptionFluidPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, ContraptionFluidPacket::entityId,
			BlockPos.STREAM_CODEC, ContraptionFluidPacket::localPos,
			FluidStack.OPTIONAL_STREAM_CODEC, ContraptionFluidPacket::fluid,
	        ContraptionFluidPacket::new
	);

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(LocalPlayer player) {
		Entity entityByID = player.clientLevel.getEntity(entityId);
		if (!(entityByID instanceof AbstractContraptionEntity contraptionEntity))
			return;
		contraptionEntity.getContraption().handleContraptionFluidPacket(localPos, fluid);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CONTRAPTION_FLUID;
	}
}
