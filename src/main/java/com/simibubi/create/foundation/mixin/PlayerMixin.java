package com.simibubi.create.foundation.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.equipment.armor.CardboardArmorHandler;

import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
	protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}

	@ModifyExpressionValue(method = "canPlayerFitWithinBlocksAndEntitiesWhen", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;noCollision(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Z"))
	public boolean create$playerHidingAsBoxIsCrouchingNotSwimming(boolean original, @Local(argsOnly = true) Pose pose) {
		return original || (pose == Pose.CROUCHING && CardboardArmorHandler.testForStealth((Entity) (Object) this));
	}

	@ModifyExpressionValue(
		method = "aiStep",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/player/Player;isPassenger()Z"
		)
	)
	private boolean pretendNotPassenger(boolean isPassenger) {
		// avoid touching all items in the contraption's massive hitbox
		boolean shouldSync = AllConfigs.server().kinetics.syncPlayerPickupHitboxWithContraptionHitbox.get();
		if (isPassenger && !shouldSync && this.getVehicle() instanceof AbstractContraptionEntity) {
			return false;
		}

		return isPassenger;
	}
}
