package com.simibubi.create.compat.jei.category;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.world.item.Item;

import net.minecraft.world.item.crafting.ShapedRecipe;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.compat.jei.category.animations.AnimatedCrafter;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.createmod.catnip.utility.lang.Components;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;

import org.joml.Matrix4fStack;

@ParametersAreNonnullByDefault
public class MechanicalCraftingCategory extends CreateRecipeCategory<CraftingRecipe> {

	private final AnimatedCrafter crafter = new AnimatedCrafter();

	public MechanicalCraftingCategory(Info<CraftingRecipe> info) {
		super(info);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, CraftingRecipe recipe, IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.OUTPUT, 134, 81)
			.addItemStack(getResultItem(recipe));

		int x = getXPadding(recipe);
		int y = getYPadding(recipe);
		float scale = getScale(recipe);

		IIngredientRenderer<ItemStack> renderer = new CrafterIngredientRenderer(recipe);
		int i = 0;

		for (Ingredient ingredient : recipe.getIngredients()) {
			float f = 19 * scale;
			int xPosition = (int) (x + 1 + (i % getWidth(recipe)) * f);
			int yPosition = (int) (y + 1 + (i / getWidth(recipe)) * f);

			builder.addSlot(RecipeIngredientRole.INPUT, xPosition, yPosition)
				.setCustomRenderer(VanillaTypes.ITEM_STACK, renderer)
				.addIngredients(ingredient);

			i++;
		}

	}

	static int maxSize = 100;

	public static float getScale(CraftingRecipe recipe) {
		int w = getWidth(recipe);
		int h = getHeight(recipe);
		return Math.min(1, maxSize / (19f * Math.max(w, h)));
	}

	public static int getYPadding(CraftingRecipe recipe) {
		return 3 + 50 - (int) (getScale(recipe) * getHeight(recipe) * 19 * .5);
	}

	public static int getXPadding(CraftingRecipe recipe) {
		return 3 + 50 - (int) (getScale(recipe) * getWidth(recipe) * 19 * .5);
	}

	private static int getWidth(CraftingRecipe recipe) {
		return recipe instanceof ShapedRecipe ? ((ShapedRecipe) recipe).getWidth() : 1;
	}

	private static int getHeight(CraftingRecipe recipe) {
		return recipe instanceof ShapedRecipe ? ((ShapedRecipe) recipe).getHeight() : 1;
	}

	@Override
	public void draw(CraftingRecipe recipe, IRecipeSlotsView iRecipeSlotsView, GuiGraphics graphics, double mouseX,
		double mouseY) {
		PoseStack matrixStack = graphics.pose();
		matrixStack.pushPose();
		float scale = getScale(recipe);
		matrixStack.translate(getXPadding(recipe), getYPadding(recipe), 0);

		for (int row = 0; row < getHeight(recipe); row++)
			for (int col = 0; col < getWidth(recipe); col++) {
				int pIndex = row * getWidth(recipe) + col;
				if (pIndex >= recipe.getIngredients()
					.size())
					break;
				if (recipe.getIngredients()
					.get(pIndex)
					.isEmpty())
					continue;
				matrixStack.pushPose();
				matrixStack.translate(col * 19 * scale, row * 19 * scale, 0);
				matrixStack.scale(scale, scale, scale);
				AllGuiTextures.JEI_SLOT.render(graphics, 0, 0);
				matrixStack.popPose();
			}

		matrixStack.popPose();

		AllGuiTextures.JEI_SLOT.render(graphics, 133, 80);
		AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 128, 59);
		crafter.draw(graphics, 129, 25);

		matrixStack.pushPose();
		matrixStack.translate(0, 0, 300);

		int amount = 0;
		for (Ingredient ingredient : recipe.getIngredients()) {
			if (Ingredient.EMPTY == ingredient)
				continue;
			amount++;
		}

		graphics.drawString(Minecraft.getInstance().font, amount + "", 142, 39, 0xFFFFFF);
		matrixStack.popPose();
	}

	private static final class CrafterIngredientRenderer implements IIngredientRenderer<ItemStack> {

		private final CraftingRecipe recipe;
		private final float scale;

		public CrafterIngredientRenderer(CraftingRecipe recipe) {
			this.recipe = recipe;
			scale = getScale(recipe);
		}

		@Override
		public void render(GuiGraphics graphics, @NotNull ItemStack ingredient) {
			PoseStack matrixStack = graphics.pose();
			matrixStack.pushPose();
			float scale = getScale(recipe);
			matrixStack.scale(scale, scale, scale);

			if (ingredient != null) {
				Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
				modelViewStack.pushMatrix();
				RenderSystem.applyModelViewMatrix();
				RenderSystem.enableDepthTest();
				Minecraft minecraft = Minecraft.getInstance();
				Font font = getFontRenderer(minecraft, ingredient);
				graphics.renderItem(ingredient, 0, 0);
				graphics.renderItemDecorations(font, ingredient, 0, 0, null);
				RenderSystem.disableBlend();
				modelViewStack.popMatrix();
				RenderSystem.applyModelViewMatrix();
			}

			matrixStack.popPose();
		}

		@Override
		public int getWidth() {
			return (int) (16 * scale);
		}

		@Override
		public int getHeight() {
			return (int) (16 * scale);
		}

		@Override
		public List<Component> getTooltip(ItemStack ingredient, TooltipFlag tooltipFlag) {
			Minecraft minecraft = Minecraft.getInstance();
			Player player = minecraft.player;
			try {
				return ingredient.getTooltipLines(Item.TooltipContext.of(minecraft.level), player, tooltipFlag);
			} catch (RuntimeException | LinkageError e) {
				List<Component> list = new ArrayList<>();
				MutableComponent crash = Components.translatable("jei.tooltip.error.crash");
				list.add(crash.withStyle(ChatFormatting.RED));
				return list;
			}
		}
	}

}
