package com.simibubi.create.compat.emi.recipes;

import com.simibubi.create.compat.emi.CreateEmiPlugin;
import com.simibubi.create.compat.emi.RenderedBlock;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.Lang;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.ChatFormatting;

public class ManualItemApplicationEmiRecipe extends CreateEmiRecipe<ItemApplicationRecipe> {
	public ManualItemApplicationEmiRecipe(ItemApplicationRecipe recipe) {
		super(CreateEmiPlugin.ITEM_APPLICATION, recipe, 177, 60);
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		EmiIngredient base = input.get(0);
		addSlot(widgets, base, 27, 38);

		RenderedBlock block = RenderedBlock.of(base);
		if (block != null) {
			widgets.addDrawable(0, 0, 0, 0, block);
		}

		SlotWidget held = addSlot(widgets, input.get(1), 51, 5);
		if (recipe.shouldKeepHeldItem()) {
			held.catalyst(true);
			held.appendTooltip(Lang.translateDirect("recipe.deploying.not_consumed").withStyle(ChatFormatting.GOLD));
		}

		addSlot(widgets, output.get(0), 132, 38);

		addTexture(widgets, AllGuiTextures.JEI_SHADOW, 62, 47);
		addTexture(widgets, AllGuiTextures.JEI_DOWN_ARROW, 74, 10);
	}
}
