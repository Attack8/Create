package com.simibubi.create.api.contraption.storage.item;

import net.minecraftforge.items.IItemHandlerModifiable;

import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.item.ItemStack;

public abstract class WrapperMountedItemStorage<T extends IItemHandlerModifiable> extends MountedItemStorage {
	protected final T wrapped;

	protected WrapperMountedItemStorage(MountedItemStorageType<? extends MountedItemStorage> type, T wrapped) {
		super(type);
		this.wrapped = wrapped;
	}

	@Override
	public void setStackInSlot(int slot, @NotNull ItemStack stack) {
		this.wrapped.setStackInSlot(slot, stack);
	}

	@Override
	public int getSlots() {
		return this.wrapped.getSlots();
	}

	@Override
	@NotNull
	public ItemStack getStackInSlot(int slot) {
		return this.wrapped.getStackInSlot(slot);
	}

	@Override
	@NotNull
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		return this.wrapped.insertItem(slot, stack, simulate);
	}

	@Override
	@NotNull
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return this.wrapped.extractItem(slot, amount, simulate);
	}

	@Override
	public int getSlotLimit(int slot) {
		return this.wrapped.getSlotLimit(slot);
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		return this.wrapped.isItemValid(slot, stack);
	}

	public static ItemStackHandler copyToItemStackHandler(IItemHandlerModifiable handler) {
		ItemStackHandler copy = new ItemStackHandler(handler.getSlots());
		for (int i = 0; i < handler.getSlots(); i++) {
			copy.setStackInSlot(i, handler.getStackInSlot(i).copy());
		}
		return copy;
	}
}
