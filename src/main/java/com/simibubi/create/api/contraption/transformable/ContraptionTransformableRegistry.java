package com.simibubi.create.api.contraption.transformable;

import com.simibubi.create.impl.contraption.transformable.ContraptionTransformableRegistryImpl;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Registry for registering new contraption transformations
 * to properly place blocks when disassembled after being part of a contraption
 */
public class ContraptionTransformableRegistry {
	/**
	 * Register a new transform for a provided block
	 *
	 * @param block              The block you want to register a new {@link TransformableBlock} for
	 * @param transformableBlock The transform that should be applied whenever this block is being placed from
	 *                           contraption disassembly
	 */
	public static void registerForBlock(Block block, TransformableBlock transformableBlock) {
		ContraptionTransformableRegistryImpl.registerForBlock(block, transformableBlock);
	}

	/**
	 * Register a new transform for a provided block entity type
	 *
	 * @param blockEntityType          The blockEntityType you want to register a new {@link TransformableBlockEntity} for
	 * @param transformableBlockEntity The transform that should be applied whenever this block entity type is
	 *                                 being placed from contraption disassembly
	 */
	public static void registerForBlockEntity(BlockEntityType<?> blockEntityType, TransformableBlockEntity transformableBlockEntity) {
		ContraptionTransformableRegistryImpl.registerForBlockEntity(blockEntityType, transformableBlockEntity);
	}
}
