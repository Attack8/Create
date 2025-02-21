package com.simibubi.create;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.simibubi.create.api.contraption.ContraptionType;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import com.simibubi.create.content.contraptions.bearing.ClockworkContraption;
import com.simibubi.create.content.contraptions.bearing.StabilizedContraption;
import com.simibubi.create.content.contraptions.elevator.ElevatorContraption;
import com.simibubi.create.content.contraptions.gantry.GantryContraption;
import com.simibubi.create.content.contraptions.mounted.MountedContraption;
import com.simibubi.create.content.contraptions.piston.PistonContraption;
import com.simibubi.create.content.contraptions.pulley.PulleyContraption;
import com.simibubi.create.content.trains.entity.CarriageContraption;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class AllContraptionTypes {
	private static final DeferredRegister<ContraptionType> REGISTER = DeferredRegister.create(CreateRegistries.CONTRAPTION_TYPE, Create.ID);

	public static final Map<String, ContraptionType> BY_LEGACY_NAME = new HashMap<>();

	public static final RegistryObject<ContraptionType> PISTON = register("piston", PistonContraption::new);
	public static final RegistryObject<ContraptionType> BEARING = register("bearing", BearingContraption::new);
	public static final RegistryObject<ContraptionType> PULLEY = register("pulley", PulleyContraption::new);
	public static final RegistryObject<ContraptionType> CLOCKWORK = register("clockwork", ClockworkContraption::new);
	public static final RegistryObject<ContraptionType> MOUNTED = register("mounted", MountedContraption::new);
	public static final RegistryObject<ContraptionType> STABILIZED = register("stabilized", StabilizedContraption::new);
	public static final RegistryObject<ContraptionType> GANTRY = register("gantry", GantryContraption::new);
	public static final RegistryObject<ContraptionType> CARRIAGE = register("carriage", CarriageContraption::new);
	public static final RegistryObject<ContraptionType> ELEVATOR = register("elevator", ElevatorContraption::new);

	private static RegistryObject<ContraptionType> register(String name, Supplier<? extends Contraption> factory) {
		return REGISTER.register(name, () -> {
			ContraptionType type = new ContraptionType(factory);
			BY_LEGACY_NAME.put(name, type);
			return type;
		});
	}

	public static void register(IEventBus modEventBus) {
		REGISTER.register(modEventBus);
	}
}
