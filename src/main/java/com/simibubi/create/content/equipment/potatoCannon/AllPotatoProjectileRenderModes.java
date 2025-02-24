package com.simibubi.create.content.equipment.potatoCannon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.Create;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileRenderMode;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileRenderMode.Billboard;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileRenderMode.StuckToEntity;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileRenderMode.TowardMotion;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileRenderMode.Tumble;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;

import net.minecraft.core.Registry;

public class AllPotatoProjectileRenderModes {
	public static void init() {
		register("billboard", Billboard.CODEC);
		register("tumble", Tumble.CODEC);
		register("toward_motion", TowardMotion.CODEC);
		register("stuck_to_entity", StuckToEntity.CODEC);
	}

	private static void register(String name, MapCodec<? extends PotatoProjectileRenderMode> codec) {
		Registry.register(CreateBuiltInRegistries.POTATO_PROJECTILE_RENDER_MODE, Create.asResource(name), codec);
	}
}
