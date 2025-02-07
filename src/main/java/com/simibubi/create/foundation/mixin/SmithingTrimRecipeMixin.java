package com.simibubi.create.foundation.mixin;

import com.simibubi.create.AllTags.AllItemTags;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SmithingTrimRecipe.class)
public class SmithingTrimRecipeMixin {
	@Inject(method = "isBaseIngredient", at = @At("HEAD"), cancellable = true)
	private void create$preventTrimmingDivingArmor(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (stack.is(AllItemTags.DIVING_ARMOR.tag))
			cir.setReturnValue(false);
	}
}
