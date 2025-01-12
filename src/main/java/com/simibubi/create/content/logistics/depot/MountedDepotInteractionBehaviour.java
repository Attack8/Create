package com.simibubi.create.content.logistics.depot;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.MountedStorage;
import com.simibubi.create.content.contraptions.MountedStorageManager;
import com.simibubi.create.content.contraptions.behaviour.MovingInteractionBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.items.IItemHandlerModifiable;

public class MountedDepotInteractionBehaviour extends MovingInteractionBehaviour {

	@Override
	public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos,
		AbstractContraptionEntity contraptionEntity) {
		ItemStack itemInHand = player.getItemInHand(activeHand);
		if (activeHand == InteractionHand.OFF_HAND)
			return false;
		if (player.level().isClientSide)
			return true;

		MountedStorageManager storageManager = contraptionEntity.getContraption()
			.getStorageManager();
		if (storageManager == null)
			return false;

		MountedStorage mountedStorage = storageManager.getMountedItemStorage()
			.get(localPos);
		if (mountedStorage == null)
			return false;

		IItemHandlerModifiable itemHandler = mountedStorage.getItemHandler();
		ItemStack itemOnDepot = itemHandler.getStackInSlot(0);

		if (itemOnDepot.isEmpty() && itemInHand.isEmpty())
			return true;

		itemHandler.setStackInSlot(0, itemInHand.copy());
		player.setItemInHand(activeHand, itemOnDepot.copy());
		AllSoundEvents.DEPOT_PLOP.playOnServer(player.level(),
			BlockPos.containing(contraptionEntity.toGlobalVector(Vec3.atCenterOf(localPos), 0)));

		return true;
	}

}
