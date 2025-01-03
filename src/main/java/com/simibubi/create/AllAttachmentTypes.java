package com.simibubi.create;

import java.util.function.Supplier;

import com.simibubi.create.content.contraptions.minecart.capability.MinecartController;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class AllAttachmentTypes {
	private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Create.ID);

	public static final Supplier<AttachmentType<MinecartController>> MINECART_CONTROLLER = ATTACHMENT_TYPES.register(
			"minecart_controller", () -> AttachmentType.builder(MinecartController::empty).serialize(MinecartController.SERIALIZER).build()
	);

	public static void register(IEventBus modEventBus) {
		ATTACHMENT_TYPES.register(modEventBus);
	}
}
