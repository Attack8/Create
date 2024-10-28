package com.simibubi.create.content.logistics.factoryBoard;

import java.util.ArrayList;
import java.util.List;

import net.createmod.catnip.utility.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FactoryPanelConnection {

	public FactoryPanelPosition from;
	public int amount;
	public List<Direction> path;
	public int arrowBendMode;
	public boolean success;

	private int arrowBendModeCurrentPathUses;

	public FactoryPanelConnection(FactoryPanelPosition from, int amount) {
		this.from = from;
		this.amount = amount;
		path = new ArrayList<>();
		success = true;
		arrowBendMode = -1;
		arrowBendModeCurrentPathUses = 0;
	}

	public static FactoryPanelConnection read(CompoundTag nbt) {
		FactoryPanelConnection connection =
			new FactoryPanelConnection(FactoryPanelPosition.read(nbt), nbt.getInt("Amount"));
		connection.arrowBendMode = nbt.getInt("ArrowBending");
		return connection;
	}

	public CompoundTag write() {
		CompoundTag nbt = from.write();
		nbt.putInt("Amount", amount);
		nbt.putInt("ArrowBending", arrowBendMode);
		return nbt;
	}

	public List<Direction> getPath(BlockState state, FactoryPanelPosition to) {
		if (!path.isEmpty() && arrowBendModeCurrentPathUses == arrowBendMode)
			return path;

		arrowBendModeCurrentPathUses = arrowBendMode;
		path.clear();

		float xRot = Mth.RAD_TO_DEG * FactoryPanelBlock.getXRot(state);
		float yRot = Mth.RAD_TO_DEG * FactoryPanelBlock.getYRot(state);

		int slotDiffx = to.slot().xOffset - from.slot().xOffset;
		int slotDiffY = to.slot().yOffset - from.slot().yOffset;
		Vec3 diff = Vec3.atLowerCornerOf(to.pos()
			.subtract(from.pos()));

		diff = VecHelper.rotate(diff, -yRot, Axis.Y);
		diff = VecHelper.rotate(diff, -xRot - 90, Axis.X);
		diff = VecHelper.rotate(diff, -180, Axis.Y);
		diff = diff.add(slotDiffx * .5, 0, slotDiffY * .5);
		diff = diff.multiply(1, 0, 1);

		BlockPos toTravelFirst = BlockPos.ZERO;
		BlockPos toTravelLast = BlockPos.containing(diff.scale(2)
			.add(0.1, 0.1, 0.1));

		if (arrowBendMode > 1) {
			boolean flipX = diff.x > 0 ^ (arrowBendMode % 2 == 1);
			boolean flipZ = diff.z > 0 ^ (arrowBendMode % 2 == 0);
			int ceilX = Mth.positiveCeilDiv(toTravelLast.getX(), 2);
			int ceilZ = Mth.positiveCeilDiv(toTravelLast.getZ(), 2);
			int floorZ = Mth.floorDiv(toTravelLast.getZ(), 2);
			int floorX = Mth.floorDiv(toTravelLast.getX(), 2);
			toTravelFirst = new BlockPos(flipX ? floorX : ceilX, 0, flipZ ? floorZ : ceilZ);
			toTravelLast = new BlockPos(!flipX ? floorX : ceilX, 0, !flipZ ? floorZ : ceilZ);
		}

		Direction lastDirection = null;
		Direction currentDirection = null;

		for (BlockPos toTravel : List.of(toTravelFirst, toTravelLast)) {
			boolean zIsPreferred =
				arrowBendMode == -1 ? (Math.abs(toTravel.getZ()) > Math.abs(toTravel.getX())) : arrowBendMode % 2 == 1;
			List<Direction> directionOrder =
				zIsPreferred ? List.of(Direction.SOUTH, Direction.NORTH, Direction.WEST, Direction.EAST)
					: List.of(Direction.WEST, Direction.EAST, Direction.SOUTH, Direction.NORTH);

			for (int i = 0; i < 100; i++) {
				if (toTravel.equals(BlockPos.ZERO))
					break;

				for (Direction d : directionOrder) {
					if (lastDirection != null && d == lastDirection.getOpposite())
						continue;
					if (currentDirection == null || toTravel.relative(d)
						.distManhattan(BlockPos.ZERO) < toTravel.relative(currentDirection)
							.distManhattan(BlockPos.ZERO))
						currentDirection = d;
				}

				lastDirection = currentDirection;
				toTravel = toTravel.relative(currentDirection);
				path.add(currentDirection);
			}
		}

		return path;
	}

}
