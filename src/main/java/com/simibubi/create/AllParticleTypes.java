package com.simibubi.create;

import java.util.function.Supplier;

import com.simibubi.create.content.equipment.bell.SoulBaseParticle;
import com.simibubi.create.content.equipment.bell.SoulParticle;
import com.simibubi.create.content.fluids.particle.FluidParticleData;
import com.simibubi.create.content.kinetics.base.RotationIndicatorParticleData;
import com.simibubi.create.content.kinetics.fan.AirFlowParticleData;
import com.simibubi.create.content.kinetics.steamEngine.SteamJetParticleData;
import com.simibubi.create.content.logistics.packagerLink.WiFiParticle;
import com.simibubi.create.content.trains.CubeParticleData;
import com.simibubi.create.foundation.particle.AirParticleData;
import com.simibubi.create.foundation.particle.ICustomParticleData;

import net.createmod.catnip.lang.Lang;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import org.jetbrains.annotations.ApiStatus.Internal;

public enum AllParticleTypes {
	ROTATION_INDICATOR(RotationIndicatorParticleData::new),
	AIR_FLOW(AirFlowParticleData::new),
	AIR(AirParticleData::new),
	STEAM_JET(SteamJetParticleData::new),
	CUBE(CubeParticleData::new),
	FLUID_PARTICLE(FluidParticleData::new),
	BASIN_FLUID(FluidParticleData::new),
	FLUID_DRIP(FluidParticleData::new),
	WIFI(WiFiParticle.Data::new),
	SOUL(SoulParticle.Data::new),
	SOUL_BASE(SoulBaseParticle.Data::new),
	SOUL_PERIMETER(SoulParticle.PerimeterData::new),
	SOUL_EXPANDING_PERIMETER(SoulParticle.ExpandingPerimeterData::new);

	private final ParticleEntry<?> entry;

	<D extends ParticleOptions> AllParticleTypes(Supplier<? extends ICustomParticleData<D>> typeFactory) {
		String name = Lang.asId(name());
		entry = new ParticleEntry<>(name, typeFactory);
	}

	@Internal
	public static void register(IEventBus modEventBus) {
		ParticleEntry.REGISTER.register(modEventBus);
	}

	@OnlyIn(Dist.CLIENT)
	public static void registerFactories(RegisterParticleProvidersEvent event) {
		for (AllParticleTypes particle : values())
			particle.entry.registerFactory(event);
	}

	public ParticleType<?> get() {
		return entry.object.get();
	}

	public String parameter() {
		return entry.name;
	}

	private static class ParticleEntry<D extends ParticleOptions> {
		private static final DeferredRegister<ParticleType<?>> REGISTER = DeferredRegister.create(Registries.PARTICLE_TYPE, Create.ID);

		private final String name;
		private final Supplier<? extends ICustomParticleData<D>> typeFactory;
		private final DeferredHolder<ParticleType<?>, ParticleType<D>> object;

		public ParticleEntry(String name, Supplier<? extends ICustomParticleData<D>> typeFactory) {
			this.name = name;
			this.typeFactory = typeFactory;

			object = REGISTER.register(name, () -> this.typeFactory.get().createType());
		}

		@OnlyIn(Dist.CLIENT)
		public void registerFactory(RegisterParticleProvidersEvent event) {
			typeFactory.get()
				.register(object.get(), event);
		}

	}
}
