package com.simibubi.create.impl.contraption.storage;

import org.jetbrains.annotations.ApiStatus;

import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.AllTags;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.lookup.BlockLookup;

import net.minecraft.world.level.block.Block;

@ApiStatus.Internal
public class MountedStorageTypeRegistryImpl {
	public static final BlockLookup<MountedItemStorageType<?>> ITEM_LOOKUP = BlockLookup.create(MountedStorageTypeRegistryImpl::itemFallback);
	public static final BlockLookup<MountedFluidStorageType<?>> FLUID_LOOKUP = BlockLookup.create();

	private static MountedItemStorageType<?> itemFallback(Block block) {
		return AllTags.AllBlockTags.FALLBACK_MOUNTED_STORAGE_BLACKLIST.matches(block)
			? null
			: AllMountedStorageTypes.FALLBACK.get();
	}
}
