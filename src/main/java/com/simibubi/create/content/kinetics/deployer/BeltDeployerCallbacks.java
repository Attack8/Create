package com.simibubi.create.content.kinetics.deployer;

import static com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING;

import java.util.List;
import java.util.stream.Collectors;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity.Mode;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity.State;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.advancement.CreateAdvancement;
import com.simibubi.create.foundation.recipe.RecipeApplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BeltDeployerCallbacks {

	public static ProcessingResult onItemReceived(TransportedItemStack s, TransportedItemStackHandlerBehaviour i,
		DeployerBlockEntity blockEntity) {

		if (blockEntity.getSpeed() == 0)
			return ProcessingResult.PASS;
		if (blockEntity.mode == Mode.PUNCH)
			return ProcessingResult.PASS;
		BlockState blockState = blockEntity.getBlockState();
		if (!blockState.hasProperty(FACING) || blockState.getValue(FACING) != Direction.DOWN)
			return ProcessingResult.PASS;
		if (blockEntity.state != State.WAITING)
			return ProcessingResult.HOLD;
		if (blockEntity.redstoneLocked)
			return ProcessingResult.PASS;

		DeployerFakePlayer player = blockEntity.getPlayer();
		ItemStack held = player == null ? ItemStack.EMPTY : player.getMainHandItem();

		if (held.isEmpty())
			return ProcessingResult.HOLD;
		if (blockEntity.getRecipe(s.stack) == null)
			return ProcessingResult.PASS;

		blockEntity.start();
		return ProcessingResult.HOLD;
	}

	public static ProcessingResult whenItemHeld(TransportedItemStack s, TransportedItemStackHandlerBehaviour i,
		DeployerBlockEntity blockEntity) {

		if (blockEntity.getSpeed() == 0)
			return ProcessingResult.PASS;
		BlockState blockState = blockEntity.getBlockState();
		if (!blockState.hasProperty(FACING) || blockState.getValue(FACING) != Direction.DOWN)
			return ProcessingResult.PASS;

		DeployerFakePlayer player = blockEntity.getPlayer();
		ItemStack held = player == null ? ItemStack.EMPTY : player.getMainHandItem();
		if (held.isEmpty())
			return ProcessingResult.HOLD;

		RecipeHolder<? extends Recipe<?>> recipeHolder = blockEntity.getRecipe(s.stack);
		if (recipeHolder == null)
			return ProcessingResult.PASS;

		if (blockEntity.state == State.RETRACTING && blockEntity.timer == 1000) {
			activate(s, i, blockEntity, recipeHolder.value());
			return ProcessingResult.HOLD;
		}

		if (blockEntity.state == State.WAITING) {
			if (blockEntity.redstoneLocked)
				return ProcessingResult.PASS;
			blockEntity.start();
		}

		return ProcessingResult.HOLD;
	}

	public static void activate(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler,
		DeployerBlockEntity blockEntity, Recipe<?> recipe) {

		List<TransportedItemStack> collect =
			RecipeApplier.applyRecipeOn(blockEntity.getLevel(), transported.stack.copyWithCount(1), recipe)
				.stream()
				.map(stack -> {
					TransportedItemStack copy = transported.copy();
					boolean centered = BeltHelper.isItemUpright(stack);
					copy.stack = stack;
					copy.locked = true;
					copy.angle = centered ? 180 : Create.RANDOM.nextInt(360);
					return copy;
				})
				.map(t -> {
					t.locked = false;
					return t;
				})
				.collect(Collectors.toList());

		blockEntity.award(AllAdvancements.DEPLOYER);

		TransportedItemStack left = transported.copy();
		blockEntity.player.spawnedItemEffects = transported.stack.copy();
		left.stack.shrink(1);
		ItemStack resultItem = null;

		if (collect.isEmpty()) {
			resultItem = left.stack.copy();
			handler.handleProcessingOnItem(transported, TransportedResult.convertTo(left));
		} else {
			resultItem = collect.get(0).stack.copy();
			handler.handleProcessingOnItem(transported, TransportedResult.convertToAndLeaveHeld(collect, left));
		}

		ItemStack heldItem = blockEntity.player.getMainHandItem();
		// https://github.com/stal111/Forbidden-Arcanus/blob/6a0ae16061dfa1e97c0d27007869c6b23e9ef43a/src/main/java/com/stal111/forbidden_arcanus/core/init/ModDataComponents.java#L27
		// FIXME 1.21: Re-enable Forbidden Arcanus compat
		boolean unbreakable = heldItem.has(DataComponents.UNBREAKABLE); //||
				//heldItem.getTag().getString("Modifier").equals("forbidden_arcanus:eternal"); // Forbidden Arcanus Compat, See Creators-of-Create#6220
		boolean keepHeld =
			recipe instanceof ItemApplicationRecipe && ((ItemApplicationRecipe) recipe).shouldKeepHeldItem();

		if (!unbreakable && !keepHeld) {
			if (heldItem.isDamageableItem())
				heldItem.hurtAndBreak(1, blockEntity.player,
						LivingEntity.getSlotForHand(InteractionHand.MAIN_HAND));
			else
				heldItem.shrink(1);
		}

		if (resultItem != null && !resultItem.isEmpty())
			awardAdvancements(blockEntity, resultItem);

		BlockPos pos = blockEntity.getBlockPos();
		Level world = blockEntity.getLevel();
		if (heldItem.isEmpty())
			world.playSound(null, pos, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, .25f, 1);
		world.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, .25f, .75f);
		if (recipe instanceof SandPaperPolishingRecipe)
			AllSoundEvents.SANDING_SHORT.playOnServer(world, pos, .35f, 1f);

		blockEntity.notifyUpdate();
	}

	private static void awardAdvancements(DeployerBlockEntity blockEntity, ItemStack created) {
		CreateAdvancement advancement = null;

		if (AllBlocks.ANDESITE_CASING.isIn(created))
			advancement = AllAdvancements.ANDESITE_CASING;
		else if (AllBlocks.BRASS_CASING.isIn(created))
			advancement = AllAdvancements.BRASS_CASING;
		else if (AllBlocks.COPPER_CASING.isIn(created))
			advancement = AllAdvancements.COPPER_CASING;
		else if (AllBlocks.RAILWAY_CASING.isIn(created))
			advancement = AllAdvancements.TRAIN_CASING;
		else
			return;

		blockEntity.award(advancement);
	}

}
