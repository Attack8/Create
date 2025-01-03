package com.simibubi.create.content.contraptions.gantry;

import com.simibubi.create.AllPackets;
import net.createmod.catnip.net.base.ClientboundPacketPayload;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record GantryContraptionUpdatePacket(int entityID, double coord, double motion, double sequenceLimit) implements ClientboundPacketPayload {
	public static StreamCodec<ByteBuf, GantryContraptionUpdatePacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, GantryContraptionUpdatePacket::entityID,
			ByteBufCodecs.DOUBLE, GantryContraptionUpdatePacket::coord,
			ByteBufCodecs.DOUBLE, GantryContraptionUpdatePacket::motion,
			ByteBufCodecs.DOUBLE, GantryContraptionUpdatePacket::sequenceLimit,
			GantryContraptionUpdatePacket::new
	);

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(LocalPlayer player) {
		GantryContraptionEntity.handlePacket(this);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.GANTRY_UPDATE;
	}
}
