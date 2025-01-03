package com.simibubi.create.content.kinetics.crafter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;

public class MechanicalCraftingInput extends CraftingInput {
	private MechanicalCraftingInput(int width, int height, List<ItemStack> item) {
		super(width, height, item);
	}

	public static MechanicalCraftingInput of(RecipeGridHandler.GroupedItems items) {
		List<ItemStack> list = new ArrayList<>(items.width * items.height);

		for (int y = 0; y < items.height; y++) {
			for (int x = 0; x < items.width; x++) {
				ItemStack stack = items.grid.get(Pair.of(x + items.minX, y + items.minY));
				list.add(stack == null ? ItemStack.EMPTY : stack.copy());
			}
		}

		return new MechanicalCraftingInput(items.width, items.height, list);
	}
}
