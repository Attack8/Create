package com.simibubi.create.content.logistics.stockTicker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.logistics.BigItemStack;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record PackageOrder(List<BigItemStack> stacks) {
	public static final Codec<PackageOrder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.list(BigItemStack.CODEC).fieldOf("entries").forGetter(PackageOrder::getNonEmptyStacks)
	).apply(instance, PackageOrder::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, PackageOrder> STREAM_CODEC = StreamCodec.composite(
		CatnipStreamCodecBuilders.list(BigItemStack.STREAM_CODEC), PackageOrder::getNonEmptyStacks,
	    PackageOrder::new
	);

	public static PackageOrder empty() {
		return new PackageOrder(Collections.emptyList());
	}

	public boolean isEmpty() {
		return stacks.isEmpty();
	}

	private List<BigItemStack> getNonEmptyStacks() {
		if (this.stacks.isEmpty())
			return this.stacks;

		List<BigItemStack> filtered = new ArrayList<>();
		for (BigItemStack stack : this.stacks) {
			if (!stack.stack.isEmpty()) {
				filtered.add(stack);
			}
		}
		return filtered;
	}
}
