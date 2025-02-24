package com.simibubi.create.content.equipment.potatoCannon;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.Create;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileEntityHitAction;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileEntityHitAction.ChorusTeleport;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileEntityHitAction.CureZombieVillager;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileEntityHitAction.FoodEffects;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileEntityHitAction.PotionEffect;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileEntityHitAction.SetOnFire;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileEntityHitAction.SuspiciousStew;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;

import net.minecraft.core.Registry;

public class AllPotatoProjectileEntityHitActions {
	public static void init() {
		register("set_on_fire", SetOnFire.CODEC);
		register("potion_effect", PotionEffect.CODEC);
		register("food_effects", FoodEffects.CODEC);
		register("chorus_teleport", ChorusTeleport.CODEC);
		register("cure_zombie_villager", CureZombieVillager.CODEC);
		register("suspicious_stew", SuspiciousStew.CODEC);
	}

	private static void register(String name, MapCodec<? extends PotatoProjectileEntityHitAction> codec) {
		Registry.register(CreateBuiltInRegistries.POTATO_PROJECTILE_ENTITY_HIT_ACTION, Create.asResource(name), codec);
	}
}
