package com.simibubi.create.content.logistics.vault;

import java.util.List;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.foundation.ICapabilityProvider;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.VersionedInventoryWrapper;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

public class ItemVaultBlockEntity extends SmartBlockEntity implements IMultiBlockEntityContainer.Inventory {

	protected ICapabilityProvider<IItemHandler> itemCapability = null;

	protected ItemStackHandler inventory;
	protected BlockPos controller;
	protected BlockPos lastKnownPos;
	protected boolean updateConnectivity;
	protected int radius;
	protected int length;
	protected Axis axis;

	public ItemVaultBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);

		inventory = new ItemStackHandler(AllConfigs.server().logistics.vaultCapacity.get()) {
			@Override
			protected void onContentsChanged(int slot) {
				super.onContentsChanged(slot);
				updateComparators();
				level.blockEntityChanged(worldPosition);
			}
		};

		radius = 1;
		length = 1;
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(
				Capabilities.ItemHandler.BLOCK,
				AllBlockEntityTypes.ITEM_VAULT.get(),
				(be, context) -> {
					be.initCapability();
					if (be.itemCapability == null)
						return null;
					return be.itemCapability.getCapability();
				}
		);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

	protected void updateConnectivity() {
		updateConnectivity = false;
		if (level.isClientSide())
			return;
		if (!isController())
			return;
		ConnectivityHandler.formMulti(this);
	}

	protected void updateComparators() {
		ItemVaultBlockEntity controllerBE = getControllerBE();
		if (controllerBE == null)
			return;

		level.blockEntityChanged(controllerBE.worldPosition);

		BlockPos pos = controllerBE.getBlockPos();
		for (int y = 0; y < controllerBE.radius; y++) {
			for (int z = 0; z < (controllerBE.axis == Axis.X ? controllerBE.radius : controllerBE.length); z++) {
				for (int x = 0; x < (controllerBE.axis == Axis.Z ? controllerBE.radius : controllerBE.length); x++) {
					level.updateNeighbourForOutputSignal(pos.offset(x, y, z), getBlockState().getBlock());
				}
			}
		}
	}

	@Override
	public void tick() {
		super.tick();

		if (lastKnownPos == null)
			lastKnownPos = getBlockPos();
		else if (!lastKnownPos.equals(worldPosition) && worldPosition != null) {
			onPositionChanged();
			return;
		}

		if (updateConnectivity)
			updateConnectivity();
	}

	@Override
	public BlockPos getLastKnownPos() {
		return lastKnownPos;
	}

	@Override
	public boolean isController() {
		return controller == null || worldPosition.getX() == controller.getX()
			&& worldPosition.getY() == controller.getY() && worldPosition.getZ() == controller.getZ();
	}

	private void onPositionChanged() {
		removeController(true);
		lastKnownPos = worldPosition;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ItemVaultBlockEntity getControllerBE() {
		if (isController())
			return this;
		BlockEntity blockEntity = level.getBlockEntity(controller);
		if (blockEntity instanceof ItemVaultBlockEntity)
			return (ItemVaultBlockEntity) blockEntity;
		return null;
	}

	public void removeController(boolean keepContents) {
		if (level.isClientSide())
			return;
		updateConnectivity = true;
		controller = null;
		radius = 1;
		length = 1;

		BlockState state = getBlockState();
		if (ItemVaultBlock.isVault(state)) {
			state = state.setValue(ItemVaultBlock.LARGE, false);
			getLevel().setBlock(worldPosition, state, 22);
		}

		itemCapability = null;
		invalidateCapabilities();
		setChanged();
		sendData();
	}

	@Override
	public void setController(BlockPos controller) {
		if (level.isClientSide && !isVirtual())
			return;
		if (controller.equals(this.controller))
			return;
		this.controller = controller;
		itemCapability = null;
		invalidateCapabilities();
		setChanged();
		sendData();
	}

	@Override
	public BlockPos getController() {
		return isController() ? worldPosition : controller;
	}

	@Override
	protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(compound, registries, clientPacket);

		BlockPos controllerBefore = controller;
		int prevSize = radius;
		int prevLength = length;

		updateConnectivity = compound.contains("Uninitialized");
		
		lastKnownPos = null;
		if (compound.contains("LastKnownPos"))
			lastKnownPos = NBTHelper.readBlockPos(compound, "LastKnownPos");
		
		controller = null;
		if (compound.contains("Controller"))
			controller = NBTHelper.readBlockPos(compound, "Controller");

		if (isController()) {
			radius = compound.getInt("Size");
			length = compound.getInt("Length");
		}

		if (!clientPacket) {
			inventory.deserializeNBT(registries, compound.getCompound("Inventory"));
			return;
		}

		boolean changeOfController =
			controllerBefore == null ? controller != null : !controllerBefore.equals(controller);
		if (hasLevel() && (changeOfController || prevSize != radius || prevLength != length))
			level.setBlocksDirty(getBlockPos(), Blocks.AIR.defaultBlockState(), getBlockState());
	}

	@Override
	protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		if (updateConnectivity)
			compound.putBoolean("Uninitialized", true);

		if (lastKnownPos != null)
			compound.put("LastKnownPos", NbtUtils.writeBlockPos(lastKnownPos));
		if (!isController())
			compound.put("Controller", NbtUtils.writeBlockPos(controller));
		if (isController()) {
			compound.putInt("Size", radius);
			compound.putInt("Length", length);
		}

		super.write(compound, registries, clientPacket);

		if (!clientPacket) {
			compound.putString("StorageType", "CombinedInv");
			compound.put("Inventory", inventory.serializeNBT(registries));
		}
	}

	public ItemStackHandler getInventoryOfBlock() {
		return inventory;
	}

	public void applyInventoryToBlock(ItemStackHandler handler) {
		for (int i = 0; i < inventory.getSlots(); i++)
			inventory.setStackInSlot(i, i < handler.getSlots() ? handler.getStackInSlot(i) : ItemStack.EMPTY);
	}

	private void initCapability() {
		if (itemCapability != null && itemCapability.getCapability() != null)
			return;
		if (!isController()) {
			ItemVaultBlockEntity controllerBE = getControllerBE();
			if (controllerBE == null)
				return;
			controllerBE.initCapability();
			itemCapability = ICapabilityProvider.of(() -> {
				if (controllerBE.isRemoved())
					return null;
				if (controllerBE.itemCapability == null)
					return null;
				return controllerBE.itemCapability.getCapability();
			});
			return;
		}

		boolean alongZ = ItemVaultBlock.getVaultBlockAxis(getBlockState()) == Axis.Z;
		IItemHandlerModifiable[] invs = new IItemHandlerModifiable[length * radius * radius];
		for (int yOffset = 0; yOffset < length; yOffset++) {
			for (int xOffset = 0; xOffset < radius; xOffset++) {
				for (int zOffset = 0; zOffset < radius; zOffset++) {
					BlockPos vaultPos = alongZ ? worldPosition.offset(xOffset, zOffset, yOffset)
						: worldPosition.offset(yOffset, xOffset, zOffset);
					ItemVaultBlockEntity vaultAt =
						ConnectivityHandler.partAt(AllBlockEntityTypes.ITEM_VAULT.get(), level, vaultPos);
					invs[yOffset * radius * radius + xOffset * radius + zOffset] =
						vaultAt != null ? vaultAt.inventory : new ItemStackHandler();
				}
			}
		}

		itemCapability = ICapabilityProvider.of(new VersionedInventoryWrapper(new CombinedInvWrapper(invs)));
	}

	public static int getMaxLength(int radius) {
		return radius * 3;
	}

	@Override
	public void preventConnectivityUpdate() { updateConnectivity = false; }

	@Override
	public void notifyMultiUpdated() {
		BlockState state = this.getBlockState();
		if (ItemVaultBlock.isVault(state)) { // safety
			level.setBlock(getBlockPos(), state.setValue(ItemVaultBlock.LARGE, radius > 2), 6);
		}
		itemCapability = null;
		invalidateCapabilities();
		setChanged();
	}

	@Override
	public Direction.Axis getMainConnectionAxis() { return getMainAxisOf(this); }

	@Override
	public int getMaxLength(Direction.Axis longAxis, int width) {
		if (longAxis == Direction.Axis.Y) return getMaxWidth();
		return getMaxLength(width);
	}

	@Override
	public int getMaxWidth() {
		return 3;
	}

	@Override
	public int getHeight() { return length; }

	@Override
	public int getWidth() { return radius; }

	@Override
	public void setHeight(int height) { this.length = height; }

	@Override
	public void setWidth(int width) { this.radius = width; }

	@Override
	public boolean hasInventory() { return true; }
}
