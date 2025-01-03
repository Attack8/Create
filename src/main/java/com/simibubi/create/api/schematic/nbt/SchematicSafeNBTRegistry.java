package com.simibubi.create.api.schematic.nbt;

import com.simibubi.create.impl.schematic.nbt.SchematicSafeNBTRegistryImpl;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Registry for modifying the data of BlockEntities when being placed with the schematic system.
 * </br>
 * Mostly used to exclude specific tags that would result in exploits from being written.
 */
public class SchematicSafeNBTRegistry {
	/**
	 * Register a new partial safe nbt provider for a specific blockEntityType
	 *
	 * @param blockEntityType The block entity type you would like to register this for
	 * @param safeNBT         The custom PartialSafeNBT provider you would like to register for this blockEntityType,
	 *                        your {@link ContextProvidingPartialSafeNBT#writeSafe(BlockEntity, CompoundTag, HolderLookup.Provider)} method will be
	 *                        called on the passed {@link ContextProvidingPartialSafeNBT}
	 *                        when the block entities data is being prepared for placement.
	 */
	public static void register(BlockEntityType<? extends BlockEntity> blockEntityType, ContextProvidingPartialSafeNBT safeNBT) {
		SchematicSafeNBTRegistryImpl.register(blockEntityType, safeNBT);
	}

	// --- Interface that provides the context that would be available if you were to implement IPartialSafeNBT instead ---

	@FunctionalInterface
	public interface ContextProvidingPartialSafeNBT {
		/**
		 * This will always be called from the logical server
		 */
		void writeSafe(BlockEntity blockEntity, CompoundTag tag, HolderLookup.Provider registries);
	}
}
