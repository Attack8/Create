package com.simibubi.create.content.logistics.stockTicker;

import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public class LogisticalStockRequestPacket extends BlockEntityConfigurationPacket<StockCheckingBlockEntity> {
	public static StreamCodec<ByteBuf, LogisticalStockRequestPacket> STREAM_CODEC = StreamCodec.composite(
	    BlockPos.STREAM_CODEC, packet -> packet.pos,
	    LogisticalStockRequestPacket::new
	);

	public LogisticalStockRequestPacket(BlockPos pos) {
		super(pos);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.LOGISTICS_STOCK_REQUEST;
	}

	@Override
	protected void applySettings(ServerPlayer player, StockCheckingBlockEntity be) {
		be.getRecentSummary()
			.divideAndSendTo(player, pos);
	}

	@Override
	protected int maxRange() {
		return 4096;
	}
}
