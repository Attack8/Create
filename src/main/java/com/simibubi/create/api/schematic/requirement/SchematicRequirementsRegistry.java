package com.simibubi.create.api.schematic.requirement;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.impl.schematic.requirement.SchematicRequirementsRegistryImpl;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Registry for schematic requirements for blocks, block entities, and entities.
 */
public class SchematicRequirementsRegistry {
	/**
	 * Register a new special requirement for a specified block
	 *
	 * @param block       The block you want to register a {@link ContextProvidingBlockRequirement} for
	 * @param requirement The requirement you would like to add to this block,
	 *                    the {@link ContextProvidingBlockRequirement#getRequiredItems(BlockState, BlockEntity)}
	 *                    method will be called on the {@link ContextProvidingBlockRequirement} you have provided,
	 *                    and you will be able to insert requirements based off the context that is given
	 */
	public static void registerForBlock(Block block, ContextProvidingBlockRequirement requirement) {
		SchematicRequirementsRegistryImpl.registerForBlock(block, requirement);
	}

	/**
	 * Register a new special requirement for a specified block
	 *
	 * @param block       The id of the block you want to register a {@link ContextProvidingBlockRequirement} for
	 * @param requirement The requirement you would like to add to this block,
	 *                    the {@link ContextProvidingBlockRequirement#getRequiredItems(BlockState, BlockEntity)}
	 *                    method will be called on the {@link ContextProvidingBlockRequirement} you have provided,
	 *                    and you will be able to insert requirements based off the context that is given
	 */
	public static void registerForBlock(ResourceLocation block, ContextProvidingBlockRequirement requirement) {
		SchematicRequirementsRegistryImpl.registerForBlock(block, requirement);
	}

	/**
	 * Register a new special requirement for a specified block entity type
	 *
	 * @param blockEntityType The blockEntityType you want to register a {@link ContextProvidingBlockEntityRequirement} for
	 * @param requirement     The requirement you would like to add to this block entity type,
	 *                        the {@link ContextProvidingBlockEntityRequirement#getRequiredItems(BlockEntity)}
	 *                        method will be called on the
	 *                        {@link ContextProvidingBlockEntityRequirement} you have provided,
	 *                        and you will be able to insert requirements based off the context that is given
	 */
	public static void registerForBlockEntity(BlockEntityType<BlockEntity> blockEntityType, ContextProvidingBlockEntityRequirement requirement) {
		SchematicRequirementsRegistryImpl.registerForBlockEntity(blockEntityType, requirement);
	}

	/**
	 * Register a new special requirement for a specified block entity type
	 *
	 * @param blockEntityType The id of the blockEntityType you want to register a {@link ContextProvidingBlockEntityRequirement} for
	 * @param requirement     The requirement you would like to add to this block entity type,
	 *                        the {@link ContextProvidingBlockEntityRequirement#getRequiredItems(BlockEntity)}
	 *                        method will be called on the
	 *                        {@link ContextProvidingBlockEntityRequirement} you have provided,
	 *                        and you will be able to insert requirements based off the context that is given
	 */
	public static void registerForBlockEntity(ResourceLocation blockEntityType, ContextProvidingBlockEntityRequirement requirement) {
		SchematicRequirementsRegistryImpl.registerForBlockEntity(blockEntityType, requirement);
	}

	/**
	 * Register a new special requirement for a specified entity type
	 *
	 * @param entityType  The entityType you want to register a {@link ContextProvidingEntityRequirement} for
	 * @param requirement The requirement you would like to add to this entity type,
	 *                    the {@link ContextProvidingEntityRequirement#getRequiredItems(Entity)}
	 *                    method will be called on the {@link ContextProvidingEntityRequirement} you have provided,
	 *                    and you will be able to insert requirements based off the context that is given
	 */
	public static void registerForEntity(EntityType<Entity> entityType, ContextProvidingEntityRequirement requirement) {
		SchematicRequirementsRegistryImpl.registerForEntity(entityType, requirement);
	}

	/**
	 * Register a new special requirement for a specified entity type
	 *
	 * @param entityType  The id of the entityType you want to register a {@link ContextProvidingEntityRequirement} for
	 * @param requirement The requirement you would like to add to this entity type,
	 *                    the {@link ContextProvidingEntityRequirement#getRequiredItems(Entity)}
	 *                    method will be called on the {@link ContextProvidingEntityRequirement} you have provided,
	 *                    and you will be able to insert requirements based off the context that is given
	 */
	public static void registerForEntity(ResourceLocation entityType, ContextProvidingEntityRequirement requirement) {
		SchematicRequirementsRegistryImpl.registerForEntity(entityType, requirement);
	}

	// --- Interfaces that provide the context that would be accessible if you implemented the ISpecial* interfaces ---

	@FunctionalInterface
	public interface ContextProvidingBlockRequirement {
		ItemRequirement getRequiredItems(BlockState state, @Nullable BlockEntity blockEntity);
	}

	@FunctionalInterface
	public interface ContextProvidingBlockEntityRequirement {
		ItemRequirement getRequiredItems(BlockEntity blockEntity);
	}

	@FunctionalInterface
	public interface ContextProvidingEntityRequirement {
		ItemRequirement getRequiredItems(Entity entity);
	}
}

