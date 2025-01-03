package com.simibubi.create.content.contraptions.actors.trainControls;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.createmod.catnip.platform.CatnipServices;
import com.simibubi.create.foundation.utility.ControlsUtil;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

public class ControlsHandler {

	public static Collection<Integer> currentlyPressed = new HashSet<>();

	public static int PACKET_RATE = 5;
	private static int packetCooldown;

	private static WeakReference<AbstractContraptionEntity> entityRef = new WeakReference<>(null);
	private static BlockPos controlsPos;

	public static void levelUnloaded(LevelAccessor level) {
		packetCooldown = 0;
		entityRef = new WeakReference<>(null);
		controlsPos = null;
		currentlyPressed.clear();
	}

	public static void startControlling(AbstractContraptionEntity entity, BlockPos controllerLocalPos) {
		entityRef = new WeakReference<>(entity);
		controlsPos = controllerLocalPos;

		Minecraft.getInstance().player.displayClientMessage(
			CreateLang.translateDirect("contraption.controls.start_controlling", entity.getContraptionName()), true);
	}

	public static void stopControlling() {
		ControlsUtil.getControls()
			.forEach(kb -> kb.setDown(ControlsUtil.isActuallyPressed(kb)));
		AbstractContraptionEntity abstractContraptionEntity = entityRef.get();

		if (!currentlyPressed.isEmpty() && abstractContraptionEntity != null)
			CatnipServices.NETWORK.sendToServer(new ControlsInputPacket(currentlyPressed, false,
				abstractContraptionEntity.getId(), controlsPos, false));

		packetCooldown = 0;
		entityRef = new WeakReference<>(null);
		controlsPos = null;
		currentlyPressed.clear();

		Minecraft.getInstance().player.displayClientMessage(CreateLang.translateDirect("contraption.controls.stop_controlling"),
			true);
	}

	public static void tick() {
		AbstractContraptionEntity entity = entityRef.get();
		if (entity == null)
			return;
		if (packetCooldown > 0)
			packetCooldown--;

		if (entity.isRemoved() || InputConstants.isKeyDown(Minecraft.getInstance()
			.getWindow()
			.getWindow(), GLFW.GLFW_KEY_ESCAPE)) {
			BlockPos pos = controlsPos;
			stopControlling();
			CatnipServices.NETWORK.sendToServer(new ControlsInputPacket(currentlyPressed, false, entity.getId(), pos, true));
			return;
		}

		List<KeyMapping> controls = ControlsUtil.getControls();
		Collection<Integer> pressedKeys = new HashSet<>();
		for (int i = 0; i < controls.size(); i++) {
			if (ControlsUtil.isActuallyPressed(controls.get(i)))
				pressedKeys.add(i);
		}

		Collection<Integer> newKeys = new HashSet<>(pressedKeys);
		Collection<Integer> releasedKeys = currentlyPressed;
		newKeys.removeAll(releasedKeys);
		releasedKeys.removeAll(pressedKeys);

		// Released Keys
		if (!releasedKeys.isEmpty()) {
			CatnipServices.NETWORK.sendToServer(new ControlsInputPacket(releasedKeys, false, entity.getId(), controlsPos, false));
//			AllSoundEvents.CONTROLLER_CLICK.playAt(player.level, player.blockPosition(), 1f, .5f, true);
		}

		// Newly Pressed Keys
		if (!newKeys.isEmpty()) {
			CatnipServices.NETWORK.sendToServer(new ControlsInputPacket(newKeys, true, entity.getId(), controlsPos, false));
			packetCooldown = PACKET_RATE;
//			AllSoundEvents.CONTROLLER_CLICK.playAt(player.level, player.blockPosition(), 1f, .75f, true);
		}

		// Keepalive Pressed Keys
		if (packetCooldown == 0) {
//			if (!pressedKeys.isEmpty()) {
				CatnipServices.NETWORK.sendToServer(new ControlsInputPacket(pressedKeys, true, entity.getId(), controlsPos, false));
				packetCooldown = PACKET_RATE;
//			}
		}

		currentlyPressed = pressedKeys;
		controls.forEach(kb -> kb.setDown(false));
	}

	@Nullable
	public static AbstractContraptionEntity getContraption() {
		return entityRef.get();
	}

	@Nullable
	public static BlockPos getControlsPos() {
		return controlsPos;
	}

}
