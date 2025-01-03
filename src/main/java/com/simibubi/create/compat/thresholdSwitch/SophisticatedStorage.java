package com.simibubi.create.compat.thresholdSwitch;

import com.simibubi.create.compat.Mods;

import net.createmod.catnip.utility.RegisteredObjectsHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;

public class SophisticatedStorage implements ThresholdSwitchCompat {

	@Override
	public boolean isFromThisMod(BlockEntity be) {
		if (be == null)
			return false;

		String namespace = RegisteredObjectsHelper.getKeyOrThrow(be.getType())
			.getNamespace();

		return
			Mods.SOPHISTICATEDSTORAGE.id().equals(namespace)
			|| Mods.SOPHISTICATEDBACKPACKS.id().equals(namespace);
	}

	@Override
	public long getSpaceInSlot(IItemHandler inv, int slot) {
		return ((long) inv.getSlotLimit(slot) * inv.getStackInSlot(slot).getOrDefault(DataComponents.MAX_STACK_SIZE, 64)) / 64;
	}

}
