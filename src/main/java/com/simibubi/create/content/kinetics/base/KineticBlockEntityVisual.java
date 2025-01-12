package com.simibubi.create.content.kinetics.base;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;

public abstract class KineticBlockEntityVisual<T extends KineticBlockEntity> extends AbstractBlockEntityVisual<T> {

	public KineticBlockEntityVisual(VisualizationContext context, T blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);
	}

	protected final void updateRotation(RotatingInstance instance) {
		updateRotation(instance, rotationAxis(), getBlockEntitySpeed());
	}

	protected final void updateRotation(RotatingInstance instance, Direction.Axis axis) {
		updateRotation(instance, axis, getBlockEntitySpeed());
	}

	protected final void updateRotation(RotatingInstance instance, float speed) {
		updateRotation(instance, rotationAxis(), speed);
	}

	protected final void updateRotation(RotatingInstance instance, Direction.Axis axis, float speed) {
		instance.setRotationAxis(axis)
			.setRotationOffset(getRotationOffset(axis))
			.setRotationalSpeed(speed * RotatingInstance.SPEED_MULTIPLIER)
			.setColor(blockEntity)
			.setChanged();
	}

	protected final RotatingInstance setup(RotatingInstance key) {
		return setup(key, rotationAxis(), getBlockEntitySpeed());
	}

	protected final RotatingInstance setup(RotatingInstance key, Direction.Axis axis) {
		return setup(key, axis, getBlockEntitySpeed());
	}

	protected final RotatingInstance setup(RotatingInstance key, float speed) {
		return setup(key, rotationAxis(), speed);
	}

	protected final RotatingInstance setup(RotatingInstance key, Direction.Axis axis, float speed) {
		key.setRotationAxis(axis)
			.setRotationalSpeed(speed * RotatingInstance.SPEED_MULTIPLIER)
			.setRotationOffset(getRotationOffset(axis))
			.setColor(blockEntity)
			.setPosition(getVisualPosition())
			.setChanged();

		return key;
	}

	protected float getRotationOffset(final Direction.Axis axis) {
		return rotationOffset(blockState, axis, pos) + blockEntity.getRotationAngleOffset(axis);
	}

	protected Direction.Axis rotationAxis() {
		return rotationAxis(blockState);
	}

	protected float getBlockEntitySpeed() {
		return blockEntity.getSpeed();
	}

	protected BlockState shaft() {
		return shaft(rotationAxis());
	}

	public static float rotationOffset(BlockState state, Axis axis, Vec3i pos) {
		if (shouldOffset(axis, pos)) {
			return 22.5f;
		} else {
			return ICogWheel.isLargeCog(state) ? 11.25f : 0;
		}
	}

	public static boolean shouldOffset(Axis axis, Vec3i pos) {
		// Sum the components of the other 2 axes.
		int x = (axis == Axis.X) ? 0 : pos.getX();
		int y = (axis == Axis.Y) ? 0 : pos.getY();
		int z = (axis == Axis.Z) ? 0 : pos.getZ();
		return ((x + y + z) % 2) == 0;
	}

	public static Axis rotationAxis(BlockState blockState) {
		return (blockState.getBlock() instanceof IRotate irotate) ? irotate.getRotationAxis(blockState) : Axis.Y;
	}

	public static BlockState shaft(Direction.Axis axis) {
		return AllBlocks.SHAFT.getDefaultState()
			.setValue(ShaftBlock.AXIS, axis);
	}

	public static BlockState shaft(BlockState blockState) {
		return shaft(rotationAxis(blockState));
	}
}
