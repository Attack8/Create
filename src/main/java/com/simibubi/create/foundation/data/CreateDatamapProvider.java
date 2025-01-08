package com.simibubi.create.foundation.data;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import com.simibubi.create.foundation.block.CopperRegistries;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.Oxidizable;
import net.neoforged.neoforge.registries.datamaps.builtin.Waxable;

public class CreateDatamapProvider extends DataMapProvider {
	public CreateDatamapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(packOutput, lookupProvider);
	}

	@Override
	protected void gather() {
		final Builder<Oxidizable, Block> oxidizables = builder(NeoForgeDataMaps.OXIDIZABLES);
		CopperRegistries.getWeatheringView().forEach((now, after) -> add(oxidizables, now, new Oxidizable(after.get())));

		final Builder<Waxable, Block> waxables = builder(NeoForgeDataMaps.WAXABLES);
		CopperRegistries.getWaxableView().forEach((now, after) -> add(waxables, now, new Waxable(after.get())));
	}

	public static <T> void add(Builder<T, Block> b, Supplier<Block> now, T after) {
		b.add(now.get().builtInRegistryHolder(), after, false);
	}
}
