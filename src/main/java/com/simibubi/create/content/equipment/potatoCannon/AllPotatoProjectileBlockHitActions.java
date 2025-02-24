package com.simibubi.create.content.equipment.potatoCannon;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.Create;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileBlockHitAction;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileBlockHitAction.PlaceBlockOnGround;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileBlockHitAction.PlantCrop;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;

import net.minecraft.core.Registry;

public class AllPotatoProjectileBlockHitActions {
	public static void init() {
		register("plant_crop", PlantCrop.CODEC);
		register("place_block_on_ground", PlaceBlockOnGround.CODEC);
	}

	private static void register(String name, MapCodec<? extends PotatoProjectileBlockHitAction> codec) {
		Registry.register(CreateBuiltInRegistries.POTATO_PROJECTILE_BLOCK_HIT_ACTION, Create.asResource(name), codec);
	}
}
