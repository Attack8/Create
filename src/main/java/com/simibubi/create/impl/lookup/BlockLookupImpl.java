package com.simibubi.create.impl.lookup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.lookup.BlockLookup;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

@ApiStatus.Internal
@EventBusSubscriber
public class BlockLookupImpl<T> implements BlockLookup<T> {
	private static final List<BlockLookupImpl<?>> allLookups = new ArrayList<>();

	private final Map<Block, T> map;
	private final Map<Block, T> providedValues;
	private final Set<Block> providedNull;
	private final List<Provider<T>> providers;

	public BlockLookupImpl() {
		this.map = new IdentityHashMap<>();
		this.providedValues = new IdentityHashMap<>();
		this.providedNull = new HashSet<>();
		this.providers = new ArrayList<>();
		allLookups.add(this);
	}

	@Nullable
	@Override
	public T find(BlockState state) {
		return this.find(state.getBlock());
	}

	@Nullable
	@Override
	public T find(Block block) {
		T registered = this.map.get(block);
		if (registered != null)
			return registered;

		if (this.providedNull.contains(block))
			return null;

		return this.providedValues.computeIfAbsent(block, $ -> {
			for (Provider<T> provider : this.providers) {
				T value = provider.get(block);
				if (value != null) {
					return value;
				}
			}

			this.providedNull.add(block);
			return null;
		});
	}

	@Override
	public void register(Block block, T value) {
		this.map.put(block, value);
	}

	@Override
	public void registerTag(TagKey<Block> tag, T value) {
		this.registerProvider(new TagProvider<>(tag, value));
	}

	@Override
	public void registerProvider(Provider<T> provider) {
		this.providers.add(0, provider);
	}

	@SubscribeEvent
	public static void onTagsReloaded(TagsUpdatedEvent event) {
		if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
			for (BlockLookupImpl<?> lookup : allLookups) {
				lookup.providedValues.clear();
				lookup.providedNull.clear();
			}
		}
	}

	private record TagProvider<T>(TagKey<Block> tag, T value) implements Provider<T> {
		@Override
		@Nullable
		@SuppressWarnings("deprecation")
		public T get(Block block) {
			return block.builtInRegistryHolder().is(this.tag) ? this.value : null;
		}
	}
}
