package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.foundation.events.custom.EntityLootEnchantmentLevelEvent;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.neoforged.neoforge.common.NeoForge;

@Mixin(EnchantedCountIncreaseFunction.class)
public abstract class EnchantedCountIncreaseFunctionMixin {
	@Shadow
	@Final
	private Holder<Enchantment> enchantment;

	@Shadow
	@Final
	private NumberProvider value;

	@Shadow
	protected abstract boolean hasLimit();

	@Shadow
	@Final
	private int limit;

	@WrapOperation(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getEnchantmentLevel(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/LivingEntity;)I"))
	private int fireWithAttackingEntity(Holder<Enchantment> enchantmentHolder, LivingEntity attacker,
										Operation<Integer> original, @Local(argsOnly = true) LootContext context) {
		int level = original.call(enchantmentHolder, attacker);
		return create$getLevel(context, enchantmentHolder, level);
	}

	@Inject(method = "run", at = @At(value = "RETURN", ordinal = 1))
	private void fireWithoutAttackingEntity(ItemStack stack, LootContext context, CallbackInfoReturnable<ItemStack> cir) {
		Entity attacker = context.getParamOrNull(LootContextParams.ATTACKING_ENTITY);
		if (!(attacker instanceof LivingEntity))
			return;

		int level = create$getLevel(context, this.enchantment, 0);
		if (level == 0)
			return;

		float f = (float) level * this.value.getFloat(context);
		stack.grow(Math.round(f));
		if (this.hasLimit()) {
			stack.limitSize(this.limit);
		}
	}

	@Unique
	private static int create$getLevel(LootContext context, Holder<Enchantment> enchantmentHolder, int original) {
		Entity thisEntity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
		if (!(thisEntity instanceof LivingEntity livingThis))
			return original;

		DamageSource damageSource = context.getParamOrNull(LootContextParams.DAMAGE_SOURCE);
		EntityLootEnchantmentLevelEvent event = new EntityLootEnchantmentLevelEvent(
				livingThis,
				damageSource,
				enchantmentHolder,
				original
		);
		NeoForge.EVENT_BUS.post(event);
		return event.getLevel();
	}
}
