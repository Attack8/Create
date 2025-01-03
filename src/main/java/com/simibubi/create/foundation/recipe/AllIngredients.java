package com.simibubi.create.foundation.recipe;

import com.simibubi.create.Create;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class AllIngredients {
	public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, Create.ID);

	// Unused currently

	public static void register(IEventBus modEventBus) {
		INGREDIENT_TYPES.register(modEventBus);
	}
}
