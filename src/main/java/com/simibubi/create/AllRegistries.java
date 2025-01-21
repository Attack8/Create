package com.simibubi.create;

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
	public static final Registry<ArmInteractionPointType> ARM_INTERACTION_POINT_TYPES = new RegistryBuilder<>(Keys.ARM_INTERACTION_POINT_TYPES).sync(true).create();
	public static final Registry<FanProcessingType> FAN_PROCESSING_TYPES = new RegistryBuilder<>(Keys.FAN_PROCESSING_TYPES).sync(true).create();
	public static final Registry<ItemAttributeType> ITEM_ATTRIBUTE_TYPES = new RegistryBuilder<>(AllRegistries.Keys.ITEM_ATTRIBUTE_TYPES).sync(true).create();
	public static final Registry<PackagePortTargetType> PACKAGE_PORT_TARGETS = new RegistryBuilder<>(Keys.PACKAGE_PORT_TARGETS).sync(true).create();

	public static final class Keys {
		public static final ResourceKey<Registry<ArmInteractionPointType>> ARM_INTERACTION_POINT_TYPES = key("arm_interaction_point_types");
		public static final ResourceKey<Registry<FanProcessingType>> FAN_PROCESSING_TYPES = key("fan_processing_types");
		public static final ResourceKey<Registry<ItemAttributeType>> ITEM_ATTRIBUTE_TYPES = key("item_attribute_types");
		public static final ResourceKey<Registry<PackagePortTargetType>> PACKAGE_PORT_TARGETS = key("package_port_targets");

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
	}
}
