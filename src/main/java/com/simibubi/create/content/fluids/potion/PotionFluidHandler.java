package com.simibubi.create.content.fluids.potion;

import java.util.List;

import com.google.common.collect.Lists;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.fluids.potion.PotionFluid.BottleType;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.fluid.FluidIngredient;

import net.createmod.catnip.data.Pair;
import net.createmod.catnip.lang.Components;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;

public class PotionFluidHandler {

	public static boolean isPotionItem(ItemStack stack) {
		return stack.getItem() instanceof PotionItem && !(stack.getCraftingRemainingItem()
			.getItem() instanceof BucketItem);
	}

	public static Pair<FluidStack, ItemStack> emptyPotion(ItemStack stack, boolean simulate) {
		FluidStack fluid = getFluidFromPotionItem(stack);
		if (!simulate)
			stack.shrink(1);
		return Pair.of(fluid, new ItemStack(Items.GLASS_BOTTLE));
	}

	public static FluidIngredient potionIngredient(Holder<Potion> potion, int amount) {
		return FluidIngredient.fromFluidStack(FluidHelper.copyStackWithAmount(PotionFluidHandler
			.getFluidFromPotionItem(PotionContents.createItemStack(Items.POTION, potion)), amount));
	}

	public static FluidStack getFluidFromPotionItem(ItemStack stack) {
		PotionContents potion = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
		BottleType bottleTypeFromItem = bottleTypeFromItem(stack.getItem());
		if (potion.is(Potions.WATER) && potion.customEffects().isEmpty() && bottleTypeFromItem == BottleType.REGULAR)
			return new FluidStack(Fluids.WATER, 250);
		FluidStack fluid = PotionFluid.withEffects(250, potion);
		fluid.set(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, bottleTypeFromItem);
		return fluid;
	}

	public static FluidStack getFluidFromPotion(PotionContents potionContents, BottleType bottleType, int amount) {
		if (potionContents.is(Potions.WATER) && bottleType == BottleType.REGULAR)
			return new FluidStack(Fluids.WATER, amount);
		FluidStack fluid = PotionFluid.of(amount, potionContents);
		fluid.set(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, bottleType);
		return fluid;
	}

	public static BottleType bottleTypeFromItem(Item item) {
		if (item == Items.LINGERING_POTION)
			return BottleType.LINGERING;
		if (item == Items.SPLASH_POTION)
			return BottleType.SPLASH;
		return BottleType.REGULAR;
	}

	public static ItemLike itemFromBottleType(BottleType type) {
		return switch (type) {
			case LINGERING -> Items.LINGERING_POTION;
			case SPLASH -> Items.SPLASH_POTION;
			default -> Items.POTION;
		};
	}

	public static int getRequiredAmountForFilledBottle(ItemStack stack, FluidStack availableFluid) {
		return 250;
	}

	public static ItemStack fillBottle(ItemStack stack, FluidStack availableFluid) {
		ItemStack potionStack = new ItemStack(itemFromBottleType(availableFluid.getOrDefault(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, BottleType.REGULAR)));
		potionStack.set(DataComponents.POTION_CONTENTS, availableFluid.get(DataComponents.POTION_CONTENTS));
		return potionStack;
	}

	// Modified version of PotionUtils#addPotionTooltip
	@OnlyIn(Dist.CLIENT)
	public static void addPotionTooltip(FluidStack fs, List<Component> tooltip, float factor) {
		List<MobEffectInstance> list = fs.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).customEffects();
		List<Pair<Holder<Attribute>, AttributeModifier>> list1 = Lists.newArrayList();
		if (list.isEmpty()) {
			tooltip.add((Components.translatable("effect.none")).withStyle(ChatFormatting.GRAY));
		} else {
			for (MobEffectInstance effectinstance : list) {
				MutableComponent textcomponent = Components.translatable(effectinstance.getDescriptionId());
				Holder<MobEffect> effect = effectinstance.getEffect();
				effect.value().createModifiers(effectinstance.getAmplifier(), (attributeHolder, attributeModifier) -> list1.add(Pair.of(attributeHolder, attributeModifier)));

				if (effectinstance.getAmplifier() > 0) {
					textcomponent.append(" ")
						.append(Components.translatable("potion.potency." + effectinstance.getAmplifier()).getString());
				}

				if (effectinstance.getDuration() > 20) {
					textcomponent.append(" (")
						.append(MobEffectUtil.formatDuration(effectinstance, factor, Minecraft.getInstance().level.tickRateManager().tickrate()))
						.append(")");
				}

				tooltip.add(textcomponent.withStyle(effect.value().getCategory()
					.getTooltipFormatting()));
			}
		}

		if (!list1.isEmpty()) {
			tooltip.add(Components.immutableEmpty());
			tooltip.add((Components.translatable("potion.whenDrank")).withStyle(ChatFormatting.DARK_PURPLE));

			for (Pair<Holder<Attribute>, AttributeModifier> pair : list1) {
				AttributeModifier attributemodifier2 = pair.getSecond();
				double d0 = attributemodifier2.amount();
				double d1;
				if (attributemodifier2.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_BASE
					&& attributemodifier2.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
					d1 = attributemodifier2.amount();
				} else {
					d1 = attributemodifier2.amount() * 100.0D;
				}

				if (d0 > 0.0D) {
					tooltip.add((Components.translatable(
						"attribute.modifier.plus." + attributemodifier2.operation().id(),
							ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d1),
						Components.translatable(pair.getFirst().value().getDescriptionId())))
							.withStyle(ChatFormatting.BLUE));
				} else if (d0 < 0.0D) {
					d1 = d1 * -1.0D;
					tooltip.add((Components.translatable(
						"attribute.modifier.take." + attributemodifier2.operation().id(),
						ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d1),
						Components.translatable(pair.getFirst().value().getDescriptionId())))
							.withStyle(ChatFormatting.RED));
				}
			}
		}

	}

}
