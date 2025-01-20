package com.simibubi.create.content.processing.sequenced;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.Create;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.utility.CreateLang;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.lang.Components;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

public class SequencedAssemblyRecipe implements Recipe<RecipeWrapper> {

	protected SequencedAssemblyRecipeSerializer serializer;

	protected Ingredient ingredient;
	protected List<SequencedRecipe<?>> sequence;
	protected int loops;
	protected ProcessingOutput transitionalItem;

	public final List<ProcessingOutput> resultPool;

	public SequencedAssemblyRecipe(SequencedAssemblyRecipeSerializer serializer) {
		this.serializer = serializer;
		sequence = new ArrayList<>();
		resultPool = new ArrayList<>();
		loops = 5;
	}

	public static <I extends RecipeInput, R extends ProcessingRecipe<I>> Optional<RecipeHolder<R>> getRecipe(Level world, I inv,
																										   RecipeType<R> type, Class<R> recipeClass) {
		return getRecipe(world, inv, type, recipeClass, r -> r.value().matches(inv, world));
	}

	public static <I extends RecipeInput, R extends ProcessingRecipe<I>> Optional<RecipeHolder<R>> getRecipe(Level world, I inv,
																											 RecipeType<R> type, Class<R> recipeClass, Predicate<? super RecipeHolder<R>> recipeFilter) {
		return getRecipes(world, inv.getItem(0), type, recipeClass).filter(recipeFilter)
			.findFirst();
	}

	public static <R extends ProcessingRecipe<?>> Optional<RecipeHolder<R>> getRecipe(Level world, ItemStack item,
																					  RecipeType<R> type, Class<R> recipeClass) {
		List<RecipeHolder<SequencedAssemblyRecipe>> all = world.getRecipeManager()
			.getAllRecipesFor(AllRecipeTypes.SEQUENCED_ASSEMBLY.getType());
		for (RecipeHolder<SequencedAssemblyRecipe> sequencedAssemblyRecipe : all) {
			if (!sequencedAssemblyRecipe.value().appliesTo(sequencedAssemblyRecipe.id(), item))
				continue;
			SequencedRecipe<?> nextRecipe = sequencedAssemblyRecipe.value().getNextRecipe(item);
			ProcessingRecipe<?> recipe = nextRecipe.getRecipe();
			if (recipe.getType() != type || !recipeClass.isInstance(recipe))
				continue;
			recipe.enforceNextResult(() -> sequencedAssemblyRecipe.value().advance(sequencedAssemblyRecipe.id(), item));
			return Optional.of(new RecipeHolder<>(sequencedAssemblyRecipe.id(), recipeClass.cast(recipe)));
		}
		return Optional.empty();
	}

	public static <R extends ProcessingRecipe<?>> Stream<RecipeHolder<R>> getRecipes(Level world, ItemStack item,
																					 RecipeType<R> type, Class<R> recipeClass) {
		List<RecipeHolder<SequencedAssemblyRecipe>> all = world.getRecipeManager()
			.<RecipeWrapper, SequencedAssemblyRecipe>getAllRecipesFor(AllRecipeTypes.SEQUENCED_ASSEMBLY.getType());

		List<RecipeHolder<R>> result = new ArrayList<>();

		for (RecipeHolder<SequencedAssemblyRecipe> holder : all) {
			if (holder.value().appliesTo(holder.id(), item)) {
				ProcessingRecipe<?> recipe = holder.value().getNextRecipe(item).getRecipe();

				if (recipe.getType() == type && recipeClass.isInstance(recipe)) {
					recipe.enforceNextResult(() -> holder.value().advance(holder.id(), item));
					R castedRecipe = recipeClass.cast(recipe);
					result.add(new RecipeHolder<>(recipe.id, castedRecipe));
				}
			}
		}

		return result.stream();
	}

	private ItemStack advance(ResourceLocation id, ItemStack input) {
		int step = getStep(input);
		if ((step + 1) / sequence.size() >= loops)
			return rollResult();

		ItemStack advancedItem = getTransitionalItem().copyWithCount(1);
		SequencedAssembly sequencedAssembly = new SequencedAssembly(
				id,
				step + 1,
				(step + 1f) / (sequence.size() * loops)
		);
		advancedItem.set(AllDataComponents.SEQUENCED_ASSEMBLY, sequencedAssembly);
		return advancedItem;
	}

	public int getLoops() {
		return loops;
	}

	public void addAdditionalIngredientsAndMachines(List<Ingredient> list) {
		sequence.forEach(sr -> sr.getAsAssemblyRecipe()
			.addAssemblyIngredients(list));
		Set<ItemLike> machines = new HashSet<>();
		sequence.forEach(sr -> sr.getAsAssemblyRecipe()
			.addRequiredMachines(machines));
		machines.stream()
			.map(Ingredient::of)
			.forEach(list::add);
	}

	public void addAdditionalFluidIngredients(List<FluidIngredient> list) {
		sequence.forEach(sr -> sr.getAsAssemblyRecipe()
			.addAssemblyFluidIngredients(list));
	}

