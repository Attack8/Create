package com.simibubi.create.api.lookup;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.impl.lookup.BlockLookupImpl;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Lookup for objects provided by blocks. Values can either be registered directly
 * or found lazily through providers. Providers are only queried once per block.
 * If they return a value, that value is cached. If they don't, that block is recorded
 * as not having a corresponding value.
 * <p>
 * Provided values are reset on resource reloads and will be re-queried and re-cached the
 * next time a block is queried.
 * <p>
 * All providers are expected to be registered synchronously during game init.
 * Adding new ones late is not supported.
 */
@ApiStatus.NonExtendable
public interface BlockLookup<T> {
	@Nullable
	T find(Block block);

	/**
	 * Shortcut to avoid calling getBlock() on a BlockState.
	 */
	@Nullable
	T find(BlockState state);

	/**
	 * Register a value to one block.
	 */
	void register(Block block, T value);

	/**
	 * Register a value to all entries of a tag.
	 */
	void registerTag(TagKey<Block> tag, T value);

	/**
	 * Register a new provider that will be queried.
	 * Providers are queried in reverse-registration order.
	 */
	void registerProvider(Provider<T> provider);

	static <T> BlockLookup<T> create() {
		return new BlockLookupImpl<>();
	}

	static <T> BlockLookup<T> create(Provider<T> initialProvider) {
		BlockLookup<T> lookup = new BlockLookupImpl<>();
		lookup.registerProvider(initialProvider);
		return lookup;
	}

	@FunctionalInterface
	interface Provider<T> {
		@Nullable
		T get(Block block);
	}
}
