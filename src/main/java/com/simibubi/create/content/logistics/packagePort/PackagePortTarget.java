package com.simibubi.create.content.logistics.packagePort;

import java.util.Map;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity.ConnectedPort;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity.ConnectionStats;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public abstract class PackagePortTarget {

	public BlockPos relativePos;
	private String typeKey;

	public PackagePortTarget(String typeKey, BlockPos relativePos) {
		this.typeKey = typeKey;
		this.relativePos = relativePos;
	}

	public abstract boolean export(LevelAccessor level, BlockPos portPos, ItemStack box, boolean simulate);

	public void setup(PackagePortBlockEntity ppbe, LevelAccessor level, BlockPos portPos) {}

	public void register(PackagePortBlockEntity ppbe, LevelAccessor level, BlockPos portPos) {}

	public void deregister(PackagePortBlockEntity ppbe, LevelAccessor level, BlockPos portPos) {}

	public abstract Vec3 getExactTargetLocation(PackagePortBlockEntity ppbe, LevelAccessor level, BlockPos portPos);

	public CompoundTag write() {
		CompoundTag compoundTag = new CompoundTag();
		writeInternal(compoundTag);
		compoundTag.putString("Type", typeKey);
		compoundTag.put("RelativePos", NbtUtils.writeBlockPos(relativePos));
		return compoundTag;
	}

	public static PackagePortTarget read(CompoundTag tag) {
		if (tag.isEmpty())
			return null;

		BlockPos relativePos = NbtUtils.readBlockPos(tag.getCompound("RelativePos"));
		PackagePortTarget target = switch (tag.getString("Type")) {

		case "ChainConveyor" -> new ChainConveyorPortTarget(relativePos, 0, null);

		default -> null;
		};
		
		if (target == null)
			return null;
		
		target.readInternal(tag);
		return target;
	}

	protected abstract void writeInternal(CompoundTag tag);

	protected abstract void readInternal(CompoundTag tag);

	protected BlockEntity be(LevelAccessor level, BlockPos portPos) {
		return level.getBlockEntity(portPos.offset(relativePos));
	}

	public static class ChainConveyorPortTarget extends PackagePortTarget {

		public float chainPos;
		public BlockPos connection;
		public boolean flipped;

		public ChainConveyorPortTarget(BlockPos relativePos, float chainPos, @Nullable BlockPos connection) {
			super("ChainConveyor", relativePos);
			this.chainPos = chainPos;
			this.connection = connection;
		}

		@Override
		public void setup(PackagePortBlockEntity ppbe, LevelAccessor level, BlockPos portPos) {
			if (be(level, portPos) instanceof ChainConveyorBlockEntity clbe)
				flipped = clbe.getSpeed() < 0;
		}

		@Override
		public boolean export(LevelAccessor level, BlockPos portPos, ItemStack box, boolean simulate) {
			if (!(be(level, portPos) instanceof ChainConveyorBlockEntity clbe))
				return false;
			if (connection != null && !clbe.connections.contains(connection))
				return false;
			if (simulate)
				return true;
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
		protected void writeInternal(CompoundTag tag) {
			tag.putFloat("ChainPos", chainPos);
			if (connection != null) {
				tag.put("Connection", NbtUtils.writeBlockPos(connection));
				tag.putBoolean("Flipped", flipped);
			}
		}

		@Override
		protected void readInternal(CompoundTag tag) {
			chainPos = tag.getFloat("ChainPos");
			if (tag.contains("Connection")) {
				connection = NbtUtils.readBlockPos(tag.getCompound("Connection"));
				flipped = tag.getBoolean("Flipped");
			}
		}

		@Override
		public Vec3 getExactTargetLocation(PackagePortBlockEntity ppbe, LevelAccessor level, BlockPos portPos) {
			if (!(be(level, portPos) instanceof ChainConveyorBlockEntity clbe))
				return Vec3.ZERO;
			return clbe.getPackagePosition(chainPos, connection);
		}

	}

}
