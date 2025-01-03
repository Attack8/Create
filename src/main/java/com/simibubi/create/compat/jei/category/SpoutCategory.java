package com.simibubi.create.compat.jei.category;

import java.util.Collection;
import java.util.function.Consumer;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.Create;
import com.simibubi.create.compat.jei.category.animations.AnimatedSpout;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.item.ItemHelper;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import net.createmod.catnip.utility.RegisteredObjectsHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

@ParametersAreNonnullByDefault
public class SpoutCategory extends CreateRecipeCategory<FillingRecipe> {

	private final AnimatedSpout spout = new AnimatedSpout();

	public SpoutCategory(Info<FillingRecipe> info) {
		super(info);
	}

	public static void consumeRecipes(Consumer<RecipeHolder<FillingRecipe>> consumer, IIngredientManager ingredientManager) {
		Collection<FluidStack> fluidStacks = ingredientManager.getAllIngredients(NeoForgeTypes.FLUID_STACK);
		for (ItemStack stack : ingredientManager.getAllIngredients(VanillaTypes.ITEM_STACK)) {
			if (PotionFluidHandler.isPotionItem(stack)) {
				FluidStack fluidFromPotionItem = PotionFluidHandler.getFluidFromPotionItem(stack);
				Ingredient bottle = Ingredient.of(Items.GLASS_BOTTLE);
				ResourceLocation id = Create.asResource("potions");
				FillingRecipe recipe = new ProcessingRecipeBuilder<>(FillingRecipe::new, id)
						.withItemIngredients(bottle)
						.withFluidIngredients(FluidIngredient.fromFluidStack(fluidFromPotionItem))
						.withSingleItemOutput(stack)
						.build();
				consumer.accept(new RecipeHolder<>(id, recipe));
				continue;
			}

			IFluidHandlerItem capability = stack.getCapability(Capabilities.FluidHandler.ITEM);
			if (capability == null)
				continue;

			for (FluidStack fluidStack : fluidStacks) {
				ItemStack copy = stack.copy();
				IFluidHandlerItem fhi = copy.getCapability(Capabilities.FluidHandler.ITEM);
				if (fhi != null) {
					if (!GenericItemFilling.isFluidHandlerValid(copy, fhi))
						return;
					FluidStack fluidCopy = fluidStack.copy();
					fluidCopy.setAmount(1000);
					fhi.fill(fluidCopy, FluidAction.EXECUTE);
					ItemStack container = fhi.getContainer();
					if (ItemHelper.sameItem(container, copy))
						return;
					if (container.isEmpty())
						return;

					Ingredient bucket = Ingredient.of(stack);
					ResourceLocation itemName = RegisteredObjectsHelper.getKeyOrThrow(stack.getItem());
					ResourceLocation fluidName = RegisteredObjectsHelper.getKeyOrThrow(fluidCopy.getFluid());
					ResourceLocation id = Create.asResource("fill_" + itemName.getNamespace() + "_" + itemName.getPath()
							+ "_with_" + fluidName.getNamespace() + "_" + fluidName.getPath());
					FillingRecipe recipe = new ProcessingRecipeBuilder<>(FillingRecipe::new, id)
							.withItemIngredients(bucket)
							.withFluidIngredients(FluidIngredient.fromFluidStack(fluidCopy))
							.withSingleItemOutput(container)
							.build();
					consumer.accept(new RecipeHolder<>(id, recipe));
				}
			}
		}
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, FillingRecipe recipe, IFocusGroup focuses) {
		builder
				.addSlot(RecipeIngredientRole.INPUT, 27, 51)
				.setBackground(getRenderedSlot(), -1, -1)
				.addIngredients(recipe.getIngredients().get(0));
		builder
				.addSlot(RecipeIngredientRole.INPUT, 27, 32)
				.setBackground(getRenderedSlot(), -1, -1)
				.addIngredients(NeoForgeTypes.FLUID_STACK, withImprovedVisibility(recipe.getRequiredFluid().getMatchingFluidStacks()))
				.addTooltipCallback(addFluidTooltip(recipe.getRequiredFluid().getRequiredAmount()));
		builder
				.addSlot(RecipeIngredientRole.OUTPUT, 132, 51)
				.setBackground(getRenderedSlot(), -1, -1)
				.addItemStack(getResultItem(recipe));
	}

	@Override
	public void draw(FillingRecipe recipe, IRecipeSlotsView iRecipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SHADOW.render(graphics, 62, 57);
		AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 126, 29);
		spout.withFluids(recipe.getRequiredFluid()
			.getMatchingFluidStacks())
			.draw(graphics, getBackground().getWidth() / 2 - 13, 22);
	}

}
