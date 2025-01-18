package com.simibubi.create.content.kinetics.base;

import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;

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
		instance.setup(blockEntity)
			.setChanged();
	}

	protected final void updateRotation(RotatingInstance instance, Direction.Axis axis) {
		instance.setup(blockEntity, axis)
			.setChanged();
	}

	protected final void updateRotation(RotatingInstance instance, float speed) {
		instance.setup(blockEntity, speed)
			.setChanged();
	}

	protected final void updateRotation(RotatingInstance instance, Direction.Axis axis, float speed) {
		instance.setup(blockEntity, axis, speed)
			.setChanged();
	}

	protected final RotatingInstance setup(RotatingInstance key) {
		key.setup(blockEntity).setPosition(getVisualPosition()).setChanged();
		return key;
	}

	protected final RotatingInstance setup(RotatingInstance key, Direction.Axis axis) {
		key.setup(blockEntity, axis).setPosition(getVisualPosition()).setChanged();
		return key;
	}

	protected final RotatingInstance setup(RotatingInstance key, float speed) {
		key.setup(blockEntity, speed).setPosition(getVisualPosition()).setChanged();
		return key;
	}

	protected float getRotationOffset(final Direction.Axis axis) {
		return rotationOffset(blockState, axis, pos) + blockEntity.getRotationAngleOffset(axis);
	}

	protected Direction.Axis rotationAxis() {
		return rotationAxis(blockState);
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
}