	private ItemStack rollResult() {
		float totalWeight = 0;
		for (ProcessingOutput entry : resultPool)
			totalWeight += entry.getChance();
		float number = Create.RANDOM.nextFloat() * totalWeight;
		for (ProcessingOutput entry : resultPool) {
			number -= entry.getChance();
			if (number < 0)
				return entry.getStack()
					.copy();
		}
		return ItemStack.EMPTY;
	}

	private boolean appliesTo(ResourceLocation id, ItemStack input) {
		if (ingredient.test(input))
			return true;
		//noinspection DataFlowIssue
		return getTransitionalItem().getItem() == input.getItem() && input
			.has(AllDataComponents.SEQUENCED_ASSEMBLY) && input
				.get(AllDataComponents.SEQUENCED_ASSEMBLY)
				.id()
				.equals(id);
	}

	private SequencedRecipe<?> getNextRecipe(ItemStack input) {
		return sequence.get(getStep(input) % sequence.size());
	}

	private int getStep(ItemStack input) {
		if (!input.has(AllDataComponents.SEQUENCED_ASSEMBLY))
			return 0;
		//noinspection DataFlowIssue
		return input.get(AllDataComponents.SEQUENCED_ASSEMBLY).step();
	}

	@Override
	public boolean matches(RecipeWrapper inv, Level p_77569_2_) {
		return false;
	}

	@Override
	public ItemStack assemble(RecipeWrapper input, HolderLookup.Provider registries) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int p_194133_1_, int p_194133_2_) {
		return false;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registries) {
		return resultPool.getFirst().getStack();
	}

	public float getOutputChance() {
		float totalWeight = 0;
		for (ProcessingOutput entry : resultPool)
			totalWeight += entry.getChance();
		return resultPool.get(0)
			.getChance() / totalWeight;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return serializer;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public RecipeType<?> getType() {
		return AllRecipeTypes.SEQUENCED_ASSEMBLY.getType();
	}

	@OnlyIn(Dist.CLIENT)
	public static void addToTooltip(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		if (!stack.has(AllDataComponents.SEQUENCED_ASSEMBLY))
			return;
		SequencedAssembly sequencedAssembly = stack.get(AllDataComponents.SEQUENCED_ASSEMBLY);
		@SuppressWarnings({"RedundantCast", "DataFlowIssue"}) // The java compiler thinks `byKey` returns an Optional<RecipeHolder<?>>
		Optional<RecipeHolder<? extends Recipe<?>>> optionalRecipe =
			(Optional<RecipeHolder<?>>) Minecraft.getInstance().level.getRecipeManager()
				.byKey(sequencedAssembly.id());
		if (!optionalRecipe.isPresent())
			return;
		Recipe<?> recipe = optionalRecipe.get().value();
		if (!(recipe instanceof SequencedAssemblyRecipe sequencedAssemblyRecipe))
			return;

		int length = sequencedAssemblyRecipe.sequence.size();
		int step = sequencedAssemblyRecipe.getStep(stack);
		int total = length * sequencedAssemblyRecipe.loops;
		List<Component> tooltip = event.getToolTip();
		tooltip.add(Components.immutableEmpty());
		tooltip.add(CreateLang.translateDirect("recipe.sequenced_assembly")
			.withStyle(ChatFormatting.GRAY));
		tooltip.add(CreateLang.translateDirect("recipe.assembly.progress", step, total)
			.withStyle(ChatFormatting.DARK_GRAY));

		int remaining = total - step;
		for (int i = 0; i < length; i++) {
			if (i >= remaining)
				break;
			SequencedRecipe<?> sequencedRecipe = sequencedAssemblyRecipe.sequence.get((i + step) % length);
			Component textComponent = sequencedRecipe.getAsAssemblyRecipe()
				.getDescriptionForAssembly();
			if (i == 0)
				tooltip.add(CreateLang.translateDirect("recipe.assembly.next", textComponent)
					.withStyle(ChatFormatting.AQUA));
			else
				tooltip.add(Components.literal("-> ").append(textComponent)
					.withStyle(ChatFormatting.DARK_AQUA));
		}

	}

	public Ingredient getIngredient() {
		return ingredient;
	}

	public List<SequencedRecipe<?>> getSequence() {
		return sequence;
	}

	public ItemStack getTransitionalItem() {
		return transitionalItem.getStack();
	}

	public record SequencedAssembly(ResourceLocation id, int step, float progress) {
		public static final Codec<SequencedAssembly> CODEC = RecordCodecBuilder.create(i -> i.group(
				ResourceLocation.CODEC.fieldOf("id").forGetter(SequencedAssembly::id),
				Codec.INT.fieldOf("step").forGetter(SequencedAssembly::step),
				Codec.FLOAT.fieldOf("progress").forGetter(SequencedAssembly::progress)
		).apply(i, SequencedAssembly::new));

		public static final StreamCodec<ByteBuf, SequencedAssembly> STREAM_CODEC = StreamCodec.composite(
				ResourceLocation.STREAM_CODEC, SequencedAssembly::id,
				ByteBufCodecs.INT, SequencedAssembly::step,
				ByteBufCodecs.FLOAT, SequencedAssembly::progress,
				SequencedAssembly::new
		);
	}

}
