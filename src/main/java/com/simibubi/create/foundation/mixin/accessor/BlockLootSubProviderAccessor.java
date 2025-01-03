package com.simibubi.create.foundation.mixin.accessor;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockLootSubProvider.class)
public interface BlockLootSubProviderAccessor {
	@Accessor("registries")
	HolderLookup.Provider create$getRegistries();
}
