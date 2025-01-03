package com.simibubi.create.foundation.item;

import net.minecraft.core.registries.BuiltInRegistries;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.utility.AttachedRegistry;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public interface TooltipModifier {
	AttachedRegistry<Item, TooltipModifier> REGISTRY = new AttachedRegistry<>(BuiltInRegistries.ITEM);

	TooltipModifier EMPTY = new TooltipModifier() {
		@Override
		public void modify(ItemTooltipEvent context) {
		}

		@Override
		public TooltipModifier andThen(TooltipModifier after) {
			return after;
		}
	};

	void modify(ItemTooltipEvent context);

	default TooltipModifier andThen(TooltipModifier after) {
		if (after == EMPTY) {
			return this;
		}
		return tooltip -> {
			modify(tooltip);
			after.modify(tooltip);
		};
	}

	static TooltipModifier mapNull(@Nullable TooltipModifier modifier) {
		if (modifier == null) {
			return EMPTY;
		}
		return modifier;
	}
}
