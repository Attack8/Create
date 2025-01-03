package com.simibubi.create.foundation.events.custom;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

public class EntityLootEnchantmentLevelEvent extends LivingEvent {

	@Nullable
	private final DamageSource damageSource;

	@NotNull
	private final Holder<Enchantment> enchantment;

	private int level;

	public EntityLootEnchantmentLevelEvent(LivingEntity target, @Nullable DamageSource damageSource,
										   @NotNull Holder<Enchantment> enchantment, int level) {
		super(target);

		this.damageSource = damageSource;
		this.enchantment = enchantment;
		this.level = level;
	}

	public @Nullable DamageSource getDamageSource() {
		return damageSource;
	}

	public @NotNull Holder<Enchantment> getEnchantment() {
		return enchantment;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
}
