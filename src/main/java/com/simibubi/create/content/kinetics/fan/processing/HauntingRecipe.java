package com.simibubi.create.content.kinetics.fan.processing;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams;

import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

@ParametersAreNonnullByDefault
public class HauntingRecipe extends ProcessingRecipe<SingleRecipeInput> {

	public HauntingRecipe(ProcessingRecipeParams params) {
		super(AllRecipeTypes.HAUNTING, params);
	}

	@Override
	public boolean matches(SingleRecipeInput inv, Level worldIn) {
		if (inv.isEmpty())
			return false;
		return ingredients.get(0)
			.test(inv.getItem(0));
	}

	@Override
	protected int getMaxInputCount() {
		return 1;
	}

	@Override
	protected int getMaxOutputCount() {
		return 12;
	}

}
