package com.simibubi.create.foundation.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.ApiStatus;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.simibubi.create.Create;
import com.simibubi.create.content.kinetics.saw.CuttingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.pack.DynamicPack;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

@ApiStatus.Internal
public class RuntimeDataGenerator {
	private static final Pattern STRIPPED_WOODS_REGEX = Pattern.compile("stripped_(\\w*)_(log|wood|stem|hyphae)");
	private static final Pattern NON_STRIPPED_WOODS_REGEX = Pattern.compile("(?!stripped_)([a-z]+)_(log|wood|stem|hyphae)");
	private static final Multimap<ResourceLocation, TagEntry> TAGS = HashMultimap.create();
	private static final Object2ObjectOpenHashMap<ResourceLocation, JsonObject> JSON_FILES = new Object2ObjectOpenHashMap<>();

	public static void insertIntoPack(DynamicPack dynamicPack) {
		for (ResourceLocation itemId : BuiltInRegistries.ITEM.keySet())
			cuttingRecipes(itemId);

		Create.LOGGER.info("Created {} recipes which will be injected into the game", JSON_FILES.size());
		JSON_FILES.forEach(dynamicPack::put);

		Create.LOGGER.info("Created {} tags which will be injected into the game", TAGS.size());
		for (Map.Entry<ResourceLocation, Collection<TagEntry>> tags : TAGS.asMap().entrySet()) {
			TagFile tagFile = new TagFile(new ArrayList<>(tags.getValue()), false);
			dynamicPack.put(tags.getKey().withPrefix("tags/items/"), TagFile.CODEC.encodeStart(JsonOps.INSTANCE, tagFile).result().orElseThrow());
		}

		JSON_FILES.clear();
		JSON_FILES.trim();
		TAGS.clear();
	}

	// logs/woods -> stripped variants
	// logs/woods both stripped and non stripped -> planks
	// planks -> stairs, slabs, fences, fence gates, doors, trapdoors, pressure plates, buttons and signs
	private static void cuttingRecipes(ResourceLocation itemId) {
		String path = itemId.getPath();

		Matcher match = STRIPPED_WOODS_REGEX.matcher(path);
		boolean hasFoundMatch = match.find();

		// Last ditch attempt. Try to find logs without stripped variants
		boolean noStrippedVariant = false;
		if (!hasFoundMatch && !BuiltInRegistries.ITEM.containsKey(itemId.withPrefix("stripped_"))) {
			match = NON_STRIPPED_WOODS_REGEX.matcher(path);
			hasFoundMatch = match.find();
			noStrippedVariant = true;
		}

		if (hasFoundMatch) {
			String type = match.group(2);
			ResourceLocation matched = itemId.withPath(match.group(1));
			ResourceLocation base = matched.withSuffix("_");
			ResourceLocation nonStrippedId = base.withSuffix(type);
			ResourceLocation planksId = base.withSuffix("planks");
			ResourceLocation stairsId = base.withSuffix("stairs");
			ResourceLocation slabId = base.withSuffix("slab");
			ResourceLocation fenceId = base.withSuffix("fence");
			ResourceLocation fenceGateId = base.withSuffix("fence_gate");
			ResourceLocation doorId = base.withSuffix("door");
			ResourceLocation trapdoorId = base.withSuffix("trapdoor");
			ResourceLocation pressurePlateId = base.withSuffix("pressure_plate");
			ResourceLocation buttonId = base.withSuffix("button");
			ResourceLocation signId = base.withSuffix("sign");

			if (!noStrippedVariant) {
				simpleWoodRecipe(nonStrippedId, itemId);
				simpleWoodRecipe(itemId, planksId, 6);
			} else if (BuiltInRegistries.ITEM.containsKey(planksId)) {
				ResourceLocation tag = Create.asResource("runtime_generated/compat/" + itemId.getNamespace() + "/" + matched.getPath());
				insertIntoTag(tag, itemId);
				insertIntoTag(tag, nonStrippedId);

				simpleWoodRecipe(TagKey.create(Registries.ITEM, tag), planksId, 6);
			}

			if (!path.contains("_wood") && !path.contains("_hyphae") && BuiltInRegistries.ITEM.containsKey(planksId)) {
				simpleWoodRecipe(planksId, stairsId);
				simpleWoodRecipe(planksId, slabId, 2);
				simpleWoodRecipe(planksId, fenceId);
				simpleWoodRecipe(planksId, fenceGateId);
				simpleWoodRecipe(planksId, doorId);
				simpleWoodRecipe(planksId, trapdoorId);
				simpleWoodRecipe(planksId, pressurePlateId);
				simpleWoodRecipe(planksId, buttonId);
				simpleWoodRecipe(planksId, signId);
			}
		}
	}

	private static void insertIntoTag(ResourceLocation tag, ResourceLocation itemId) {
		if (BuiltInRegistries.ITEM.containsKey(itemId))
			TAGS.put(tag, TagEntry.optionalElement(itemId));
	}

	private static void simpleWoodRecipe(ResourceLocation inputId, ResourceLocation outputId) {
		simpleWoodRecipe(inputId, outputId, 1);
	}

	private static void simpleWoodRecipe(ResourceLocation inputId, ResourceLocation outputId, int amount) {
		if (BuiltInRegistries.ITEM.containsKey(outputId)) {
			new Builder<>(inputId.getNamespace(), CuttingRecipe::new, inputId.getPath(), outputId.getPath())
				.require(BuiltInRegistries.ITEM.get(inputId))
				.output(BuiltInRegistries.ITEM.get(outputId), amount)
				.duration(50)
				.build();
		}
	}

	private static void simpleWoodRecipe(TagKey<Item> inputTag, ResourceLocation outputId, int amount) {
		if (BuiltInRegistries.ITEM.containsKey(outputId)) {
			new Builder<>(inputTag.location().getNamespace(), CuttingRecipe::new, "tag_" + inputTag.location().getPath(), outputId.getPath())
				.require(inputTag)
				.output(BuiltInRegistries.ITEM.get(outputId), amount)
				.duration(50)
				.build();
		}
	}

	private static class Builder<T extends ProcessingRecipe<?>> extends ProcessingRecipeBuilder<T> {
		public Builder(String modid, ProcessingRecipeBuilder.ProcessingRecipeFactory<T> factory, String from, String to) {
			super(factory, Create.asResource("runtime_generated/compat/" + modid + "/" + from + "_to_" + to));
		}

		@Override
		public T build() {
			T t = super.build();
			DataGenResult<T> result = new DataGenResult<>(t, Collections.emptyList());
			JSON_FILES.put(result.getId().withPrefix("recipes/"), result.serializeRecipe());
			return t;
		}
	}
}
