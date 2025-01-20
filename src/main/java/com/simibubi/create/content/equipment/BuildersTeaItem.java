package com.simibubi.create.content.equipment;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

public class BuildersTeaItem extends Item {
	public BuildersTeaItem(Properties properties) {
		super(properties);
	}

	public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity livingEntity) {
		ItemStack eatResult = super.finishUsingItem(stack, level, livingEntity);
		if (livingEntity instanceof Player player && !player.getAbilities().instabuild) {
			if (eatResult.isEmpty()) {
				return Items.GLASS_BOTTLE.getDefaultInstance();
			} else {
				player.getInventory().add(Items.GLASS_BOTTLE.getDefaultInstance());
			}
		}
		return eatResult;
	}

	public int getUseDuration(@NotNull ItemStack stack) {
		return 42;
	}

	public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
		return UseAnim.DRINK;
	}
}
