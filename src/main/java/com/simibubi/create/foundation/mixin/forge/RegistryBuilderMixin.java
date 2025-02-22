package com.simibubi.create.foundation.mixin.forge;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;

import net.minecraft.resources.ResourceKey;

import net.neoforged.neoforge.registries.RegistryBuilder;

@Mixin(RegistryBuilder.class)
public class RegistryBuilderMixin {
	@Shadow
	@Final
	private ResourceKey<?> registryKey;

	// only a single At is supported by ModifyArg for some reason.

	@ModifyArg(
		method = "create",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/core/DefaultedMappedRegistry;<init>(Ljava/lang/String;Lnet/minecraft/resources/ResourceKey;Lcom/mojang/serialization/Lifecycle;Z)V"
		)
	)
	private boolean allowIntrusiveHoldersDefaulted(boolean hasIntrusiveHolders) {
		return hasIntrusiveHolders || CreateBuiltInRegistries.hasIntrusiveHolders(this.registryKey);
	}

	@ModifyArg(
		method = "create",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/core/MappedRegistry;<init>(Lnet/minecraft/resources/ResourceKey;Lcom/mojang/serialization/Lifecycle;Z)V"
		)
	)
	private boolean allowIntrusiveHolders(boolean hasIntrusiveHolders) {
		return hasIntrusiveHolders || CreateBuiltInRegistries.hasIntrusiveHolders(this.registryKey);
	}
}
