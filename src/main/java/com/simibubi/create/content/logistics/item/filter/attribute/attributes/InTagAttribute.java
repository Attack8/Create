package com.simibubi.create.content.logistics.item.filter.attribute.attributes;

import java.util.List;
import java.util.stream.Collectors;

import net.createmod.catnip.nbt.NBTHelper;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.logistics.item.filter.attribute.AllItemAttributeTypes;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record InTagAttribute(TagKey<Item> tag) implements ItemAttribute {
	public static final MapCodec<InTagAttribute> CODEC = TagKey.codec(Registries.ITEM)
			.xmap(InTagAttribute::new, InTagAttribute::tag)
			.fieldOf("value");

	@Override
	public boolean appliesTo(ItemStack stack, Level level) {
		return stack.is(tag);
	}

	@Override
	public String getTranslationKey() {
		return "in_tag";
	}

	@Override
	public Object[] getTranslationParameters() {
		return new Object[]{"#" + tag.location()};
	}

	@Override
	public ItemAttributeType getType() {
		return AllItemAttributeTypes.IN_TAG;
	}

	public static class Type implements ItemAttributeType {
		@Override
		public @NotNull ItemAttribute createAttribute() {
			return new InTagAttribute(ItemTags.LOGS);
		}

		@Override
		public List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
			return stack.getTags()
				.map(InTagAttribute::new)
				.collect(Collectors.toList());
		}

		@Override
		public MapCodec<? extends ItemAttribute> codec() {
			return CODEC;
		}
	}
}
