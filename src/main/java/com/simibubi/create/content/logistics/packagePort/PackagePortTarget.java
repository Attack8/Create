package com.simibubi.create.content.logistics.packagePort;

import java.util.Map;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity.ConnectedPort;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity.ConnectionStats;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import com.simibubi.create.content.trains.station.StationBlockEntity;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.utility.lang.Lang;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public abstract class PackagePortTarget {
	public static final Codec<PackagePortTarget> CODEC = PackagePortTarget.Type.CODEC.dispatch(PackagePortTarget::getType, type -> type.codec);
	public static StreamCodec<ByteBuf, PackagePortTarget> STREAM_CODEC = Type.STREAM_CODEC.dispatch(PackagePortTarget::getType, type -> type.streamCodec);

	public BlockPos relativePos;

	public PackagePortTarget(BlockPos relativePos) {
		this.relativePos = relativePos;
	}

	public abstract boolean export(LevelAccessor level, BlockPos portPos, ItemStack box, boolean simulate);

	public void setup(PackagePortBlockEntity ppbe, LevelAccessor level, BlockPos portPos) {}

	public void register(PackagePortBlockEntity ppbe, LevelAccessor level, BlockPos portPos) {}

	public void deregister(PackagePortBlockEntity ppbe, LevelAccessor level, BlockPos portPos) {}

	public abstract Vec3 getExactTargetLocation(PackagePortBlockEntity ppbe, LevelAccessor level, BlockPos portPos);

	public abstract ItemStack getIcon();

	public abstract boolean canSupport(BlockEntity be);

	public boolean depositImmediately() {
		return false;
	}

	protected abstract Type getType();

	public BlockEntity be(LevelAccessor level, BlockPos portPos) {
		if (level instanceof Level l && !l.isLoaded(portPos.offset(relativePos)))
			return null;
		return level.getBlockEntity(portPos.offset(relativePos));
	}

	public static class ChainConveyorFrogportTarget extends PackagePortTarget {
		public static MapCodec<ChainConveyorFrogportTarget> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			BlockPos.CODEC.fieldOf("relativePos").forGetter(i -> i.relativePos),
			Codec.FLOAT.fieldOf("chainPos").forGetter(i -> i.chainPos),
			BlockPos.CODEC.fieldOf("connection").forGetter(i -> i.connection)
		).apply(instance, ChainConveyorFrogportTarget::new));

		public static StreamCodec<ByteBuf, ChainConveyorFrogportTarget> STREAM_CODEC = StreamCodec.composite(
		    BlockPos.STREAM_CODEC, i -> i.relativePos,
			ByteBufCodecs.FLOAT, i -> i.chainPos,
		    BlockPos.STREAM_CODEC, i -> i.connection,
		    ChainConveyorFrogportTarget::new
		);

		public float chainPos;
		public BlockPos connection;
		public boolean flipped;

		public ChainConveyorFrogportTarget(BlockPos relativePos, float chainPos, @Nullable BlockPos connection) {
			super(relativePos);
			this.chainPos = chainPos;
			this.connection = connection;
		}

		@Override
		public void setup(PackagePortBlockEntity ppbe, LevelAccessor level, BlockPos portPos) {
			if (be(level, portPos) instanceof ChainConveyorBlockEntity clbe)
				flipped = clbe.getSpeed() < 0;
		}

		@Override
		public ItemStack getIcon() {
			return AllBlocks.CHAIN_CONVEYOR.asStack();
		}

		@Override
		public boolean export(LevelAccessor level, BlockPos portPos, ItemStack box, boolean simulate) {
			if (!(be(level, portPos) instanceof ChainConveyorBlockEntity clbe))
				return false;
			if (connection != null && !clbe.connections.contains(connection))
				return false;
			if (simulate)
				return clbe.getSpeed() != 0 && clbe.canAcceptPackagesFor(connection);
			ChainConveyorPackage box2 = new ChainConveyorPackage(chainPos, box.copy());
			if (connection == null)
				return clbe.addLoopingPackage(box2);
			return clbe.addTravellingPackage(box2, connection);
		}

		@Override
		public void register(PackagePortBlockEntity ppbe, LevelAccessor level, BlockPos portPos) {
			if (!(be(level, portPos) instanceof ChainConveyorBlockEntity clbe))
				return;
			ChainConveyorBlockEntity actualBe = clbe;

			// Jump to opposite chain if motion reversed
			if (connection != null && clbe.getSpeed() < 0 != flipped) {
				deregister(ppbe, level, portPos);
				actualBe = AllBlocks.CHAIN_CONVEYOR.get()
					.getBlockEntity(level, clbe.getBlockPos()
						.offset(connection));
				if (actualBe == null)
					return;
				clbe.prepareStats();
				ConnectionStats stats = clbe.connectionStats.get(connection);
				if (stats != null)
					chainPos = stats.chainLength() - chainPos;
				connection = connection.multiply(-1);
				flipped = !flipped;
				relativePos = actualBe.getBlockPos()
					.subtract(portPos);
				ppbe.notifyUpdate();
			}

			if (connection != null && !actualBe.connections.contains(connection))
				return;
			String portFilter = ppbe.getFilterString();
			if (portFilter == null)
				return;
			actualBe.routingTable.receivePortInfo(portFilter, connection == null ? BlockPos.ZERO : connection);
			Map<BlockPos, ConnectedPort> portMap = connection == null ? actualBe.loopPorts : actualBe.travelPorts;
			portMap.put(relativePos.multiply(-1), new ConnectedPort(chainPos, connection, portFilter));
		}

		@Override
		public void deregister(PackagePortBlockEntity ppbe, LevelAccessor level, BlockPos portPos) {
			if (!(be(level, portPos) instanceof ChainConveyorBlockEntity clbe))
				return;
			clbe.loopPorts.remove(relativePos.multiply(-1));
			clbe.travelPorts.remove(relativePos.multiply(-1));
			String portFilter = ppbe.getFilterString();
			if (portFilter == null)
				return;
			clbe.routingTable.entriesByDistance.removeIf(e -> e.endOfRoute() && e.port()
				.equals(portFilter));
			clbe.routingTable.changed = true;
		}

		@Override
		public Vec3 getExactTargetLocation(PackagePortBlockEntity ppbe, LevelAccessor level, BlockPos portPos) {
			if (!(be(level, portPos) instanceof ChainConveyorBlockEntity clbe))
				return Vec3.ZERO;
			return clbe.getPackagePosition(chainPos, connection);
		}

		@Override
		public boolean canSupport(BlockEntity be) {
			return AllBlockEntityTypes.PACKAGE_FROGPORT.is(be);
		}

		@Override
		protected Type getType() {
			return Type.CHAIN_CONVEYOR;
		}
	}

	public static class TrainStationFrogportTarget extends PackagePortTarget {
		public static MapCodec<TrainStationFrogportTarget> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			BlockPos.CODEC.fieldOf("relativePos").forGetter(i -> i.relativePos)
		).apply(instance, TrainStationFrogportTarget::new));

		public static StreamCodec<ByteBuf, TrainStationFrogportTarget> STREAM_CODEC = StreamCodec.composite(
		    BlockPos.STREAM_CODEC, i -> i.relativePos,
		    TrainStationFrogportTarget::new
		);

		public TrainStationFrogportTarget(BlockPos relativePos) {
			super(relativePos);
		}

		@Override
		public ItemStack getIcon() {
			return AllBlocks.TRACK_STATION.asStack();
		}

		@Override
		public boolean export(LevelAccessor level, BlockPos portPos, ItemStack box, boolean simulate) {
			return false;
		}

		@Override
		public Vec3 getExactTargetLocation(PackagePortBlockEntity ppbe, LevelAccessor level, BlockPos portPos) {
			return Vec3.atCenterOf(portPos.offset(relativePos));
		}

		@Override
		public void register(PackagePortBlockEntity ppbe, LevelAccessor level, BlockPos portPos) {
			if (be(level, portPos) instanceof StationBlockEntity sbe)
				sbe.attachPackagePort(ppbe);
		}

		@Override
		public void deregister(PackagePortBlockEntity ppbe, LevelAccessor level, BlockPos portPos) {
			if (be(level, portPos) instanceof StationBlockEntity sbe)
				sbe.removePackagePort(ppbe);
		}

		@Override
		public boolean depositImmediately() {
			return true;
		}

		@Override
		public boolean canSupport(BlockEntity be) {
			return AllBlockEntityTypes.PACKAGE_POSTBOX.is(be);
		}

		@Override
		protected Type getType() {
			return Type.TRAIN_STATION;
		}
	}

	// TODO - Turn this into a registry so addons and other mods can extend it
	public enum Type implements StringRepresentable {
		CHAIN_CONVEYOR(ChainConveyorFrogportTarget.CODEC, ChainConveyorFrogportTarget.STREAM_CODEC),
		TRAIN_STATION(TrainStationFrogportTarget.CODEC, TrainStationFrogportTarget.STREAM_CODEC);

		public static Codec<Type> CODEC = StringRepresentable.fromValues(Type::values);
		public static StreamCodec<ByteBuf, Type> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(Type.class);

		private final MapCodec<? extends PackagePortTarget> codec;
		private final StreamCodec<ByteBuf, ? extends PackagePortTarget> streamCodec;

		Type(MapCodec<? extends PackagePortTarget> codec, StreamCodec<ByteBuf, ? extends PackagePortTarget> streamCodec) {
			this.codec = codec;
			this.streamCodec = streamCodec;
		}

		@Override
		public @NotNull String getSerializedName() {
			return Lang.asId(name());
		}
	}
}
