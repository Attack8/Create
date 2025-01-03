package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;

import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.extensions.IEntityExtension;

@Mixin(Entity.class)
@Implements(@Interface(iface = IEntityExtension.class, prefix = "iForgeEntity$"))
public abstract class ContraptionDriverInteractMixin {
	@Shadow
	public abstract Entity getRootVehicle();

	@Intrinsic
	public boolean iForgeEntity$canRiderInteract() {
		return getRootVehicle() instanceof AbstractContraptionEntity;
	}
}
