package com.simibubi.create.infrastructure.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.compat.Mods;
import com.tterrag.registrate.util.entry.RegistryEntry;

import net.createmod.catnip.platform.CatnipServices;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

public class AllCreatePonderTags {

	public static final ResourceLocation

	KINETIC_RELAYS = loc("kinetic_relays"),
	KINETIC_SOURCES = loc("kinetic_sources"),
	KINETIC_APPLIANCES = loc("kinetic_appliances"),
	FLUIDS = loc("fluids"),
	LOGISTICS = loc("logistics"),
	REDSTONE = loc("redstone"),
	DECORATION = loc("decoration"),
	CREATIVE = loc("creative"),
	MOVEMENT_ANCHOR = loc("movement_anchor"),
	CONTRAPTION_ACTOR = loc("contraption_actor"),
	CONTRAPTION_ASSEMBLY = loc("contraption_assembly"),
	SAILS = loc("windmill_sails"),
	ARM_TARGETS = loc("arm_targets"),
	TRAIN_RELATED = loc("train_related"),
	RECENTLY_UPDATED = loc("recently_updated"),
	DISPLAY_SOURCES = loc("display_sources"),
	DISPLAY_TARGETS = loc("display_targets");

	/*public static final PonderTag

	KINETIC_RELAYS = create("kinetic_relays").item(AllBlocks.COGWHEEL.get())
			.defaultLang("Kinetic Blocks", "Components which help relaying Rotational Force elsewhere"),

	KINETIC_SOURCES = create("kinetic_sources").item(AllBlocks.WATER_WHEEL.get())
			.defaultLang("Kinetic Sources", "Components which generate Rotational Force"),

	KINETIC_APPLIANCES = create("kinetic_appliances").item(AllBlocks.MECHANICAL_PRESS.get())
			.defaultLang("Kinetic Appliances", "Components which make use of Rotational Force"),

	FLUIDS = create("fluids").item(AllBlocks.FLUID_PIPE.get())
			.defaultLang("Fluid Manipulators", "Components which help relaying and making use of Fluids"),

	LOGISTICS = create("logistics").item(Blocks.CHEST)
			.defaultLang("Item Transportation", "Components which help moving items around"),

	REDSTONE = create("redstone").item(Items.REDSTONE)
			.defaultLang("Logic Components", "Components which help with redstone engineering"),

	DECORATION = create("decoration").item(Items.ROSE_BUSH)
			.defaultLang("Aesthetics", "Components used mostly for decorative purposes"),

	CREATIVE = create("creative").item(AllBlocks.CREATIVE_CRATE.get())
			.defaultLang("Creative Mode", "Components not usually available for Survival Mode"),

	MOVEMENT_ANCHOR = create("movement_anchor").item(AllBlocks.MECHANICAL_PISTON.get())
			.defaultLang("Movement Anchors",
						 "Components which allow the creation of moving contraptions, animating an attached structure in a variety of ways"),

	CONTRAPTION_ACTOR = create("contraption_actor").item(AllBlocks.MECHANICAL_HARVESTER.get())
			.defaultLang("Contraption Actors",
						 "Components which expose special behaviour when attached to a moving contraption"),

	CONTRAPTION_ASSEMBLY = create("contraption_assembly").item(AllItems.SUPER_GLUE.get())
			.defaultLang("Block Attachment Utility",
						 "Tools and Components used to assemble structures moved as an animated Contraption"),

	SAILS = create("windmill_sails").item(AllBlocks.WINDMILL_BEARING.get(), true, true)
			.defaultLang("Sails for Windmill Bearings",
						 "Blocks that count towards the strength of a Windmill Contraption when assembled. Each of these have equal efficiency in doing so."),

	ARM_TARGETS = create("arm_targets").item(AllBlocks.MECHANICAL_ARM.get(), true, true)
			.defaultLang("Targets for Mechanical Arms",
						 "Components which can be selected as inputs or outputs to the Mechanical Arm"),

	TRAIN_RELATED = create("train_related").item(AllBlocks.TRACK.get())
			.defaultLang("Railway Equipment",
						 "Components used in the construction or management of Train Contraptions"),

	RECENTLY_UPDATED = create("recently_updated").item(AllBlocks.CLIPBOARD.get())
			.defaultLang("Recent Changes",
						 "Components that have been added or changed significantly in the latest versions of Create"),

	DISPLAY_SOURCES = create("display_sources").item(AllBlocks.DISPLAY_LINK.get(), true, true)
			.defaultLang("Sources for Display Links",
						 "Components or Blocks which offer some data that can be read with a Display Link"),

	DISPLAY_TARGETS = create("display_targets").item(AllBlocks.DISPLAY_LINK.get(), true, true)
			.defaultLang("Targets for Display Links",
						 "Components or Blocks which can process and display the data received from a Display Link");*/

