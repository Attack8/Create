package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.advancement.AllAdvancements;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;

@Mixin(SmithingMenu.class)
public class SmithingMenuMixin {
	@Inject(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ResultContainer;awardUsedRecipes(Lnet/minecraft/world/entity/player/Player;Ljava/util/List;)V"))
	private void create$awardAdvancementWhenTrimmingCardboardArmor(Player player, ItemStack stack, CallbackInfo ci) {
		if (AllItems.CARDBOARD_HELMET.isIn(stack) ||
			AllItems.CARDBOARD_CHESTPLATE.isIn(stack) ||
			AllItems.CARDBOARD_LEGGINGS.isIn(stack) ||
			AllItems.CARDBOARD_BOOTS.isIn(stack)) {
			AllAdvancements.CARDBOARD_ARMOR_TRIM.awardTo(player);
		}
	}
}
