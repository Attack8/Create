package com.simibubi.create.api.contraption.storage.item;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.simibubi.create.api.contraption.storage.MountedStorageTypeRegistries;

import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public abstract class MountedItemStorageType<T extends MountedItemStorage> {
	public static final Codec<MountedItemStorageType<?>> CODEC = ExtraCodecs.lazyInitializedCodec(
		() -> MountedStorageTypeRegistries.getItemsRegistry().getCodec()
	);

	public final Codec<? extends T> codec;

	protected MountedItemStorageType(Codec<? extends T> codec) {
		this.codec = codec;
	}

	@Nullable
	public abstract T mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be);
}
