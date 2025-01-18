package com.simibubi.create;

import java.util.Random;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.simibubi.create.api.behaviour.BlockSpoutingBehaviour;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.compat.computercraft.ComputerCraftProxy;
import com.simibubi.create.compat.curios.Curios;
import com.simibubi.create.content.contraptions.ContraptionMovementSetting;
import com.simibubi.create.content.decoration.palettes.AllPaletteBlocks;
import com.simibubi.create.content.equipment.armor.AllArmorMaterials;
import com.simibubi.create.content.equipment.potatoCannon.BuiltinPotatoProjectileTypes;
import com.simibubi.create.content.fluids.tank.BoilerHeaters;
import com.simibubi.create.content.kinetics.TorquePropagator;
import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import com.simibubi.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes;
import com.simibubi.create.content.logistics.item.filter.attribute.AllItemAttributeTypes;
import com.simibubi.create.content.logistics.packagerLink.GlobalLogisticsManager;
import com.simibubi.create.content.redstone.displayLink.AllDisplayBehaviours;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import com.simibubi.create.content.schematics.ServerSchematicLoader;
import com.simibubi.create.content.trains.GlobalRailwayManager;
import com.simibubi.create.content.trains.bogey.BogeySizes;
import com.simibubi.create.content.trains.track.AllPortalTracks;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.simibubi.create.foundation.recipe.AllIngredients;
import com.simibubi.create.foundation.utility.AttachedRegistry;
import com.simibubi.create.foundation.utility.CreateNBTProcessors;
import com.simibubi.create.infrastructure.command.ServerLagger;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.data.CreateDatagen;
import com.simibubi.create.infrastructure.worldgen.AllFeatures;
import com.simibubi.create.infrastructure.worldgen.AllPlacementModifiers;

import net.createmod.catnip.utility.FontHelper;
import net.createmod.catnip.utility.lang.LangBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.Level;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(Create.ID)
public class Create {
	public static final String ID = "create";
	public static final String NAME = "Create";

	public static final Logger LOGGER = LogUtils.getLogger();

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting()
		.disableHtmlEscaping()
		.create();

	/** Use the {@link Random} of a local {@link Level} or {@link Entity} or create one */
	@Deprecated
	public static final Random RANDOM = new Random();

	/**
	 * <b>Other mods should not use this field!</b> If you are an addon developer, create your own instance of
	 * {@link CreateRegistrate}.
	 */
	public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(ID)
		.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);

	static {
		REGISTRATE.setTooltipModifierFactory(item ->
			new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
			.andThen(TooltipModifier.mapNull(KineticStats.create(item)))
		);
	}

	public static final ServerSchematicLoader SCHEMATIC_RECEIVER = new ServerSchematicLoader();
	public static final RedstoneLinkNetworkHandler REDSTONE_LINK_NETWORK_HANDLER = new RedstoneLinkNetworkHandler();
	public static final TorquePropagator TORQUE_PROPAGATOR = new TorquePropagator();
	public static final GlobalRailwayManager RAILWAYS = new GlobalRailwayManager();
	public static final GlobalLogisticsManager LOGISTICS = new GlobalLogisticsManager();
	public static final ServerLagger LAGGER = new ServerLagger();

	public Create(IEventBus eventBus, ModContainer modContainer) {
		onCtor(eventBus, modContainer);
	}

	public static void onCtor(IEventBus modEventBus, ModContainer modContainer) {
		LOGGER.info("{} {} initializing! Commit hash: {}", NAME, CreateBuildInfo.VERSION, CreateBuildInfo.GIT_COMMIT);
		ModLoadingContext modLoadingContext = ModLoadingContext.get();

		REGISTRATE.registerEventListeners(modEventBus);

		AllSoundEvents.prepare();
		AllTags.init();
		AllCreativeModeTabs.register(modEventBus);
		AllArmorMaterials.register(modEventBus);
		AllBlocks.register();
		AllItems.register();
		AllFluids.register();
		AllPaletteBlocks.register();
		AllMenuTypes.register();
		AllEntityTypes.register();
		AllBlockEntityTypes.register();
		AllRecipeTypes.register(modEventBus);
		AllParticleTypes.register(modEventBus);
		AllStructureProcessorTypes.register(modEventBus);
		AllEntityDataSerializers.register(modEventBus);
		AllFeatures.register(modEventBus);
		AllPlacementModifiers.register(modEventBus);
		AllIngredients.register(modEventBus);
		AllAttachmentTypes.register(modEventBus);
		AllDataComponents.register(modEventBus);
		AllMapDecorationTypes.register(modEventBus);

		AllConfigs.register(modLoadingContext, modContainer);

		AllPackets.register();

		AllArmInteractionPointTypes.register(modEventBus);
		AllFanProcessingTypes.register(modEventBus);
		AllItemAttributeTypes.register(modEventBus);
		BlockSpoutingBehaviour.registerDefaults();

		// FIXME: some of these registrations are not thread-safe
		AllMovementBehaviours.registerDefaults();
		AllInteractionBehaviours.registerDefaults();
		AllPortalTracks.registerDefaults();
		AllDisplayBehaviours.registerDefaults();
		ContraptionMovementSetting.registerDefaults();
		BogeySizes.init();
		AllBogeyStyles.init();
		// ----

		ComputerCraftProxy.register();

		NeoForgeMod.enableMilkFluid();

		modEventBus.addListener(Create::init);
		modEventBus.addListener(Create::registerAdvancements);
		modEventBus.addListener(AllEntityTypes::registerEntityAttributes);
		modEventBus.addListener(EventPriority.LOWEST, CreateDatagen::gatherData);
		modEventBus.addListener(AllSoundEvents::register);

		// FIXME: this is not thread-safe
		Mods.CURIOS.executeIfInstalled(() -> () -> Curios.init(modEventBus));
	}

	public static void init(final FMLCommonSetupEvent event) {
		AllFluids.registerFluidInteractions();
		CreateNBTProcessors.register();

		event.enqueueWork(() -> {
			// TODO: custom registration should all happen in one place
			// Most registration happens in the constructor.
			// These registrations use Create's registered objects directly so they must run after registration has finished.
			BuiltinPotatoProjectileTypes.register();
			BoilerHeaters.registerDefaults();
			// --

			AttachedRegistry.unwrapAll();
		});
	}

	public static void registerAdvancements(final RegisterEvent event) {
		if (event.getRegistry() == BuiltInRegistries.TRIGGER_TYPES) {
			AllAdvancements.register();
			AllTriggers.register();
		}
	}

	public static LangBuilder lang() {
		return new LangBuilder(ID);
	}

	public static ResourceLocation asResource(String path) {
		return ResourceLocation.fromNamespaceAndPath(ID, path);
	}
}
