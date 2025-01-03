package com.simibubi.create.foundation.recipe;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import com.simibubi.create.foundation.utility.RecipeGenericsUtil;

import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

/**
 * Utility for searching through a world's recipe collection. Non-dynamic
 * conditions can be split off into an initial search for caching intermediate
 * results.
 *
 * @author simibubi
 *
 */
public class RecipeFinder {

	private static Cache<Object, List<RecipeHolder<? extends Recipe<?>>>> cachedSearches = CacheBuilder.newBuilder().build();

	/**
	 * Find all IRecipes matching the condition predicate. If this search is made
	 * more than once, using the same object instance as the cacheKey will retrieve
	 * the cached result from the first time.
	 *
	 * @param cacheKey   (can be null to prevent the caching)
	 * @param world
	 * @param conditions
	 * @return A started search to continue with more specific conditions.
	 */
	public static List<RecipeHolder<? extends Recipe<?>>> get(@Nullable Object cacheKey, Level world, Predicate<RecipeHolder<? extends Recipe<?>>> conditions) {
		if (cacheKey == null)
			return startSearch(world, conditions);

		try {
			return cachedSearches.get(cacheKey, () -> startSearch(world, conditions));
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return Collections.emptyList();
	}

	private static List<RecipeHolder<? extends Recipe<?>>> startSearch(Level world, Predicate<? super RecipeHolder<? extends Recipe<?>>> conditions) {
		//noinspection RedundantCast
		return (List<RecipeHolder<? extends Recipe<?>>>) RecipeGenericsUtil.specify(world.getRecipeManager().getRecipes())
				.stream().filter(conditions)
				.collect(Collectors.toList());
	}

	public static final ResourceManagerReloadListener LISTENER = resourceManager -> {
		cachedSearches.invalidateAll();
	};

}
