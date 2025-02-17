package com.simibubi.create.api.contraption.storage.fluid;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllRegistries;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public abstract class MountedFluidStorageType<T extends MountedFluidStorage> {
	public static final Codec<MountedFluidStorageType<?>> CODEC = Codec.lazyInitialized(
		AllRegistries.MOUNTED_FLUID_STORAGE_TYPE::byNameCodec
	);

	public final MapCodec<? extends T> codec;

	protected MountedFluidStorageType(MapCodec<? extends T> codec) {
		this.codec = codec;
	}

	@Nullable
	public abstract T mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be);
}
