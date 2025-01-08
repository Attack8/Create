package com.simibubi.create;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AllMapDecorationTypes {
	private static final DeferredRegister<MapDecorationType> DECORATION_TYPES = DeferredRegister.create(Registries.MAP_DECORATION_TYPE, Create.ID);

	public static final Holder<MapDecorationType> STATION_MAP_DECORATION = DECORATION_TYPES.register("station", () -> new MapDecorationType(Create.asResource("station"), true, -1, false, true));

	public static void register(IEventBus modEventBus) {
		DECORATION_TYPES.register(modEventBus);
	}
}
