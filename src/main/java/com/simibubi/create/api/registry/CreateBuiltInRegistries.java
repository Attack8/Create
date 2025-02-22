package com.simibubi.create.api.registry;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.api.contraption.ContraptionType;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.simibubi.create.content.logistics.packagePort.PackagePortTargetType;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

/**
 * Static registries added by Create.
 *
 * @see CreateRegistries
 */
@EventBusSubscriber(bus = Bus.MOD)
public class CreateBuiltInRegistries {
	public static final Set<Registry<?>> REGISTRIES = new HashSet<>();

	public static final Registry<ArmInteractionPointType> ARM_INTERACTION_POINT_TYPE = simple(CreateRegistries.ARM_INTERACTION_POINT_TYPE);
	public static final Registry<FanProcessingType> FAN_PROCESSING_TYPE = simple(CreateRegistries.FAN_PROCESSING_TYPE);
	public static final Registry<ItemAttributeType> ITEM_ATTRIBUTE_TYPE = simple(CreateRegistries.ITEM_ATTRIBUTE_TYPE);
	public static final Registry<DisplaySource> DISPLAY_SOURCE = simple(CreateRegistries.DISPLAY_SOURCE);
	public static final Registry<DisplayTarget> DISPLAY_TARGET = simple(CreateRegistries.DISPLAY_TARGET);
	public static final Registry<MountedItemStorageType<?>> MOUNTED_ITEM_STORAGE_TYPE = withCallback(CreateRegistries.MOUNTED_ITEM_STORAGE_TYPE, MountedItemStorageType::bind);
	public static final Registry<MountedFluidStorageType<?>> MOUNTED_FLUID_STORAGE_TYPE = simple(CreateRegistries.MOUNTED_FLUID_STORAGE_TYPE);
	public static final Registry<ContraptionType> CONTRAPTION_TYPE = withCallback(CreateRegistries.CONTRAPTION_TYPE, ContraptionType::bind);
	public static final Registry<PackagePortTargetType> PACKAGE_PORT_TARGET_TYPE = simple(CreateRegistries.PACKAGE_PORT_TARGET_TYPE);

	private static <T> Registry<T> simple(ResourceKey<Registry<T>> key) {
		Registry<T> registry = new RegistryBuilder<>(key)
			.sync(true)
			.create();
		return register(registry);
	}

	private static <T> Registry<T> withCallback(ResourceKey<Registry<T>> key, Consumer<T> callback) {
		Registry<T> registry = new RegistryBuilder<>(key)
			.onAdd((r, i, k, v) -> callback.accept(v))
			.sync(true)
			.create();
		return register(registry);
	}

	private static <T> Registry<T> register(Registry<T> registry) {
		REGISTRIES.add(registry);
		return registry;
	}

	@SubscribeEvent
	public static void onNewRegistryEvent(NewRegistryEvent event) {
		for (Registry<?> registry : REGISTRIES)
			event.register(registry);
		REGISTRIES.clear();
	}
}