	private static ResourceLocation loc(String id) {
		return Create.asResource(id);
	}

	public static void register(PonderTagRegistrationHelper<ResourceLocation> helper) {

		PonderTagRegistrationHelper<RegistryEntry<?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

		PonderTagRegistrationHelper<ItemLike> itemHelper = helper.withKeyFunction(
				CatnipServices.REGISTRIES::getKeyOrThrow);

		helper.registerTag(KINETIC_RELAYS)
				.addToIndex()
				.item(AllBlocks.COGWHEEL.get())
				.title("Kinetic Blocks")
				.description("Components which help relaying Rotational Force elsewhere")
				.register();

		helper.registerTag(KINETIC_SOURCES)
				.addToIndex()
				.item(AllBlocks.WATER_WHEEL.get())
				.title("Kinetic Sources")
				.description("Components which generate Rotational Force")
				.register();

		helper.registerTag(KINETIC_APPLIANCES)
				.addToIndex()
				.item(AllBlocks.MECHANICAL_PRESS.get())
				.title("Kinetic Appliances")
				.description("Components which make use of Rotational Force")
				.register();

		helper.registerTag(FLUIDS)
				.addToIndex()
				.item(AllBlocks.FLUID_PIPE.get())
				.title("Fluid Manipulators")
				.description("Components which help relaying and making use of Fluids")
				.register();

		helper.registerTag(LOGISTICS)
				.addToIndex()
				.item(Blocks.CHEST)
				.title("Item Transportation")
				.description("Components which help moving items around")
				.register();

		helper.registerTag(REDSTONE)
				.addToIndex()
				.item(Items.REDSTONE)
				.title("Logic Components")
				.description("Components which help with redstone engineering")
				.register();

		helper.registerTag(DECORATION)
				.addToIndex()
				.item(Items.ROSE_BUSH)
				.title("Aesthetics")
				.description("Components used mostly for decorative purposes")
				.register();

		helper.registerTag(CREATIVE)
				.addToIndex()
				.item(AllBlocks.CREATIVE_CRATE.get())
				.title("Creative Mode")
				.description("Components not usually available for Survival Mode")
				.register();

		helper.registerTag(MOVEMENT_ANCHOR)
				.addToIndex()
				.item(AllBlocks.MECHANICAL_PISTON.get())
				.title("Movement Anchors")
				.description("Components which allow the creation of moving contraptions, animating an attached structure in a variety of ways")
				.register();

		helper.registerTag(CONTRAPTION_ACTOR)
				.addToIndex()
				.item(AllBlocks.MECHANICAL_HARVESTER.get())
				.title("Contraption Actors")
				.description("Components which expose special behaviour when attached to a moving contraption")
				.register();

		helper.registerTag(CONTRAPTION_ASSEMBLY)
				.addToIndex()
				.item(AllItems.SUPER_GLUE.get())
				.title("Block Attachment Utility")
				.description("Tools and Components used to assemble structures moved as an animated Contraption")
				.register();

		helper.registerTag(SAILS)
				.item(AllBlocks.WINDMILL_BEARING.get())
				.title("Sails for Windmill Bearings")
				.description("Blocks that count towards the strength of a Windmill Contraption when assembled. Each of these have equal efficiency in doing so.")
				.register();

		helper.registerTag(ARM_TARGETS)
				.item(AllBlocks.MECHANICAL_ARM.get())
				.title("Targets for Mechanical Arms")
				.description("Components which can be selected as inputs or outputs to the Mechanical Arm")
				.register();

		helper.registerTag(TRAIN_RELATED)
				.addToIndex()
				.item(AllBlocks.TRACK.get())
				.title("Railway Equipment")
				.description("Components used in the construction or management of Train Contraptions")
				.register();

		helper.registerTag(RECENTLY_UPDATED)
				.addToIndex()
				.item(AllBlocks.CLIPBOARD.get())
				.title("Recent Changes")
				.description("Components that have been added or changed significantly in the latest versions of Create")
				.register();

		helper.registerTag(DISPLAY_SOURCES)
				.item(AllBlocks.DISPLAY_LINK.get())
				.title("Sources for Display Links")
				.description("Components or Blocks which offer some data that can be read with a Display Link")
				.register();

		helper.registerTag(DISPLAY_TARGETS)
				.item(AllBlocks.DISPLAY_LINK.get())
				.title("Targets for Display Links")
				.description("Components or Blocks which can process and display the data received from a Display Link")
				.register();

		HELPER.addToTag(RECENTLY_UPDATED)
				.add(AllBlocks.WATER_WHEEL)
				.add(AllBlocks.LARGE_WATER_WHEEL)
				.add(AllBlocks.COPPER_VALVE_HANDLE)
				.add(AllBlocks.ELEVATOR_PULLEY)
				.add(AllBlocks.CONTRAPTION_CONTROLS)
				.add(AllBlocks.MECHANICAL_ROLLER)
				.add(AllBlocks.MECHANICAL_PUMP)
				.add(AllBlocks.SMART_OBSERVER)
				.add(AllBlocks.THRESHOLD_SWITCH)
				.add(AllItems.NETHERITE_BACKTANK)
				.add(AllBlocks.COPYCAT_PANEL)
				.add(AllBlocks.COPYCAT_STEP);

		HELPER.addToTag(KINETIC_RELAYS)
				.add(AllBlocks.SHAFT)
				.add(AllBlocks.COGWHEEL)
				.add(AllBlocks.LARGE_COGWHEEL)
				.add(AllItems.BELT_CONNECTOR)
				.add(AllBlocks.GEARBOX)
				.add(AllBlocks.CLUTCH)
				.add(AllBlocks.GEARSHIFT)
				.add(AllBlocks.ENCASED_CHAIN_DRIVE)
				.add(AllBlocks.ADJUSTABLE_CHAIN_GEARSHIFT)
				.add(AllBlocks.SEQUENCED_GEARSHIFT)
				.add(AllBlocks.ROTATION_SPEED_CONTROLLER);

		HELPER.addToTag(KINETIC_SOURCES)
				.add(AllBlocks.HAND_CRANK)
				.add(AllBlocks.COPPER_VALVE_HANDLE)
				.add(AllBlocks.WATER_WHEEL)
				.add(AllBlocks.LARGE_WATER_WHEEL)
				.add(AllBlocks.WINDMILL_BEARING)
				.add(AllBlocks.STEAM_ENGINE)
				.add(AllBlocks.CREATIVE_MOTOR);

		HELPER.addToTag(TRAIN_RELATED)
				.add(AllBlocks.TRACK)
				.add(AllBlocks.TRACK_STATION)
				.add(AllBlocks.TRACK_SIGNAL)
				.add(AllBlocks.TRACK_OBSERVER)
				.add(AllBlocks.TRAIN_CONTROLS)
				.add(AllItems.SCHEDULE)
				.add(AllBlocks.TRAIN_DOOR)
				.add(AllBlocks.TRAIN_TRAPDOOR)
				.add(AllBlocks.RAILWAY_CASING);

		HELPER.addToTag(KINETIC_APPLIANCES)
				.add(AllBlocks.MILLSTONE)
				.add(AllBlocks.TURNTABLE)
				.add(AllBlocks.ENCASED_FAN)
				.add(AllBlocks.CUCKOO_CLOCK)
				.add(AllBlocks.MECHANICAL_PRESS)
				.add(AllBlocks.MECHANICAL_MIXER)
				.add(AllBlocks.MECHANICAL_CRAFTER)
				.add(AllBlocks.MECHANICAL_DRILL)
				.add(AllBlocks.MECHANICAL_SAW)
				.add(AllBlocks.DEPLOYER)
				.add(AllBlocks.MECHANICAL_PUMP)
				.add(AllBlocks.MECHANICAL_ARM)
				.add(AllBlocks.MECHANICAL_PISTON)
				.add(AllBlocks.ROPE_PULLEY)
				.add(AllBlocks.ELEVATOR_PULLEY)
				.add(AllBlocks.MECHANICAL_BEARING)
				.add(AllBlocks.GANTRY_SHAFT)
				.add(AllBlocks.GANTRY_CARRIAGE)
				.add(AllBlocks.CLOCKWORK_BEARING)
				.add(AllBlocks.DISPLAY_BOARD)
				.add(AllBlocks.CRUSHING_WHEEL);

		HELPER.addToTag(FLUIDS)
				.add(AllBlocks.FLUID_PIPE)
				.add(AllBlocks.MECHANICAL_PUMP)
				.add(AllBlocks.FLUID_VALVE)
				.add(AllBlocks.SMART_FLUID_PIPE)
				.add(AllBlocks.HOSE_PULLEY)
				.add(AllBlocks.ITEM_DRAIN)
				.add(AllBlocks.SPOUT)
				.add(AllBlocks.PORTABLE_FLUID_INTERFACE)
				.add(AllBlocks.FLUID_TANK)
				.add(AllBlocks.CREATIVE_FLUID_TANK);

		HELPER.addToTag(ARM_TARGETS)
				.add(AllBlocks.MECHANICAL_ARM)
				.add(AllItems.BELT_CONNECTOR)
				.add(AllBlocks.CHUTE)
				.add(AllBlocks.DEPOT)
				.add(AllBlocks.WEIGHTED_EJECTOR)
				.add(AllBlocks.BASIN)
				.add(AllBlocks.ANDESITE_FUNNEL)
				.add(AllBlocks.BRASS_FUNNEL)
				.add(AllBlocks.MECHANICAL_CRAFTER)
				.add(AllBlocks.MILLSTONE)
				.add(AllBlocks.DEPLOYER)
				.add(AllBlocks.MECHANICAL_SAW)
				.add(AllBlocks.BLAZE_BURNER)
				.add(AllBlocks.CRUSHING_WHEEL)
				.add(AllBlocks.TRACK_STATION);

		itemHelper.addToTag(ARM_TARGETS)
				.add(Blocks.COMPOSTER)
				.add(Blocks.JUKEBOX)
				.add(Blocks.CAMPFIRE)
				.add(Blocks.SOUL_CAMPFIRE)
				.add(Blocks.RESPAWN_ANCHOR);

		HELPER.addToTag(LOGISTICS)
				.add(AllItems.BELT_CONNECTOR)
				.add(AllItems.FILTER)
				.add(AllItems.ATTRIBUTE_FILTER)
				.add(AllBlocks.CHUTE)
				.add(AllBlocks.SMART_CHUTE)
				.add(AllBlocks.ITEM_VAULT)
				.add(AllBlocks.DEPOT)
				.add(AllBlocks.WEIGHTED_EJECTOR)
				.add(AllBlocks.MECHANICAL_ARM)
				.add(AllBlocks.ANDESITE_FUNNEL)
				.add(AllBlocks.BRASS_FUNNEL)
				.add(AllBlocks.ANDESITE_TUNNEL)
				.add(AllBlocks.BRASS_TUNNEL)
				.add(AllBlocks.SMART_OBSERVER)
				.add(AllBlocks.THRESHOLD_SWITCH)
				.add(AllBlocks.CREATIVE_CRATE)
				.add(AllBlocks.PORTABLE_STORAGE_INTERFACE);

		HELPER.addToTag(DECORATION)
				.add(AllBlocks.ORANGE_NIXIE_TUBE)
				.add(AllBlocks.DISPLAY_BOARD)
				.add(AllBlocks.CUCKOO_CLOCK)
				.add(AllBlocks.WOODEN_BRACKET)
				.add(AllBlocks.METAL_BRACKET)
				.add(AllBlocks.METAL_GIRDER)
				.add(AllBlocks.ANDESITE_CASING)
				.add(AllBlocks.BRASS_CASING)
				.add(AllBlocks.COPPER_CASING)
				.add(AllBlocks.RAILWAY_CASING);

		HELPER.addToTag(CREATIVE)
				.add(AllBlocks.CREATIVE_CRATE)
				.add(AllBlocks.CREATIVE_FLUID_TANK)
				.add(AllBlocks.CREATIVE_MOTOR);

		HELPER.addToTag(SAILS)
				.add(AllBlocks.SAIL)
				.add(AllBlocks.SAIL_FRAME);

		itemHelper.addToTag(SAILS)
				.add(Blocks.WHITE_WOOL);

		HELPER.addToTag(REDSTONE)
				.add(AllBlocks.SMART_OBSERVER)
				.add(AllBlocks.THRESHOLD_SWITCH)
				.add(AllBlocks.ORANGE_NIXIE_TUBE)
				.add(AllBlocks.REDSTONE_CONTACT)
				.add(AllBlocks.ANALOG_LEVER)
				.add(AllBlocks.REDSTONE_LINK)
				.add(AllBlocks.PULSE_EXTENDER)
				.add(AllBlocks.PULSE_REPEATER)
				.add(AllBlocks.POWERED_LATCH)
				.add(AllBlocks.POWERED_TOGGLE_LATCH)
				.add(AllBlocks.ROSE_QUARTZ_LAMP);

		HELPER.addToTag(MOVEMENT_ANCHOR)
				.add(AllBlocks.MECHANICAL_PISTON)
				.add(AllBlocks.WINDMILL_BEARING)
				.add(AllBlocks.MECHANICAL_BEARING)
				.add(AllBlocks.CLOCKWORK_BEARING)
				.add(AllBlocks.ROPE_PULLEY)
				.add(AllBlocks.ELEVATOR_PULLEY)
				.add(AllBlocks.GANTRY_CARRIAGE)
				.add(AllBlocks.CART_ASSEMBLER)
				.add(AllBlocks.TRACK_STATION);

		HELPER.addToTag(CONTRAPTION_ASSEMBLY)
				.add(AllBlocks.LINEAR_CHASSIS)
				.add(AllBlocks.SECONDARY_LINEAR_CHASSIS)
				.add(AllBlocks.RADIAL_CHASSIS)
				.add(AllItems.SUPER_GLUE)
				.add(AllBlocks.STICKER);

		itemHelper.addToTag(CONTRAPTION_ASSEMBLY)
				.add(Blocks.SLIME_BLOCK)
				.add(Blocks.HONEY_BLOCK);

		HELPER.addToTag(CONTRAPTION_ACTOR)
				.add(AllBlocks.MECHANICAL_HARVESTER)
				.add(AllBlocks.MECHANICAL_PLOUGH)
				.add(AllBlocks.MECHANICAL_DRILL)
				.add(AllBlocks.MECHANICAL_SAW)
				.add(AllBlocks.DEPLOYER)
				.add(AllBlocks.PORTABLE_STORAGE_INTERFACE)
				.add(AllBlocks.PORTABLE_FLUID_INTERFACE)
				.add(AllBlocks.MECHANICAL_BEARING)
				.add(AllBlocks.ANDESITE_FUNNEL)
				.add(AllBlocks.BRASS_FUNNEL)
				.add(AllBlocks.SEATS.get(DyeColor.WHITE))
				.add(AllBlocks.TRAIN_CONTROLS)
				.add(AllBlocks.CONTRAPTION_CONTROLS)
				.add(AllBlocks.REDSTONE_CONTACT);

		itemHelper.addToTag(CONTRAPTION_ACTOR)
				.add(Blocks.BELL)
				.add(Blocks.DISPENSER)
				.add(Blocks.DROPPER);

		HELPER.addToTag(DISPLAY_SOURCES)
				.add(AllBlocks.SEATS.get(DyeColor.WHITE))
				.add(AllBlocks.ORANGE_NIXIE_TUBE)
				.add(AllBlocks.THRESHOLD_SWITCH)
				.add(AllBlocks.SMART_OBSERVER)
				.add(AllBlocks.ANDESITE_TUNNEL)
				.add(AllBlocks.TRACK_OBSERVER)
				.add(AllBlocks.TRACK_STATION)
				.add(AllBlocks.DISPLAY_LINK)
				.add(AllBlocks.BRASS_TUNNEL)
				.add(AllBlocks.CUCKOO_CLOCK)
				.add(AllBlocks.STRESSOMETER)
				.add(AllBlocks.SPEEDOMETER)
				.add(AllBlocks.FLUID_TANK)
				.add(AllItems.BELT_CONNECTOR);

		itemHelper.addToTag(DISPLAY_SOURCES)
				.add(Blocks.ENCHANTING_TABLE)
				.add(Blocks.RESPAWN_ANCHOR)
				.add(Blocks.COMMAND_BLOCK)
				.add(Blocks.TARGET);

		Mods.COMPUTERCRAFT.executeIfInstalled(() -> () -> {
			Block computer = ForgeRegistries.BLOCKS.getValue(Mods.COMPUTERCRAFT.rl("computer_advanced"));
			if (computer != null)
				itemHelper.addToTag(DISPLAY_SOURCES).add(computer);
		});

		HELPER.addToTag(DISPLAY_TARGETS)
				.add(AllBlocks.ORANGE_NIXIE_TUBE)
				.add(AllBlocks.DISPLAY_BOARD)
				.add(AllBlocks.DISPLAY_LINK);

		itemHelper.addToTag(DISPLAY_TARGETS)
				.add(Blocks.OAK_SIGN)
				.add(Blocks.LECTERN);
	}

}
