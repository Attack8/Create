package com.simibubi.create.api.contraption.train;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.simibubi.create.AllInteractionBehaviours;
import com.simibubi.create.content.processing.burner.BlockBasedTrainConductorInteractionBehaviour;
import com.simibubi.create.impl.contraption.train.TrainConductorHandlerImpl;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;


/**
 * All required methods to make your block a train conductor similar to the blaze burner
 */
public interface TrainConductorHandler {
	boolean isValidConductor(BlockState state);

	private static void registerHandler(TrainConductorHandler handler) {
		TrainConductorHandlerImpl.CONDUCTOR_HANDLERS.add(handler);
	}

	static void registerConductor(ResourceLocation blockRl, Predicate<BlockState> isValidConductor, UpdateScheduleCallback updateScheduleCallback) {
		AllInteractionBehaviours.registerBehaviour(blockRl, new BlockBasedTrainConductorInteractionBehaviour(isValidConductor, updateScheduleCallback));
		registerHandler(isValidConductor::test);
	}

	interface UpdateScheduleCallback {
		UpdateScheduleCallback EMPTY = (hasSchedule, blockState, blockStateSetter) -> {};

		void update(boolean hasSchedule, BlockState currentBlockState, Consumer<BlockState> blockStateSetter);
	}
}
