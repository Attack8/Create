package com.simibubi.create.content.fluids.spout;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

public class FillingBySpout {

	public static boolean canItemBeFilled(Level world, ItemStack stack) {
		SingleRecipeInput input = new SingleRecipeInput(stack);

		Optional<RecipeHolder<FillingRecipe>> assemblyRecipe =
			SequencedAssemblyRecipe.getRecipe(world, input, AllRecipeTypes.FILLING.getType(), FillingRecipe.class);
		if (assemblyRecipe.isPresent())
			return true;

		if (AllRecipeTypes.FILLING.find(input, world)
			.isPresent())
			return true;
		return GenericItemFilling.canItemBeFilled(world, stack);
	}

	public static int getRequiredAmountForItem(Level world, ItemStack stack, FluidStack availableFluid) {
		SingleRecipeInput input = new SingleRecipeInput(stack);

		Optional<RecipeHolder<FillingRecipe>> assemblyRecipe = SequencedAssemblyRecipe.getRecipe(world, input,
			AllRecipeTypes.FILLING.getType(), FillingRecipe.class, matchItemAndFluid(world, availableFluid, input));
		if (assemblyRecipe.isPresent()) {
			FluidIngredient requiredFluid = assemblyRecipe.get().value()
				.getRequiredFluid();
			if (requiredFluid.test(availableFluid))
				return requiredFluid.getRequiredAmount();
		}

		for (RecipeHolder<Recipe<SingleRecipeInput>> recipe : world.getRecipeManager()
			.getRecipesFor(AllRecipeTypes.FILLING.getType(), input, world)) {
			FillingRecipe fillingRecipe = (FillingRecipe) recipe.value();
			FluidIngredient requiredFluid = fillingRecipe.getRequiredFluid();
			if (requiredFluid.test(availableFluid))
				return requiredFluid.getRequiredAmount();
		}
		return GenericItemFilling.getRequiredAmountForItem(world, stack, availableFluid);
	}

	public static ItemStack fillItem(Level world, int requiredAmount, ItemStack stack, FluidStack availableFluid) {
		FluidStack toFill = availableFluid.copy();
		toFill.setAmount(requiredAmount);

		SingleRecipeInput input = new SingleRecipeInput(stack);

		RecipeHolder<FillingRecipe> fillingRecipe = SequencedAssemblyRecipe
			.getRecipe(world, input, AllRecipeTypes.FILLING.getType(), FillingRecipe.class,
				matchItemAndFluid(world, availableFluid, input))
			.filter(fr -> fr.value().getRequiredFluid()
					.test(toFill))
				.orElseGet(() -> {
					for (RecipeHolder<Recipe<SingleRecipeInput>> recipe : world.getRecipeManager()
						.getRecipesFor(AllRecipeTypes.FILLING.getType(), input, world)) {
						FillingRecipe fr = (FillingRecipe) recipe.value();
						FluidIngredient requiredFluid = fr.getRequiredFluid();
						if (requiredFluid.test(toFill))
							return new RecipeHolder<>(recipe.id(), fr);
					}
					return null;
				});

		if (fillingRecipe != null) {
			List<ItemStack> results = fillingRecipe.value().rollResults();
			availableFluid.shrink(requiredAmount);
			stack.shrink(1);
			return results.isEmpty() ? ItemStack.EMPTY : results.get(0);
		}

		return GenericItemFilling.fillItem(world, requiredAmount, stack, availableFluid);
	}

	private static Predicate<RecipeHolder<FillingRecipe>> matchItemAndFluid(Level world, FluidStack availableFluid, SingleRecipeInput input) {
		return r -> r.value().matches(input, world) && r.value().getRequiredFluid()
			.test(availableFluid);
	}

}
