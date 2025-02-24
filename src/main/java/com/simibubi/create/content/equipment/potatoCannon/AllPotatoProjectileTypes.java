package com.simibubi.create.content.equipment.potatoCannon;

import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.api.equipment.potatoCannon.PotatoCannonProjectileType;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileBlockHitAction.PlaceBlockOnGround;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileBlockHitAction.PlantCrop;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileEntityHitAction.ChorusTeleport;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileEntityHitAction.CureZombieVillager;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileEntityHitAction.FoodEffects;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileEntityHitAction.PotionEffect;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileEntityHitAction.SetOnFire;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileEntityHitAction.SuspiciousStew;
import com.simibubi.create.api.registry.CreateRegistries;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class AllPotatoProjectileTypes {
	public static ResourceKey<PotatoCannonProjectileType> FALLBACK = ResourceKey.create(CreateRegistries.POTATO_PROJECTILE_TYPE, Create.asResource("fallback"));

	public static void bootstrap(BootstrapContext<PotatoCannonProjectileType> ctx) {
		create("fallback")
			.damage(0)
			.register(ctx);

		create("potato")
			.damage(5)
			.reloadTicks(15)
			.velocity(1.25f)
			.knockback(1.5f)
			.renderTumbling()
			.onBlockHit(new PlantCrop(Blocks.POTATOES))
			.registerAndAssign(ctx, Items.POTATO);

		create("baked_potato")
			.damage(5)
			.reloadTicks(15)
			.velocity(1.25f)
			.knockback(0.5f)
			.renderTumbling()
			.preEntityHit(SetOnFire.seconds(3))
			.registerAndAssign(ctx, Items.BAKED_POTATO);

		create("carrot")
			.damage(4)
			.reloadTicks(12)
			.velocity(1.45f)
			.knockback(0.3f)
			.renderTowardMotion(140, 1)
			.soundPitch(1.5f)
			.onBlockHit(new PlantCrop(Blocks.CARROTS))
			.registerAndAssign(ctx, Items.CARROT);

		create("golden_carrot")
			.damage(12)
			.reloadTicks(15)
			.velocity(1.45f)
			.knockback(0.5f)
			.renderTowardMotion(140, 2)
			.soundPitch(1.5f)
			.registerAndAssign(ctx, Items.GOLDEN_CARROT);

		create("sweet_berry")
			.damage(3)
			.reloadTicks(10)
			.knockback(0.1f)
			.velocity(1.05f)
			.renderTumbling()
			.splitInto(3)
			.soundPitch(1.25f)
			.registerAndAssign(ctx, Items.SWEET_BERRIES);

		create("glow_berry")
			.damage(2)
			.reloadTicks(10)
			.knockback(0.05f)
			.velocity(1.05f)
			.renderTumbling()
			.splitInto(2)
			.soundPitch(1.2f)
			.onEntityHit(new PotionEffect(MobEffects.GLOWING, 1, 200, false))
			.registerAndAssign(ctx, Items.GLOW_BERRIES);

		create("chocolate_berry")
			.damage(4)
			.reloadTicks(10)
			.knockback(0.2f)
			.velocity(1.05f)
			.renderTumbling()
			.splitInto(3)
			.soundPitch(1.25f)
			.registerAndAssign(ctx, AllItems.CHOCOLATE_BERRIES.get());

		create("poison_potato")
			.damage(5)
			.reloadTicks(15)
			.knockback(0.05f)
			.velocity(1.25f)
			.renderTumbling()
			.onEntityHit(new PotionEffect(MobEffects.POISON, 1, 160, true))
			.registerAndAssign(ctx, Items.POISONOUS_POTATO);

		create("chorus_fruit")
			.damage(3)
			.reloadTicks(15)
			.velocity(1.20f)
			.knockback(0.05f)
			.renderTumbling()
			.onEntityHit(new ChorusTeleport(20))
			.registerAndAssign(ctx, Items.CHORUS_FRUIT);

		create("apple")
			.damage(5)
			.reloadTicks(10)
			.velocity(1.45f)
			.knockback(0.5f)
			.renderTumbling()
			.soundPitch(1.1f)
			.registerAndAssign(ctx, Items.APPLE);

		create("honeyed_apple")
			.damage(6)
			.reloadTicks(15)
			.velocity(1.35f)
			.knockback(0.1f)
			.renderTumbling()
			.soundPitch(1.1f)
			.onEntityHit(new PotionEffect(MobEffects.MOVEMENT_SLOWDOWN, 2, 160, true))
			.registerAndAssign(ctx, AllItems.HONEYED_APPLE.get());

		create("golden_apple")
			.damage(1)
			.reloadTicks(100)
			.velocity(1.45f)
			.knockback(0.05f)
			.renderTumbling()
			.soundPitch(1.1f)
			.onEntityHit(CureZombieVillager.INSTANCE)
			.registerAndAssign(ctx, Items.GOLDEN_APPLE);

		create("enchanted_golden_apple")
			.damage(1)
			.reloadTicks(100)
			.velocity(1.45f)
			.knockback(0.05f)
			.renderTumbling()
			.soundPitch(1.1f)
			.onEntityHit(new FoodEffects(Foods.ENCHANTED_GOLDEN_APPLE, false))
			.registerAndAssign(ctx, Items.ENCHANTED_GOLDEN_APPLE);

		create("beetroot")
			.damage(2)
			.reloadTicks(5)
			.velocity(1.6f)
			.knockback(0.1f)
			.renderTowardMotion(140, 2)
			.soundPitch(1.6f)
			.registerAndAssign(ctx, Items.BEETROOT);

		create("melon_slice")
			.damage(3)
			.reloadTicks(8)
			.knockback(0.1f)
			.velocity(1.45f)
			.renderTumbling()
			.soundPitch(1.5f)
			.registerAndAssign(ctx, Items.MELON_SLICE);

		create("glistering_melon")
			.damage(5)
			.reloadTicks(8)
			.knockback(0.1f)
			.velocity(1.45f)
			.renderTumbling()
			.soundPitch(1.5f)
			.onEntityHit(new PotionEffect(MobEffects.GLOWING, 1, 100, true))
			.registerAndAssign(ctx, Items.GLISTERING_MELON_SLICE);

		create("melon_block")
			.damage(8)
			.reloadTicks(20)
			.knockback(2.0f)
			.velocity(0.95f)
			.renderTumbling()
			.soundPitch(0.9f)
			.onBlockHit(new PlaceBlockOnGround(Blocks.MELON))
			.registerAndAssign(ctx, Blocks.MELON);

		create("pumpkin_block")
			.damage(6)
			.reloadTicks(15)
			.knockback(2.0f)
			.velocity(0.95f)
			.renderTumbling()
			.soundPitch(0.9f)
			.onBlockHit(new PlaceBlockOnGround(Blocks.PUMPKIN))
			.registerAndAssign(ctx, Blocks.PUMPKIN);

		create("pumpkin_pie")
			.damage(7)
			.reloadTicks(15)
			.knockback(0.05f)
			.velocity(1.1f)
			.renderTumbling()
			.sticky()
			.soundPitch(1.1f)
			.registerAndAssign(ctx, Items.PUMPKIN_PIE);

		create("cake")
			.damage(8)
			.reloadTicks(15)
			.knockback(0.1f)
			.velocity(1.1f)
			.renderTumbling()
			.sticky()
			.registerAndAssign(ctx, Items.CAKE);

		create("blaze_cake")
			.damage(15)
			.reloadTicks(20)
			.knockback(0.3f)
			.velocity(1.1f)
			.renderTumbling()
			.sticky()
			.preEntityHit(SetOnFire.seconds(12))
			.registerAndAssign(ctx, AllItems.BLAZE_CAKE.get());

		create("fish")
			.damage(4)
			.knockback(0.6f)
			.velocity(1.3f)
			.renderTowardMotion(140, 1)
			.sticky()
			.soundPitch(1.3f)
			.registerAndAssign(ctx, Items.COD, Items.COOKED_COD, Items.SALMON, Items.COOKED_SALMON, Items.TROPICAL_FISH);

		create("pufferfish")
			.damage(4)
			.knockback(0.4f)
			.velocity(1.1f)
			.renderTowardMotion(140, 1)
			.sticky()
			.onEntityHit(new FoodEffects(Foods.PUFFERFISH, false))
			.soundPitch(1.1f)
			.registerAndAssign(ctx, Items.PUFFERFISH);

		create("suspicious_stew")
			.damage(3)
			.reloadTicks(40)
			.knockback(0.2f)
			.velocity(0.8f)
			.renderTowardMotion(140, 1)
			.dropStack(Items.BOWL.getDefaultInstance())
			.onEntityHit(SuspiciousStew.INSTANCE)
			.registerAndAssign(ctx, Items.SUSPICIOUS_STEW);
	}

	private static PotatoCannonProjectileType.Builder create(String name) {
		return new PotatoCannonProjectileType.Builder(Create.asResource(name));
	}
}
