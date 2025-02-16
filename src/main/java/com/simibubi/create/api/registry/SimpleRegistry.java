package com.simibubi.create.api.registry;

import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.impl.registry.SimpleRegistryImpl;
import com.simibubi.create.impl.registry.TagProviderImpl;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * A simple registry mapping between objects. Provides simple registration functionality, as well as lazy providers.
 * This class is thread-safe, and may be safely used during parallel mod init.
 */
public interface SimpleRegistry<K, V> {
	/**
	 * Register an association between a key and a value.
	 * Direct registrations here always take priority over providers.
	 * @throws IllegalArgumentException if the object already has an associated value
	 */
	void register(K object, V value);

	/**
	 * Add a new provider to this registry. For information on providers, see {@link Provider}.
	 * @throws IllegalArgumentException if the provider has already been registered to this registry
	 */
	void registerProvider(Provider<K, V> provider);

	/**
	 * Invalidate the cached values provided by all providers, so they get re-computed on the next query.
	 * This should be called by providers when something changes that would affect their results, such as
	 * a resource reload in the case of providers based on tags.
	 */
	void invalidate();

	/**
	 * Query the value associated with the given object. May be null if no association is present.
	 */
	@Nullable
	V get(K object);

	static <K, V> SimpleRegistry<K, V> create() {
		return new SimpleRegistryImpl<>();
	}

	/**
	 * A provider can provide values to the registry in a lazy fashion. When a key does not have an
	 * associated value, all providers will be queried in reverse-registration order (newest first).
	 * <p>
	 * The values returned by providers are cached so that repeated queries always return the same value.
	 * To invalidate the cache of a registry, call {@link SimpleRegistry#invalidate()}.
	 */
	@FunctionalInterface
	interface Provider<K, V> {
		@Nullable
		V get(K object);

		/**
		 * Called by the SimpleRegistry this provider is registered to after it's registered.
		 * This is useful for behavior that should only happen if a provider is actually registered,
		 * such as registering event listeners.
		 */
		default void onRegister(SimpleRegistry<K, V> registry) {
		}

		/**
		 * Create a provider that will return the same value for all entries in a tag.
		 * The Provider will invalidate itself when tags are reloaded.
		 */
		static <K, V> Provider<K, V> forTag(TagKey<K> tag, Function<K, Holder<K>> holderGetter, V value) {
			return new TagProviderImpl<>(tag, holderGetter, value);
		}

		/**
		 * Shortcut for {@link #forTag} when the registry's type is Block.
		 */
		@SuppressWarnings("deprecation")
		static <V> Provider<Block, V> forBlockTag(TagKey<Block> tag, V value) {
			return new TagProviderImpl<>(tag, Block::builtInRegistryHolder, value);
		}

		/**
		 * Shortcut for {@link #forTag} when the registry's type is Item.
		 */
		@SuppressWarnings("deprecation")
		static <V> Provider<Item, V> forItemTag(TagKey<Item> tag, V value) {
			return new TagProviderImpl<>(tag, Item::builtInRegistryHolder, value);
		}
	}
}
