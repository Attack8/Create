package com.simibubi.create.api.registry;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.ApiStatus;

import com.mojang.serialization.Lifecycle;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.api.contraption.ContraptionType;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.simibubi.create.content.logistics.packagePort.PackagePortTargetType;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.registries.NewRegistryEvent;

/**
 * Static registries added by Create.
 *
 * @see CreateRegistries
 */
@EventBusSubscriber(bus = Bus.MOD)
public class CreateBuiltInRegistries {
	private static final WritableRegistry<WritableRegistry<?>> ROOT_REGISTRY = getRootRegistry();

	public static final Set<Registry<?>> REGISTRIES = new HashSet<>();

	public static final Registry<ArmInteractionPointType> ARM_INTERACTION_POINT_TYPE = simple(CreateRegistries.ARM_INTERACTION_POINT_TYPE);
	public static final Registry<FanProcessingType> FAN_PROCESSING_TYPE = simple(CreateRegistries.FAN_PROCESSING_TYPE);
	public static final Registry<ItemAttributeType> ITEM_ATTRIBUTE_TYPE = simple(CreateRegistries.ITEM_ATTRIBUTE_TYPE);
	public static final Registry<DisplaySource> DISPLAY_SOURCE = simple(CreateRegistries.DISPLAY_SOURCE);
	public static final Registry<DisplayTarget> DISPLAY_TARGET = simple(CreateRegistries.DISPLAY_TARGET);
	public static final Registry<MountedItemStorageType<?>> MOUNTED_ITEM_STORAGE_TYPE = simple(CreateRegistries.MOUNTED_ITEM_STORAGE_TYPE);
	public static final Registry<MountedFluidStorageType<?>> MOUNTED_FLUID_STORAGE_TYPE = simple(CreateRegistries.MOUNTED_FLUID_STORAGE_TYPE);
	public static final Registry<ContraptionType> CONTRAPTION_TYPE = withIntrusiveHolders(CreateRegistries.CONTRAPTION_TYPE);
	public static final Registry<PackagePortTargetType> PACKAGE_PORT_TARGET_TYPE = simple(CreateRegistries.PACKAGE_PORT_TARGET_TYPE);

	private static <T> Registry<T> simple(ResourceKey<Registry<T>> key) {
		return register(key, new MappedRegistry<>(key, Lifecycle.stable(), false));
	}

	private static <T> Registry<T> withIntrusiveHolders(ResourceKey<Registry<T>> key) {
		return register(key, new MappedRegistry<>(key, Lifecycle.stable(), true));
	}

	@SuppressWarnings("unchecked")
	private static <T> Registry<T> register(ResourceKey<Registry<T>> key, WritableRegistry<T> registry) {
		ROOT_REGISTRY.register(
			(ResourceKey<WritableRegistry<?>>) (Object) key, registry, new RegistrationInfo(Optional.empty(), Lifecycle.stable())
		);
		REGISTRIES.add(registry);
		return registry;
	}

	@SuppressWarnings("unchecked")
	private static WritableRegistry<WritableRegistry<?>> getRootRegistry() {
		// an accessor can't be used here because BuiltInRegistries is loaded too early during datagen.
		try {
			Field field = BuiltInRegistries.class.getDeclaredField("WRITABLE_REGISTRY");
			field.setAccessible(true);
			return (WritableRegistry<WritableRegistry<?>>) field.get(null);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException("Create: Failed to get root registry", e);
		}
	}

	// TODO Remove
	@ApiStatus.Internal
	public static void init() {
		// make sure the class is loaded.
		// this method is called at the tail of BuiltInRegistries, injected by a coremod. See it for details.
	}

	@SubscribeEvent
	public static void onNewRegistryEvent(NewRegistryEvent event) {
		for (Registry<?> registry : REGISTRIES)
			event.register(registry);
	}
}
