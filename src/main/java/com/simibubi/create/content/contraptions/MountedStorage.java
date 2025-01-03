package com.simibubi.create.content.contraptions;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.content.equipment.toolbox.ToolboxInventory;
import com.simibubi.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import com.simibubi.create.content.logistics.crate.BottomlessItemHandler;
import com.simibubi.create.content.logistics.vault.ItemVaultBlockEntity;
import com.simibubi.create.content.processing.recipe.ProcessingInventory;

import net.createmod.catnip.utility.NBTHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;

public class MountedStorage {

	private static final ItemStackHandler dummyHandler = new ItemStackHandler();

	ItemStackHandler handler;
	boolean noFuel;
	boolean valid;

	private BlockEntity blockEntity;

	public static boolean canUseAsStorage(BlockEntity be) {
		if (be == null)
			return false;
		if (be instanceof MechanicalCrafterBlockEntity)
			return false;
		if (AllBlockEntityTypes.CREATIVE_CRATE.is(be))
			return true;
		if (be instanceof ShulkerBoxBlockEntity)
			return true;
		if (be instanceof ChestBlockEntity)
			return true;
		if (be instanceof BarrelBlockEntity)
			return true;
		if (be instanceof ItemVaultBlockEntity)
			return true;

		try {
			IItemHandler capability = be.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, be.getBlockPos(), null);
			if (capability instanceof ItemStackHandler)
				return !(capability instanceof ProcessingInventory);
			return canUseModdedInventory(be, capability);
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean canUseModdedInventory(BlockEntity be, IItemHandler handler) {
		if (!(handler instanceof IItemHandlerModifiable))
			return false;
		BlockState blockState = be.getBlockState();
		if (AllBlockTags.CONTRAPTION_INVENTORY_DENY.matches(blockState))
			return false;

		// There doesn't appear to be much of a standard for tagging chests/barrels
		String blockId = BuiltInRegistries.BLOCK.getKey(blockState.getBlock())
			.getPath();
		if (blockId.contains("ender"))
			return false;
		return blockId.endsWith("_chest") || blockId.endsWith("_barrel");
	}

	public MountedStorage(BlockEntity be) {
		this.blockEntity = be;
		handler = dummyHandler;
		noFuel = be instanceof ItemVaultBlockEntity;
	}

	public void removeStorageFromWorld() {
		Level level = blockEntity.getLevel();

		valid = false;
		if (blockEntity == null)
			return;

		RegistryAccess registryAccess = level.registryAccess();

		if (blockEntity instanceof ChestBlockEntity) {
			CompoundTag tag = blockEntity.saveWithFullMetadata(registryAccess);
			if (tag.contains("LootTable", 8))
				return;

			handler = new ItemStackHandler(((ChestBlockEntity) blockEntity).getContainerSize());
			NonNullList<ItemStack> items = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
			ContainerHelper.loadAllItems(tag, items, registryAccess);
			for (int i = 0; i < items.size(); i++)
				handler.setStackInSlot(i, items.get(i));
			valid = true;
			return;
		}

		IItemHandler beHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, blockEntity.getBlockPos(), null, blockEntity, null);
		if (beHandler == null || beHandler == dummyHandler)
			return;

		// multiblock vaults need to provide individual invs
		if (blockEntity instanceof ItemVaultBlockEntity vbe) {
			handler = vbe.getInventoryOfBlock();
			valid = true;
			return;
		}

		// be uses ItemStackHandler
		if (beHandler instanceof ItemStackHandler) {
			handler = (ItemStackHandler) beHandler;
			valid = true;
			return;
		}

		// serialization not accessible -> fill into a serializable handler
		if (beHandler instanceof IItemHandlerModifiable) {
			IItemHandlerModifiable inv = (IItemHandlerModifiable) beHandler;
			handler = new ItemStackHandler(beHandler.getSlots());
			for (int slot = 0; slot < handler.getSlots(); slot++) {
				handler.setStackInSlot(slot, inv.getStackInSlot(slot));
				inv.setStackInSlot(slot, ItemStack.EMPTY);
			}
			valid = true;
			return;
		}

	}

	public void addStorageToWorld(BlockEntity be) {
		// FIXME: More dynamic mounted storage in .4
		if (handler instanceof BottomlessItemHandler)
			return;

		if (be instanceof ChestBlockEntity) {
			RegistryAccess registryAccess = be.getLevel().registryAccess();

			CompoundTag tag = be.saveWithFullMetadata(registryAccess);
			tag.remove("Items");
			NonNullList<ItemStack> items = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
			for (int i = 0; i < items.size(); i++)
				items.set(i, handler.getStackInSlot(i));
			ContainerHelper.saveAllItems(tag, items, registryAccess);
			be.loadWithComponents(tag, registryAccess);
			return;
		}

		if (be instanceof ItemVaultBlockEntity) {
			((ItemVaultBlockEntity) be).applyInventoryToBlock(handler);
			return;
		}

		IItemHandler capability = be.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, be.getBlockPos(), null);
		if (!(capability instanceof IItemHandlerModifiable inv))
			return;

		for (int slot = 0; slot < Math.min(inv.getSlots(), handler.getSlots()); slot++)
			inv.setStackInSlot(slot, handler.getStackInSlot(slot));
	}

	public IItemHandlerModifiable getItemHandler() {
		return handler;
	}

	public CompoundTag serialize(HolderLookup.Provider registries) {
		if (!valid)
			return null;

		CompoundTag tag = handler.serializeNBT(registries);
		if (noFuel)
			NBTHelper.putMarker(tag, "NoFuel");
		if (handler instanceof ToolboxInventory)
			NBTHelper.putMarker(tag, "Toolbox");
		if (!(handler instanceof BottomlessItemHandler))
			return tag;

		NBTHelper.putMarker(tag, "Bottomless");
		tag.put("ProvidedStack", handler.getStackInSlot(0).saveOptional(registries));
		return tag;
	}

	public static MountedStorage deserialize(CompoundTag nbt, HolderLookup.Provider registries) {
		MountedStorage storage = new MountedStorage(null);
		storage.handler = new ItemStackHandler();
		if (nbt == null)
			return storage;
		if (nbt.contains("Toolbox"))
			storage.handler = new ToolboxInventory(null);

		storage.valid = true;
		storage.noFuel = nbt.contains("NoFuel");

		if (nbt.contains("Bottomless")) {
			ItemStack providedStack = ItemStack.parseOptional(registries, nbt.getCompound("ProvidedStack"));
			storage.handler = new BottomlessItemHandler(() -> providedStack);
			return storage;
		}

		storage.handler.deserializeNBT(registries, nbt);
		return storage;
	}

	public boolean isValid() {
		return valid;
	}

	public boolean canUseForFuel() {
		return !noFuel;
	}

}
