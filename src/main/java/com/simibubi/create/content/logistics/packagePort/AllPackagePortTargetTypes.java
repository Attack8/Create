package com.simibubi.create.content.logistics.packagePort;

import com.simibubi.create.AllRegistries;
import com.simibubi.create.Create;

import com.simibubi.create.content.logistics.packagePort.PackagePortTarget.ChainConveyorFrogportTarget;

import com.simibubi.create.content.logistics.packagePort.PackagePortTarget.TrainStationFrogportTarget;

import net.minecraft.core.Holder;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import org.jetbrains.annotations.ApiStatus.Internal;

public class AllPackagePortTargetTypes {
	private static final DeferredRegister<PackagePortTargetType> REGISTER = DeferredRegister.create(AllRegistries.PACKAGE_PORT_TARGETS, Create.ID);

	public static final Holder<PackagePortTargetType> CHAIN_CONVEYOR = REGISTER.register("chain_conveyor", ChainConveyorFrogportTarget.Type::new);
	public static final Holder<PackagePortTargetType> TRAIN_STATION = REGISTER.register("train_station", TrainStationFrogportTarget.Type::new);

	@Internal
	public static void register(IEventBus eventBus) {
		REGISTER.register(eventBus);
	}
}
