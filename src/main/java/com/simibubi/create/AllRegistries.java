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
	public static final Registry<ArmInteractionPointType> ARM_INTERACTION_POINT_TYPE = create(Keys.ARM_INTERACTION_POINT_TYPE);
	public static final Registry<FanProcessingType> FAN_PROCESSING_TYPE = create(Keys.FAN_PROCESSING_TYPE);
	public static final Registry<ItemAttributeType> ITEM_ATTRIBUTE_TYPE = create(AllRegistries.Keys.ITEM_ATTRIBUTE_TYPE);
	public static final Registry<PackagePortTargetType> PACKAGE_PORT_TARGET = create(Keys.PACKAGE_PORT_TARGET);
	public static final Registry<MountedItemStorageType<?>> MOUNTED_ITEM_STORAGE_TYPE = create(Keys.MOUNTED_ITEM_STORAGE_TYPE);
	public static final Registry<MountedFluidStorageType<?>> MOUNTED_FLUID_STORAGE_TYPE = create(Keys.MOUNTED_FLUID_STORAGE_TYPE);

	private static <T> Registry<T> create(ResourceKey<Registry<T>> key) {
		return new RegistryBuilder<>(key).sync(true).create();
	}

	// Make these non-plural
	public static final class Keys {
		public static final ResourceKey<Registry<ArmInteractionPointType>> ARM_INTERACTION_POINT_TYPE = key("arm_interaction_point_type");
		public static final ResourceKey<Registry<FanProcessingType>> FAN_PROCESSING_TYPE = key("fan_processing_type");
		public static final ResourceKey<Registry<ItemAttributeType>> ITEM_ATTRIBUTE_TYPE = key("item_attribute_type");
		public static final ResourceKey<Registry<PackagePortTargetType>> PACKAGE_PORT_TARGET = key("package_port_target");
		public static final ResourceKey<Registry<MountedItemStorageType<?>>> MOUNTED_ITEM_STORAGE_TYPE = key("mounted_item_storage_type");
		public static final ResourceKey<Registry<MountedFluidStorageType<?>>> MOUNTED_FLUID_STORAGE_TYPE = key("mounted_fluid_storage_type");

		private static <T> ResourceKey<Registry<T>> key(String name) {
			return ResourceKey.createRegistryKey(Create.asResource(name));
		}
	}

	@SubscribeEvent
	public static void registerRegistries(NewRegistryEvent event) {
		event.register(AllRegistries.ARM_INTERACTION_POINT_TYPE);
		event.register(AllRegistries.FAN_PROCESSING_TYPE);
		event.register(AllRegistries.ITEM_ATTRIBUTE_TYPE);
		event.register(AllRegistries.PACKAGE_PORT_TARGET);
		event.register(AllRegistries.MOUNTED_ITEM_STORAGE_TYPE);
		event.register(AllRegistries.MOUNTED_FLUID_STORAGE_TYPE);
	}
}
