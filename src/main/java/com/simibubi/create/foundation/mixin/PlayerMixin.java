package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.equipment.armor.CardboardArmorHandler;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

@Mixin(Player.class)
public class PlayerMixin {
	@ModifyExpressionValue(method = "canPlayerFitWithinBlocksAndEntitiesWhen", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;noCollision(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Z"))
	public boolean create$playerHidingAsBoxIsCrouchingNotSwimming(boolean original, @Local(argsOnly = true) Pose pose) {
		return original || (pose == Pose.CROUCHING && CardboardArmorHandler.testForStealth((Entity) (Object) this));
	}
}
