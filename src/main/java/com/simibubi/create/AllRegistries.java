package com.simibubi.create;

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
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class AllRegistries {
	public static final Registry<ArmInteractionPointType> ARM_INTERACTION_POINT_TYPES = create(Keys.ARM_INTERACTION_POINT_TYPES);
	public static final Registry<FanProcessingType> FAN_PROCESSING_TYPES = create(Keys.FAN_PROCESSING_TYPES);
	public static final Registry<ItemAttributeType> ITEM_ATTRIBUTE_TYPES = create(AllRegistries.Keys.ITEM_ATTRIBUTE_TYPES);
	public static final Registry<PackagePortTargetType> PACKAGE_PORT_TARGETS = create(Keys.PACKAGE_PORT_TARGETS);
	public static final Registry<MountedItemStorageType<?>> MOUNTED_ITEM_STORAGE_TYPES = create(Keys.MOUNTED_ITEM_STORAGE_TYPES);
	public static final Registry<MountedFluidStorageType<?>> MOUNTED_FLUID_STORAGE_TYPES = create(Keys.MOUNTED_FLUID_STORAGE_TYPES);

	private static <T> Registry<T> create(ResourceKey<Registry<T>> key) {
		return new RegistryBuilder<>(key).sync(true).create();
	}

	public static final class Keys {
		public static final ResourceKey<Registry<ArmInteractionPointType>> ARM_INTERACTION_POINT_TYPES = key("arm_interaction_point_types");
		public static final ResourceKey<Registry<FanProcessingType>> FAN_PROCESSING_TYPES = key("fan_processing_types");
		public static final ResourceKey<Registry<ItemAttributeType>> ITEM_ATTRIBUTE_TYPES = key("item_attribute_types");
		public static final ResourceKey<Registry<PackagePortTargetType>> PACKAGE_PORT_TARGETS = key("package_port_targets");
		public static final ResourceKey<Registry<MountedItemStorageType<?>>> MOUNTED_ITEM_STORAGE_TYPES = key("mounted_item_storage_type");
		public static final ResourceKey<Registry<MountedFluidStorageType<?>>> MOUNTED_FLUID_STORAGE_TYPES = key("mounted_fluid_storage_type");

		private static <T> ResourceKey<Registry<T>> key(String name) {
			return ResourceKey.createRegistryKey(Create.asResource(name));
		}
	}

	@SubscribeEvent
	public static void registerRegistries(NewRegistryEvent event) {
		event.register(AllRegistries.ARM_INTERACTION_POINT_TYPES);
		event.register(AllRegistries.FAN_PROCESSING_TYPES);
		event.register(AllRegistries.ITEM_ATTRIBUTE_TYPES);
		event.register(AllRegistries.PACKAGE_PORT_TARGETS);
		event.register(AllRegistries.MOUNTED_ITEM_STORAGE_TYPES);
		event.register(AllRegistries.MOUNTED_FLUID_STORAGE_TYPES);
	}
}
