package com.simibubi.create.content.logistics.factoryBoard;

import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public class FactoryPanelConnectionPacket extends BlockEntityConfigurationPacket<FactoryPanelBlockEntity> {
	public static final StreamCodec<ByteBuf, FactoryPanelConnectionPacket> STREAM_CODEC = StreamCodec.composite(
	    FactoryPanelPosition.STREAM_CODEC, packet -> packet.fromPos,
		FactoryPanelPosition.STREAM_CODEC, packet -> packet.toPos,
	    FactoryPanelConnectionPacket::new
	);

	private final FactoryPanelPosition fromPos;
	private final FactoryPanelPosition toPos;

	public FactoryPanelConnectionPacket(FactoryPanelPosition fromPos, FactoryPanelPosition toPos) {
		super(toPos.pos());
		this.fromPos = fromPos;
		this.toPos = toPos;
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CONNECT_FACTORY_PANEL;
	}

	@Override
	protected void applySettings(ServerPlayer player, FactoryPanelBlockEntity be) {
		FactoryPanelBehaviour behaviour = FactoryPanelBehaviour.at(be.getLevel(), toPos);
		if (behaviour != null)
			behaviour.addConnection(fromPos);
	}

	@Override
	protected int maxRange() {
		return super.maxRange() * 2;
	}

}
