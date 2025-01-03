package com.simibubi.create.content.contraptions.elevator;

import java.util.List;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.net.base.ServerboundPacketPayload;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.utility.Couple;
import net.createmod.catnip.utility.IntAttached;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ElevatorFloorListPacket(int entityId, List<IntAttached<Couple<String>>> floors) implements ClientboundPacketPayload {
	public static final StreamCodec<ByteBuf, ElevatorFloorListPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, ElevatorFloorListPacket::entityId,
			CatnipStreamCodecBuilders.list(IntAttached.streamCodec(Couple.streamCodec(ByteBufCodecs.STRING_UTF8))), ElevatorFloorListPacket::floors,
			ElevatorFloorListPacket::new
	);

	public ElevatorFloorListPacket(AbstractContraptionEntity entity, List<IntAttached<Couple<String>>> floors) {
		this(entity.getId(), floors);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(LocalPlayer player) {
		Entity entityByID = player.clientLevel.getEntity(entityId);
		if (!(entityByID instanceof AbstractContraptionEntity ace))
			return;
		if (!(ace.getContraption() instanceof ElevatorContraption ec))
			return;

		ec.namesList = this.floors;
		ec.syncControlDisplays();
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.UPDATE_ELEVATOR_FLOORS;
	}

	public record RequestFloorList(int entityId) implements ServerboundPacketPayload {
		public static final StreamCodec<ByteBuf, RequestFloorList> STREAM_CODEC = ByteBufCodecs.INT.map(
				RequestFloorList::new, RequestFloorList::entityId
		);

		public RequestFloorList(AbstractContraptionEntity entity) {
			this(entity.getId());
		}

		@Override
		public void handle(ServerPlayer sender) {
			Entity entityByID = sender.level()
					.getEntity(entityId);
			if (!(entityByID instanceof AbstractContraptionEntity ace))
				return;
			if (!(ace.getContraption()instanceof ElevatorContraption ec))
				return;
			CatnipServices.NETWORK.sendToClient(sender,
					new ElevatorFloorListPacket(ace, ec.namesList));
		}

		@Override
		public PacketTypeProvider getTypeProvider() {
			return AllPackets.REQUEST_FLOOR_LIST;
		}
	}

}
