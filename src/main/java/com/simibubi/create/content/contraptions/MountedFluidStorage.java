package com.simibubi.create.content.contraptions;

import com.simibubi.create.content.contraptions.sync.ContraptionFluidPacket;
import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity.CreativeSmartFluidTank;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.utility.NBTHelper;
import net.createmod.catnip.utility.animation.LerpedFloat;
import net.createmod.catnip.utility.animation.LerpedFloat.Chaser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class MountedFluidStorage {

	SmartFluidTank tank;
	private boolean valid;
	private BlockEntity blockEntity;

	private int packetCooldown = 0;
	private boolean sendPacket = false;

	public static boolean canUseAsStorage(BlockEntity be) {
		if (be instanceof FluidTankBlockEntity)
			return ((FluidTankBlockEntity) be).isController();
		return false;
	}

	public MountedFluidStorage(BlockEntity be) {
		assignBlockEntity(be);
	}

	public void assignBlockEntity(BlockEntity be) {
		this.blockEntity = be;
		tank = createMountedTank(be);
	}

	private SmartFluidTank createMountedTank(BlockEntity be) {
		if (be instanceof CreativeFluidTankBlockEntity)
			return new CreativeSmartFluidTank(
				((FluidTankBlockEntity) be).getTotalTankSize() * FluidTankBlockEntity.getCapacityMultiplier(), $ -> {
				});
		if (be instanceof FluidTankBlockEntity)
			return new SmartFluidTank(
				((FluidTankBlockEntity) be).getTotalTankSize() * FluidTankBlockEntity.getCapacityMultiplier(),
				this::onFluidStackChanged);
		return null;
	}

	public void tick(Entity entity, BlockPos pos, boolean isRemote) {
		if (!isRemote) {
			if (packetCooldown > 0)
				packetCooldown--;
			else if (sendPacket) {
				sendPacket = false;
				CatnipServices.NETWORK.sendToClientsTrackingEntity(entity,
					new ContraptionFluidPacket(entity.getId(), pos, tank.getFluid()));
				packetCooldown = 8;
			}
			return;
		}

		if (!(blockEntity instanceof FluidTankBlockEntity))
			return;
		FluidTankBlockEntity tank = (FluidTankBlockEntity) blockEntity;
		tank.getFluidLevel()
			.tickChaser();
	}

	public void updateFluid(FluidStack fluid) {
		tank.setFluid(fluid);
		if (!(blockEntity instanceof FluidTankBlockEntity))
			return;
		float fillState = tank.getFluidAmount() / (float) tank.getCapacity();
		FluidTankBlockEntity tank = (FluidTankBlockEntity) blockEntity;
		if (tank.getFluidLevel() == null)
			tank.setFluidLevel(LerpedFloat.linear()
				.startWithValue(fillState));
		tank.getFluidLevel()
			.chase(fillState, 0.5, Chaser.EXP);
		IFluidTank tankInventory = tank.getTankInventory();
		if (tankInventory instanceof SmartFluidTank)
			((SmartFluidTank) tankInventory).setFluid(fluid);
	}

	public void removeStorageFromWorld() {
		valid = false;
		if (blockEntity == null)
			return;

		IFluidHandler teHandler = blockEntity.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, blockEntity.getBlockPos(), null);
		if (!(teHandler instanceof SmartFluidTank smartTank))
			return;
		tank.setFluid(smartTank.getFluid());
		sendPacket = false;
		valid = true;
	}

	private void onFluidStackChanged(FluidStack fs) {
		sendPacket = true;
	}

	public void addStorageToWorld(BlockEntity be) {
		if (tank instanceof CreativeSmartFluidTank)
			return;

		IFluidHandler teHandler = be.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, be.getBlockPos(), null);
		if (!(teHandler instanceof SmartFluidTank inv))
			return;

		inv.setFluid(tank.getFluid()
			.copy());
	}

	public IFluidHandler getFluidHandler() {
		return tank;
	}

	public CompoundTag serialize(HolderLookup.Provider registries) {
		if (!valid)
			return null;
		CompoundTag tag = tank.writeToNBT(registries, new CompoundTag());
		tag.putInt("Capacity", tank.getCapacity());

		if (tank instanceof CreativeSmartFluidTank) {
			NBTHelper.putMarker(tag, "Bottomless");
			tag.put("ProvidedStack", tank.getFluid()
				.saveOptional(registries));
		}
		return tag;
	}

	public static MountedFluidStorage deserialize(CompoundTag nbt, HolderLookup.Provider registries) {
		MountedFluidStorage storage = new MountedFluidStorage(null);
		if (nbt == null)
			return storage;

		int capacity = nbt.getInt("Capacity");
		storage.tank = new SmartFluidTank(capacity, storage::onFluidStackChanged);
		storage.valid = true;

		if (nbt.contains("Bottomless")) {
			FluidStack providedStack = FluidStack.parseOptional(registries, nbt.getCompound("ProvidedStack"));
			CreativeSmartFluidTank creativeSmartFluidTank = new CreativeSmartFluidTank(capacity, $ -> {
			});
			creativeSmartFluidTank.setContainedFluid(providedStack);
			storage.tank = creativeSmartFluidTank;
			return storage;
		}

		storage.tank.readFromNBT(registries, nbt);
		return storage;
	}

	public boolean isValid() {
		return valid;
	}

}
