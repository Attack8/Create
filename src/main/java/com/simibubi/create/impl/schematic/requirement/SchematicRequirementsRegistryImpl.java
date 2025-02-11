package com.simibubi.create.impl.schematic.requirement;

import org.jetbrains.annotations.ApiStatus;

import com.simibubi.create.api.schematic.requirement.SchematicRequirementsRegistry.ContextProvidingBlockEntityRequirement;
import com.simibubi.create.api.schematic.requirement.SchematicRequirementsRegistry.ContextProvidingBlockRequirement;
import com.simibubi.create.api.schematic.requirement.SchematicRequirementsRegistry.ContextProvidingEntityRequirement;
import com.simibubi.create.foundation.utility.AttachedRegistry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

@ApiStatus.Internal
public class SchematicRequirementsRegistryImpl {
	private static final AttachedRegistry<Block, ContextProvidingBlockRequirement> BLOCK_REQUIREMENTS = new AttachedRegistry<>(BuiltInRegistries.BLOCK);
	private static final AttachedRegistry<BlockEntityType<?>, ContextProvidingBlockEntityRequirement> BLOCK_ENTITY_REQUIREMENTS = new AttachedRegistry<>(BuiltInRegistries.BLOCK_ENTITY_TYPE);
	private static final AttachedRegistry<EntityType<?>, ContextProvidingEntityRequirement> ENTITY_REQUIREMENTS = new AttachedRegistry<>(BuiltInRegistries.ENTITY_TYPE);

	public static void registerForBlock(Block block, ContextProvidingBlockRequirement requirement) {
		BLOCK_REQUIREMENTS.register(block, requirement);
	}

	public static void registerForBlock(ResourceLocation block, ContextProvidingBlockRequirement requirement) {
		BLOCK_REQUIREMENTS.register(block, requirement);
	}

	public static void registerForBlockEntity(BlockEntityType<BlockEntity> blockEntityType, ContextProvidingBlockEntityRequirement requirement) {
		BLOCK_ENTITY_REQUIREMENTS.register(blockEntityType, requirement);
	}

	public static void registerForBlockEntity(ResourceLocation blockEntityType, ContextProvidingBlockEntityRequirement requirement) {
		BLOCK_ENTITY_REQUIREMENTS.register(blockEntityType, requirement);
	}

	public static void registerForEntity(EntityType<Entity> entityType, ContextProvidingEntityRequirement requirement) {
		ENTITY_REQUIREMENTS.register(entityType, requirement);
	}

	// ---

	public static void registerForEntity(ResourceLocation entityType, ContextProvidingEntityRequirement requirement) {
		ENTITY_REQUIREMENTS.register(entityType, requirement);
	}

	public static ContextProvidingBlockRequirement getRequirementForBlock(Block block) {
		return BLOCK_REQUIREMENTS.get(block);
	}

	public static ContextProvidingBlockEntityRequirement getRequirementForBlockEntityType(BlockEntityType<? extends BlockEntity> blockEntityType) {
		return BLOCK_ENTITY_REQUIREMENTS.get(blockEntityType);
	}

	public static ContextProvidingEntityRequirement getRequirementForEntityType(EntityType<? extends Entity> entityType) {
		return ENTITY_REQUIREMENTS.get(entityType);
	}
}
