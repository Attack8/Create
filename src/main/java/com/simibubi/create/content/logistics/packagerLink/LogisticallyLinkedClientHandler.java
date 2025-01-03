package com.simibubi.create.content.logistics.packagerLink;

import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

import net.createmod.catnip.CatnipClient;
import net.createmod.catnip.utility.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LogisticallyLinkedClientHandler {

	public static void tick() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null)
			return;
		ItemStack mainHandItem = player.getMainHandItem();
		if (!(mainHandItem.getItem() instanceof LogisticallyLinkedBlockItem)
			|| !LogisticallyLinkedBlockItem.isTuned(mainHandItem))
			return;

		CompoundTag tag = mainHandItem.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();
		if (!tag.hasUUID("Freq"))
			return;

		UUID uuid = tag.getUUID("Freq");
		for (LogisticallyLinkedBehaviour behaviour : LogisticallyLinkedBehaviour.getAllPresent(uuid, false, true)) {
			SmartBlockEntity be = behaviour.blockEntity;
			VoxelShape shape = be.getBlockState()
				.getShape(player.level(), be.getBlockPos());
			if (shape.isEmpty())
				continue;
			if (!player.blockPosition()
				.closerThan(be.getBlockPos(), 64))
				continue;
			for (int i = 0; i < shape.toAabbs()
				.size(); i++) {
				AABB aabb = shape.toAabbs()
					.get(i);
				CatnipClient.OUTLINER.showAABB(Pair.of(behaviour, i), aabb.inflate(-1 / 128f)
					.move(be.getBlockPos()), 2)
					.lineWidth(1 / 32f)
					.disableLineNormals()
					.colored(AnimationTickHolder.getTicks() % 16 < 8 ? 0x708DAD : 0x90ADCD);
			}

		}
	}

}
