package com.simibubi.create.infrastructure.worldgen;

import com.simibubi.create.Create;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AllPlacementModifiers {
	private static final DeferredRegister<PlacementModifierType<?>> REGISTER = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, Create.ID);

	public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<ConfigPlacementFilter>> CONFIG_FILTER = REGISTER.register("config_filter", () -> () -> ConfigPlacementFilter.CODEC);

	public static void register(IEventBus modEventBus) {
		REGISTER.register(modEventBus);
	}
}
