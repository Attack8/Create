package com.simibubi.create.api.contraption.storage.fluid.registrate;

import com.simibubi.create.AllRegistries;
import com.simibubi.create.api.contraption.storage.MountedStorageTypeRegistry;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class MountedFluidStorageTypeBuilder<T extends MountedFluidStorageType<?>, P> extends AbstractBuilder<MountedFluidStorageType<?>, T, P, MountedFluidStorageTypeBuilder<T, P>> {
	private final T type;

	public MountedFluidStorageTypeBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, T type) {
		super(owner, parent, name, callback, AllRegistries.Keys.MOUNTED_FLUID_STORAGE_TYPES);
		this.type = type;
	}

	public MountedFluidStorageTypeBuilder<T, P> registerTo(Block block) {
		MountedStorageTypeRegistry.FLUID_LOOKUP.register(block, this.type);
		return this;
	}

	public MountedFluidStorageTypeBuilder<T, P> registerTo(TagKey<Block> tag) {
		MountedStorageTypeRegistry.FLUID_LOOKUP.registerTag(tag, this.type);
		return this;
	}

	@Override
	@NonnullType
	protected T createEntry() {
		return this.type;
	}
}
