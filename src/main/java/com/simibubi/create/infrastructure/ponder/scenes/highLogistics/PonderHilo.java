package com.simibubi.create.infrastructure.ponder.scenes.highLogistics;

import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class PonderHilo {

	public static void packagerCreate(CreateSceneBuilder scene, BlockPos pos, ItemStack box) {
		scene.world()
			.modifyBlockEntity(pos, PackagerBlockEntity.class, be -> {
				be.animationTicks = PackagerBlockEntity.CYCLE;
				be.animationInward = false;
				be.heldBox = box;
			});
	}

	public static void packagerUnpack(CreateSceneBuilder scene, BlockPos pos, ItemStack box) {
		scene.world()
			.modifyBlockEntity(pos, PackagerBlockEntity.class, be -> {
				be.animationTicks = PackagerBlockEntity.CYCLE;
				be.animationInward = true;
				be.previouslyUnwrapped = box;
			});
	}

	public static void packagerClear(CreateSceneBuilder scene, BlockPos pos) {
		scene.world()
			.modifyBlockEntity(pos, PackagerBlockEntity.class, be -> be.heldBox = ItemStack.EMPTY);
	}

	public static ElementLink<EntityElement> packageHopsOffBelt(CreateSceneBuilder scene, BlockPos beltPos,
		Direction side) {
		scene.world()
			.removeItemsFromBelt(beltPos);
		return scene.world()
			.createEntity(l -> {
				PackageEntity packageEntity = new PackageEntity(l, beltPos.getX() + 0.5 + side.getStepX() * 0.25,
					beltPos.getY() + 0.875, beltPos.getZ() + 0.5 + side.getStepZ() * 0.25);
				packageEntity.setDeltaMovement(new Vec3(side.getStepX(), 0.5f, side.getStepZ()).scale(0.25f));
				packageEntity.box = PackageStyles.getDefaultBox();
				return packageEntity;
			});
	}

}
