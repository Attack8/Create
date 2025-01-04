package com.simibubi.create.content.logistics.vault;

import com.mojang.serialization.Codec;
import com.simibubi.create.AllMountedStorageTypes;

import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.contraption.storage.item.WrapperMountedItemStorage;

import com.simibubi.create.foundation.utility.CreateCodecs;

import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ItemVaultMountedStorage extends WrapperMountedItemStorage<ItemStackHandler> {
	public static final Codec<ItemVaultMountedStorage> CODEC = CreateCodecs.ITEM_STACK_HANDLER.xmap(
		ItemVaultMountedStorage::new, storage -> storage.wrapped
	);

	protected ItemVaultMountedStorage(MountedItemStorageType<?> type, ItemStackHandler handler) {
		super(type, handler);
	}

	protected ItemVaultMountedStorage(ItemStackHandler handler) {
		this(AllMountedStorageTypes.VAULT.get(), handler);
	}

	@Override
	public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		if (be instanceof ItemVaultBlockEntity vault) {
			vault.applyInventoryToBlock(this.wrapped);
		}
	}

	@Override
	public boolean providesFuel() {
		return false;
	}

	public static ItemVaultMountedStorage fromVault(ItemVaultBlockEntity vault) {
		// Vault inventories have a world-affecting onContentsChanged, copy to a safe one
		return new ItemVaultMountedStorage(copyToItemStackHandler(vault.getInventoryOfBlock()));
	}
}
