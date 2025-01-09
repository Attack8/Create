package com.simibubi.create.content.logistics.packagerLink;

import com.simibubi.create.AllPackets;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record PackagerLinkEffectPacket(BlockPos pos) implements ClientboundPacketPayload {
	public static StreamCodec<ByteBuf, PackagerLinkEffectPacket> STREAM_CODEC = StreamCodec.composite(
	    BlockPos.STREAM_CODEC, PackagerLinkEffectPacket::pos,
	    PackagerLinkEffectPacket::new
	);

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.PACKAGER_LINK_EFFECT;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(LocalPlayer player) {
		if (Minecraft.getInstance().level.getBlockEntity(pos) instanceof PackagerLinkBlockEntity plbe)
			plbe.playEffect();
	}
}
