package com.simibubi.create.foundation.blockEntity.behaviour.scrollValue;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBox;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBox.IconValueBox;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBox.TextValueBox;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ScrollValueRenderer {

	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		HitResult target = mc.hitResult;
		if (target == null || !(target instanceof BlockHitResult result))
			return;

		ClientLevel world = mc.level;
		BlockPos pos = result.getBlockPos();
		Direction face = result.getDirection();
		boolean highlightFound = false;

		if (!(world.getBlockEntity(pos) instanceof SmartBlockEntity sbe))
			return;

		for (BlockEntityBehaviour blockEntityBehaviour : sbe.getAllBehaviours()) {
			if (!(blockEntityBehaviour instanceof ScrollValueBehaviour behaviour))
				continue;

			if (!behaviour.isActive()) {
				Outliner.getInstance().remove(behaviour);
				continue;
			}

			ItemStack mainhandItem = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
			boolean clipboard = behaviour.bypassesInput(mainhandItem);
			if (behaviour.needsWrench && !AllItems.WRENCH.isIn(mainhandItem) && !clipboard)
				continue;
			boolean highlight = behaviour.testHit(target.getLocation()) && !clipboard && !highlightFound;

			if (behaviour instanceof BulkScrollValueBehaviour bulkScrolling && AllKeys.ctrlDown()) {
				for (SmartBlockEntity smartBlockEntity : bulkScrolling.getBulk()) {
					ScrollValueBehaviour other = smartBlockEntity.getBehaviour(ScrollValueBehaviour.TYPE);
					if (other != null)
						addBox(world, smartBlockEntity.getBlockPos(), face, other, highlight);
				}
			} else
				addBox(world, pos, face, behaviour, highlight);

			if (!highlight)
				continue;

			highlightFound = true;
			List<MutableComponent> tip = new ArrayList<>();
			tip.add(behaviour.label.copy());
			tip.add(CreateLang.translateDirect("gui.value_settings.hold_to_edit"));
			CreateClient.VALUE_SETTINGS_HANDLER.showHoverTip(tip);
		}
	}

	protected static void addBox(ClientLevel world, BlockPos pos, Direction face, ScrollValueBehaviour behaviour,
								 boolean highlight) {
		AABB bb = new AABB(Vec3.ZERO, Vec3.ZERO).inflate(.5f)
			.contract(0, 0, -.5f)
			.move(0, 0, -.125f);
		Component label = behaviour.label;
		ValueBox box;

		if (behaviour instanceof ScrollOptionBehaviour) {
			box = new IconValueBox(label, ((ScrollOptionBehaviour<?>) behaviour).getIconForSelected(), bb, pos);
		} else {
			box = new TextValueBox(label, bb, pos, Component.literal(behaviour.formatValue()));
		}

		box.passive(!highlight)
			.wideOutline();

		Outliner.getInstance().showOutline(behaviour, box.transform(behaviour.slotPositioning))
			.highlightFace(face);
	}

}
