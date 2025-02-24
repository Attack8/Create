package com.simibubi.create;

import java.util.function.BiConsumer;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public enum AllKeys {

	TOOL_MENU("toolmenu", GLFW.GLFW_KEY_LEFT_ALT, "Focus Schematic Overlay"),
	ACTIVATE_TOOL(GLFW.GLFW_KEY_LEFT_CONTROL),
	TOOLBELT("toolbelt", GLFW.GLFW_KEY_LEFT_ALT, "Access Nearby Toolboxes"),
	ROTATE_MENU("rotate_menu", GLFW.GLFW_KEY_UNKNOWN, "Open Block Rotation Menu"),

	;

	private KeyMapping keybind;
	private final String description;
	private final String translation;
	private final int key;
	private final boolean modifiable;

	AllKeys(int defaultKey) {
		this("", defaultKey, "");
	}

	AllKeys(String description, int defaultKey, String translation) {
		this.description = Create.ID + ".keyinfo." + description;
		this.key = defaultKey;
		this.modifiable = !description.isEmpty();
		this.translation = translation;
	}

	public static void provideLang(BiConsumer<String, String> consumer) {
		for (AllKeys key : values())
			if (key.modifiable)
				consumer.accept(key.description, key.translation);
	}

	@SubscribeEvent
	public static void register(RegisterKeyMappingsEvent event) {
		for (AllKeys key : values()) {
			key.keybind = new KeyMapping(key.description, key.key, Create.NAME);
			if (!key.modifiable)
				continue;

			event.register(key.keybind);
		}
	}

	public KeyMapping getKeybind() {
		return keybind;
	}

	public boolean isPressed() {
		if (!modifiable)
			return isKeyDown(key);
		return keybind.isDown();
	}

	public String getBoundKey() {
		return keybind.getTranslatedKeyMessage()
			.getString()
			.toUpperCase();
	}

	public int getBoundCode() {
		return keybind.getKey()
			.getValue();
	}

	public static boolean isKeyDown(int key) {
		return InputConstants.isKeyDown(Minecraft.getInstance()
			.getWindow()
			.getWindow(), key);
	}

	public static boolean isMouseButtonDown(int button) {
		return GLFW.glfwGetMouseButton(Minecraft.getInstance()
			.getWindow()
			.getWindow(), button) == 1;
	}

	public static boolean ctrlDown() {
		return Screen.hasControlDown();
	}

	public static boolean shiftDown() {
		return Screen.hasShiftDown();
	}

	public static boolean altDown() {
		return Screen.hasAltDown();
	}

}
