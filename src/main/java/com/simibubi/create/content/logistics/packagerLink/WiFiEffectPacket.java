package com.simibubi.create.content.logistics.packagerLink;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record WiFiEffectPacket(BlockPos pos) implements ClientboundPacketPayload {
	public static StreamCodec<ByteBuf, WiFiEffectPacket> STREAM_CODEC = StreamCodec.composite(
	    BlockPos.STREAM_CODEC, WiFiEffectPacket::pos,
		WiFiEffectPacket::new
	);

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.PACKAGER_LINK_EFFECT;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(LocalPlayer player) {
		BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(pos);
			if (blockEntity instanceof PackagerLinkBlockEntity plbe)
				plbe.playEffect();
			if (blockEntity instanceof StockTickerBlockEntity plbe)
				plbe.playEffect();
	}

	public static void send(Level level, BlockPos pos) {
		if (level instanceof ServerLevel serverLevel)
			CatnipServices.NETWORK.sendToClientsAround(serverLevel, pos, 32, new WiFiEffectPacket(pos));
	}
}
