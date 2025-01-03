package com.simibubi.create.content.kinetics.crafter;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.foundation.mixin.accessor.ShapedRecipeAccessor;
import com.simibubi.create.infrastructure.codec.CombiningCodec;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

public class MechanicalCraftingRecipe extends ShapedRecipe {

	private final boolean acceptMirrored;

	public MechanicalCraftingRecipe(String groupIn, CraftingBookCategory category,
									ShapedRecipePattern pattern, ItemStack recipeOutputIn, boolean acceptMirrored) {
		super(groupIn, category, pattern, recipeOutputIn, acceptMirrored);
		this.acceptMirrored = acceptMirrored;
	}

	private static MechanicalCraftingRecipe fromShaped(ShapedRecipe recipe, boolean acceptMirrored) {
		return new MechanicalCraftingRecipe(recipe.getGroup(), recipe.category(), ((ShapedRecipeAccessor) recipe).create$getPattern(), recipe.getResultItem(null), acceptMirrored);
	}

	@Override
	public boolean matches(CraftingInput input, Level worldIn) {
		if (!(input instanceof MechanicalCraftingInput))
			return false;
		if (acceptsMirrored())
			return super.matches(input, worldIn);

		// From ShapedRecipe except the symmetry
		for (int i = 0; i <= input.width() - this.getWidth(); ++i)
			for (int j = 0; j <= input.height() - this.getHeight(); ++j)
				if (this.matchesSpecific(input, i, j))
					return true;
		return false;
	}

	// From ShapedRecipe
	private boolean matchesSpecific(CraftingInput input, int p_77573_2_, int p_77573_3_) {
		NonNullList<Ingredient> ingredients = getIngredients();
		int width = getWidth();
		int height = getHeight();
		for (int i = 0; i < input.width(); ++i) {
			for (int j = 0; j < input.height(); ++j) {
				int k = i - p_77573_2_;
				int l = j - p_77573_3_;
				Ingredient ingredient = Ingredient.EMPTY;
				if (k >= 0 && l >= 0 && k < width && l < height)
					ingredient = ingredients.get(k + l * width);
				if (!ingredient.test(input.getItem(i + j * input.width())))
					return false;
			}
		}
		return true;
	}

	@Override
	public RecipeType<?> getType() {
		return AllRecipeTypes.MECHANICAL_CRAFTING.getType();
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public @NotNull RecipeSerializer<?> getSerializer() {
		return AllRecipeTypes.MECHANICAL_CRAFTING.getSerializer();
	}

	public boolean acceptsMirrored() {
		return acceptMirrored;
	}

	public static class Serializer extends ShapedRecipe.Serializer {
		public static final MapCodec<MechanicalCraftingRecipe> CODEC = CombiningCodec.ofExtending(
				RecipeSerializer.SHAPED_RECIPE.codec(),
				Codec.BOOL.fieldOf("acceptMirrored"),
				MechanicalCraftingRecipe::fromShaped,
				MechanicalCraftingRecipe::acceptsMirrored
		);

		public static final StreamCodec<RegistryFriendlyByteBuf, ShapedRecipe> STREAM_CODEC = StreamCodec.of(
				Serializer::toNetwork, Serializer::fromNetwork
		);

		public static @NotNull ShapedRecipe fromNetwork(@NotNull RegistryFriendlyByteBuf buffer) {
			return fromShaped(ShapedRecipe.Serializer.STREAM_CODEC.decode(buffer), buffer.readBoolean() && buffer.readBoolean());
		}

		public static void toNetwork(@NotNull RegistryFriendlyByteBuf buffer, @NotNull ShapedRecipe recipe) {
			ShapedRecipe.Serializer.STREAM_CODEC.encode(buffer, recipe);
			if (recipe instanceof MechanicalCraftingRecipe) {
				buffer.writeBoolean(true);
				buffer.writeBoolean(((MechanicalCraftingRecipe) recipe).acceptsMirrored());
			} else
				buffer.writeBoolean(false);
		}

		@SuppressWarnings({"unchecked", "rawtypes"})
		@Override
		public @NotNull MapCodec<ShapedRecipe> codec() {
			return (MapCodec) CODEC;
		}

		@Override
		public @NotNull StreamCodec<RegistryFriendlyByteBuf, ShapedRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}

}
