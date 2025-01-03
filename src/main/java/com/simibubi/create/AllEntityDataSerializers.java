package com.simibubi.create;

import com.simibubi.create.content.trains.entity.CarriageSyncDataSerializer;

import net.minecraft.network.syncher.EntityDataSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class AllEntityDataSerializers {
	private static final DeferredRegister<EntityDataSerializer<?>> REGISTER = DeferredRegister.create(NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, Create.ID);

	public static final CarriageSyncDataSerializer CARRIAGE_DATA = new CarriageSyncDataSerializer();

	public static final DeferredHolder<EntityDataSerializer<?>, CarriageSyncDataSerializer> CARRIAGE_DATA_ENTRY = REGISTER.register("carriage_data", () -> CARRIAGE_DATA);

	public static void register(IEventBus modEventBus) {
		REGISTER.register(modEventBus);
	}
}
