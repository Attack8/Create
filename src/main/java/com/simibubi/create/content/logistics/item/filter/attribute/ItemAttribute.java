package com.simibubi.create.content.logistics.item.filter.attribute;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllRegistries;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public interface ItemAttribute {
	Codec<ItemAttribute> CODEC = AllRegistries.ITEM_ATTRIBUTE_TYPES.byNameCodec().dispatch(ItemAttribute::getType, ItemAttributeType::codec);

	static CompoundTag saveStatic(ItemAttribute attribute, HolderLookup.Provider registries) {
		CompoundTag nbt = new CompoundTag();
		nbt.put("attribute", CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE, registries), attribute).getOrThrow());
		return nbt;
	}

	@Nullable
	static ItemAttribute loadStatic(CompoundTag nbt, HolderLookup.Provider registries) {
		return CODEC.decode(RegistryOps.create(NbtOps.INSTANCE, registries), nbt.get("attribute")).getOrThrow().getFirst();
	}

	static List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
		List<ItemAttribute> attributes = new ArrayList<>();
		for (ItemAttributeType type : AllRegistries.ITEM_ATTRIBUTE_TYPES) {
			attributes.addAll(type.getAllAttributes(stack, level));
		}
		return attributes;
	}

	boolean appliesTo(ItemStack stack, Level world);

	ItemAttributeType getType();

	@OnlyIn(value = Dist.CLIENT)
	default MutableComponent format(boolean inverted) {
		return CreateLang.translateDirect("item_attributes." + getTranslationKey() + (inverted ? ".inverted" : ""),
			getTranslationParameters());
	}

	String getTranslationKey();

	default Object[] getTranslationParameters() {
		return new String[0];
	}

	record ItemAttributeEntry(ItemAttribute attribute, boolean inverted) {
		public static Codec<ItemAttribute.ItemAttributeEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
				ItemAttribute.CODEC.fieldOf("attribute").forGetter(ItemAttribute.ItemAttributeEntry::attribute),
				Codec.BOOL.fieldOf("inverted").forGetter(ItemAttribute.ItemAttributeEntry::inverted)
		).apply(i, ItemAttribute.ItemAttributeEntry::new));
	}
}
