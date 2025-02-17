package com.simibubi.create.api.contraption.storage;

import com.simibubi.create.AllRegistries.Keys;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.lookup.BlockLookup;
import com.simibubi.create.impl.contraption.storage.MountedStorageTypeRegistryImpl;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

import net.minecraft.world.level.block.Block;

public class MountedStorageTypeRegistry {
	/**
	 * Lookup used for finding the item storage type associated with a block.
	 * @see BlockLookup
	 */
	public static final BlockLookup<MountedItemStorageType<?>> ITEM_LOOKUP = MountedStorageTypeRegistryImpl.ITEM_LOOKUP;
	/**
	 * Lookup used for finding the fluid storage type associated with a block.
	 * @see BlockLookup
	 */
	public static final BlockLookup<MountedFluidStorageType<?>> FLUID_LOOKUP = MountedStorageTypeRegistryImpl.FLUID_LOOKUP;

	/**
	 * Utility for use with Registrate builders. Creates a builder transformer
	 * that will register the given MountedItemStorageType to a block when ready.
	 */
	public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> mountedItemStorage(RegistryEntry<? extends MountedItemStorageType<?>, ?> type) {
		return builder -> builder.onRegisterAfter(Keys.MOUNTED_ITEM_STORAGE_TYPE, block -> ITEM_LOOKUP.register(block, type.get()));
	}

	/**
	 * Utility for use with Registrate builders. Creates a builder transformer
	 * that will register the given MountedFluidStorageType to a block when ready.
	 */
	public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> mountedFluidStorage(RegistryEntry<? extends MountedFluidStorageType<?>, ?> type) {
		return builder -> builder.onRegisterAfter(Keys.MOUNTED_FLUID_STORAGE_TYPE, block -> FLUID_LOOKUP.register(block, type.get()));
	}
}
