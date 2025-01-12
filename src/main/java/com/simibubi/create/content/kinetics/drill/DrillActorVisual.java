package com.simibubi.create.content.kinetics.drill;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ActorVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import net.createmod.catnip.utility.VecHelper;
import net.createmod.catnip.utility.math.AngleHelper;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class DrillActorVisual extends ActorVisual {

    RotatingInstance drillHead;
    private final Direction facing;

    public DrillActorVisual(VisualizationContext visualizationContext, VirtualRenderWorld contraption, MovementContext context) {
        super(visualizationContext, contraption, context);

        BlockState state = context.state;

        facing = state.getValue(DrillBlock.FACING);

        Direction.Axis axis = facing.getAxis();
        float eulerX = AngleHelper.verticalAngle(facing);

        float eulerY;
        if (axis == Direction.Axis.Y)
            eulerY = 0;
        else
            eulerY = facing.toYRot() + ((axis == Direction.Axis.X) ? 180 : 0);

		drillHead = instancerProvider.instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.DRILL_HEAD))
				.createInstance();

		drillHead.rotation.rotationXYZ(eulerX * Mth.DEG_TO_RAD, eulerY * Mth.DEG_TO_RAD, 0);

        drillHead.setPosition(context.localPos)
			.setRotationOffset(0)
			.setRotationAxis(0, 0, 1)
			.setRotationalSpeed(getSpeed(facing))
			.light(localBlockLight(), 0)
			.setChanged();
    }

    @Override
    public void beginFrame() {
        drillHead.setRotationalSpeed(getSpeed(facing))
        		.setChanged();
    }

    protected float getSpeed(Direction facing) {
        if (context.contraption.stalled || !VecHelper.isVecPointingTowards(context.relativeMotion, facing.getOpposite()))
            return context.getAnimationSpeed();
        return 0;
    }

	@Override
	protected void _delete() {
		drillHead.delete();
	}
}
