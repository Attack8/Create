package com.simibubi.create.content.fluids.tank;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.decoration.steamWhistle.WhistleBlock;
import com.simibubi.create.content.decoration.steamWhistle.WhistleBlockEntity;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlock;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.utility.CreateLang;

import joptsimple.internal.Strings;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.createmod.catnip.data.Iterate;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class BoilerData {

	static final int SAMPLE_RATE = 5;

	private static final int waterSupplyPerLevel = 10;
	private static final float passiveEngineEfficiency = 1 / 8f;

	// pooled water supply
	int gatheredSupply;
	float[] supplyOverTime = new float[10];
	int ticksUntilNextSample;
	int currentIndex;

	// heat score
	public boolean needsHeatLevelUpdate;
	public boolean passiveHeat;
	public int activeHeat;

	public float waterSupply;
	public int attachedEngines;
	public int attachedWhistles;

	// display
	private int maxHeatForSize = 0;
	private int maxHeatForWater = 0;
	private int minValue = 0;
	private int maxValue = 0;
	public boolean[] occludedDirections = {true, true, true, true};

	public LerpedFloat gauge = LerpedFloat.linear();

	// client only sound control

	// re-use the same lambda for each side
	private final SoundPool.Sound sound = (level, pos) -> {
		float volume = 3f / Math.max(2, attachedEngines / 6);
		float pitch = 1.18f - level.random.nextFloat() * .25f;
		level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(),
			SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, volume, pitch, false);

		AllSoundEvents.STEAM.playAt(level, pos, volume / 16, .8f, false);
	};
	// separate pools for each side so they sound distinct when standing at corners of the boiler
	private final EnumMap<Direction, SoundPool> pools = new EnumMap<>(Direction.class);

	public void tick(FluidTankBlockEntity controller) {
		if (!isActive())
			return;
		Level level = controller.getLevel();
		if (level.isClientSide) {
			pools.values().forEach(p -> p.play(level));
			gauge.tickChaser();
			float current = gauge.getValue(1);
			if (current > 1 && level.random.nextFloat() < 1 / 2f)
				gauge.setValueNoUpdate(current + Math.min(-(current - 1) * level.random.nextFloat(), 0));
			return;
		}
		if (needsHeatLevelUpdate && updateTemperature(controller))
			controller.notifyUpdate();
		ticksUntilNextSample--;
		if (ticksUntilNextSample > 0)
			return;
		int capacity = controller.tankInventory.getCapacity();
		if (capacity == 0)
			return;

		ticksUntilNextSample = SAMPLE_RATE;
		supplyOverTime[currentIndex] = gatheredSupply / (float) SAMPLE_RATE;
		waterSupply = Math.max(waterSupply, supplyOverTime[currentIndex]);
		currentIndex = (currentIndex + 1) % supplyOverTime.length;
		gatheredSupply = 0;

		if (currentIndex == 0) {
			waterSupply = 0;
			for (float i : supplyOverTime)
				waterSupply = Math.max(i, waterSupply);
		}

		if (controller instanceof CreativeFluidTankBlockEntity)
			waterSupply = waterSupplyPerLevel * 20;

		if (getActualHeat(controller.getTotalTankSize()) == 18)
			controller.award(AllAdvancements.STEAM_ENGINE_MAXED);

		controller.notifyUpdate();
	}

	public void updateOcclusion(FluidTankBlockEntity controller) {
		if (!controller.getLevel().isClientSide)
			return;
		if (attachedEngines + attachedWhistles == 0)
			return;
		for (Direction d : Iterate.horizontalDirections) {
			AABB aabb =
				new AABB(controller.getBlockPos()).move(controller.width / 2f - .5f, 0, controller.width / 2f - .5f)
					.deflate(5f / 8);
			aabb = aabb.move(d.getStepX() * (controller.width / 2f + 1 / 4f), 0,
				d.getStepZ() * (controller.width / 2f + 1 / 4f));
			aabb = aabb.inflate(Math.abs(d.getStepZ()) / 2f, 0.25f, Math.abs(d.getStepX()) / 2f);
			occludedDirections[d.get2DDataValue()] = !controller.getLevel()
				.noCollision(aabb);
		}
	}

	public void queueSoundOnSide(BlockPos pos, Direction side) {
		SoundPool pool = pools.get(side);
		if (pool == null) {
			pool = new SoundPool(4, 2, sound);
			pools.put(side, pool);
		}
		pool.queueAt(pos);
	}

	public int getTheoreticalHeatLevel() {
		return activeHeat;
	}

	public int getMaxHeatLevelForBoilerSize(int boilerSize) {
		return (int) Math.min(18, boilerSize / 4);
	}

	public int getMaxHeatLevelForWaterSupply() {
		return (int) Math.min(18, Mth.ceil(waterSupply) / waterSupplyPerLevel);
	}

	public boolean isPassive() {
		return passiveHeat && maxHeatForSize > 0 && maxHeatForWater > 0;
	}

	public boolean isPassive(int boilerSize) {
		calcMinMaxForSize(boilerSize);
		return isPassive();
	}

	public float getEngineEfficiency(int boilerSize) {
		if (isPassive(boilerSize))
			return passiveEngineEfficiency / attachedEngines;
		if (activeHeat == 0)
			return 0;
		int actualHeat = getActualHeat(boilerSize);
		return attachedEngines <= actualHeat ? 1 : (float) actualHeat / attachedEngines;
	}

	private int getActualHeat(int boilerSize) {
		int forBoilerSize = getMaxHeatLevelForBoilerSize(boilerSize);
		int forWaterSupply = getMaxHeatLevelForWaterSupply();
		int actualHeat = Math.min(activeHeat, Math.min(forWaterSupply, forBoilerSize));
		return actualHeat;
	}

	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, int boilerSize) {
		if (!isActive())
			return false;

		calcMinMaxForSize(boilerSize);

		CreateLang.translate("boiler.status", getHeatLevelTextComponent().withStyle(ChatFormatting.GREEN))
			.forGoggles(tooltip);
		CreateLang.builder().add(getSizeComponent(true, false)).forGoggles(tooltip, 1);
		CreateLang.builder().add(getWaterComponent(true, false)).forGoggles(tooltip, 1);
		CreateLang.builder().add(getHeatComponent(true, false)).forGoggles(tooltip, 1);

		if (attachedEngines == 0)
			return true;

		int boilerLevel = Math.min(activeHeat, Math.min(maxHeatForWater, maxHeatForSize));
		double totalSU = getEngineEfficiency(boilerSize) * 16 * Math.max(boilerLevel, attachedEngines)
			* BlockStressValues.getCapacity(AllBlocks.STEAM_ENGINE.get());

		tooltip.add(CommonComponents.EMPTY);

		if (attachedEngines > 0 && maxHeatForSize > 0 && maxHeatForWater == 0 && (passiveHeat ? 1 : activeHeat) > 0) {
			CreateLang.translate("boiler.water_input_rate")
				.style(ChatFormatting.GRAY)
				.forGoggles(tooltip);
			CreateLang.number(waterSupply)
				.style(ChatFormatting.BLUE)
				.add(CreateLang.translate("generic.unit.millibuckets"))
				.add(CreateLang.text(" / ")
					.style(ChatFormatting.GRAY))
				.add(CreateLang.translate("boiler.per_tick", CreateLang.number(waterSupplyPerLevel)
						.add(CreateLang.translate("generic.unit.millibuckets")))
					.style(ChatFormatting.DARK_GRAY))
				.forGoggles(tooltip, 1);
			return true;
		}

		CreateLang.translate("tooltip.capacityProvided")
			.style(ChatFormatting.GRAY)
			.forGoggles(tooltip);

		CreateLang.number(totalSU)
			.translate("generic.unit.stress")
			.style(ChatFormatting.AQUA)
			.space()
			.add((attachedEngines == 1 ? CreateLang.translate("boiler.via_one_engine")
				: CreateLang.translate("boiler.via_engines", attachedEngines)).style(ChatFormatting.DARK_GRAY))
			.forGoggles(tooltip, 1);

		return true;
	}

	public void calcMinMaxForSize(int boilerSize) {
		maxHeatForSize = getMaxHeatLevelForBoilerSize(boilerSize);
		maxHeatForWater = getMaxHeatLevelForWaterSupply();

		minValue = Math.min(passiveHeat ? 1 : activeHeat, Math.min(maxHeatForWater, maxHeatForSize));
		maxValue = Math.max(passiveHeat ? 1 : activeHeat, Math.max(maxHeatForWater, maxHeatForSize));
	}

	@NotNull
	public MutableComponent getHeatLevelTextComponent() {
		int boilerLevel = Math.min(activeHeat, Math.min(maxHeatForWater, maxHeatForSize));

		return isPassive() ? CreateLang.translateDirect("boiler.passive")
			: (boilerLevel == 0 ? CreateLang.translateDirect("boiler.idle")
			: boilerLevel == 18 ? CreateLang.translateDirect("boiler.max_lvl")
			: CreateLang.translateDirect("boiler.lvl", String.valueOf(boilerLevel)));
	}

	public MutableComponent getSizeComponent(boolean forGoggles, boolean useBlocksAsBars, ChatFormatting... styles) {
		return componentHelper("size", maxHeatForSize, forGoggles, useBlocksAsBars, styles);
	}

	public MutableComponent getWaterComponent(boolean forGoggles, boolean useBlocksAsBars, ChatFormatting... styles) {
		return componentHelper("water", maxHeatForWater, forGoggles, useBlocksAsBars, styles);
	}

	public MutableComponent getHeatComponent(boolean forGoggles, boolean useBlocksAsBars, ChatFormatting... styles) {
		return componentHelper("heat", passiveHeat ? 1 : activeHeat, forGoggles, useBlocksAsBars, styles);
	}

	private MutableComponent componentHelper(String label, int level, boolean forGoggles, boolean useBlocksAsBars,
											 ChatFormatting... styles) {
		MutableComponent base = useBlocksAsBars ? blockComponent(level) : barComponent(level);

		if (!forGoggles)
			return base;

		ChatFormatting style1 = styles.length >= 1 ? styles[0] : ChatFormatting.GRAY;
		ChatFormatting style2 = styles.length >= 2 ? styles[1] : ChatFormatting.DARK_GRAY;

		return CreateLang.translateDirect("boiler." + label)
			.withStyle(style1)
			.append(CreateLang.translateDirect("boiler." + label + "_dots")
				.withStyle(style2))
			.append(base);
	}

	private MutableComponent blockComponent(int level) {
		return Component.literal("" + "\u2588".repeat(minValue) + "\u2592".repeat(level - minValue) + "\u2591".repeat(maxValue - level));
	}

	private MutableComponent barComponent(int level) {
		return Component.empty()
			.append(bars(Math.max(0, minValue - 1), ChatFormatting.DARK_GREEN))
			.append(bars(minValue > 0 ? 1 : 0, ChatFormatting.GREEN))
			.append(bars(Math.max(0, level - minValue), ChatFormatting.DARK_GREEN))
			.append(bars(Math.max(0, maxValue - level), ChatFormatting.DARK_RED))
			.append(bars(Math.max(0, Math.min(18 - maxValue, ((maxValue / 5 + 1) * 5) - maxValue)),
				ChatFormatting.DARK_GRAY));

	}

	private MutableComponent bars(int level, ChatFormatting format) {
		return Component.literal(Strings.repeat('|', level))
			.withStyle(format);
	}

	public boolean evaluate(FluidTankBlockEntity controller) {
		BlockPos controllerPos = controller.getBlockPos();
		Level level = controller.getLevel();
		int prevEngines = attachedEngines;
		int prevWhistles = attachedWhistles;
		attachedEngines = 0;
		attachedWhistles = 0;

		for (int yOffset = 0; yOffset < controller.height; yOffset++) {
			for (int xOffset = 0; xOffset < controller.width; xOffset++) {
				for (int zOffset = 0; zOffset < controller.width; zOffset++) {

					BlockPos pos = controllerPos.offset(xOffset, yOffset, zOffset);
					BlockState blockState = level.getBlockState(pos);
					if (!FluidTankBlock.isTank(blockState))
						continue;
					for (Direction d : Iterate.directions) {
						BlockPos attachedPos = pos.relative(d);
						BlockState attachedState = level.getBlockState(attachedPos);
						if (AllBlocks.STEAM_ENGINE.has(attachedState) && SteamEngineBlock.getFacing(attachedState) == d)
							attachedEngines++;
						if (AllBlocks.STEAM_WHISTLE.has(attachedState)
							&& WhistleBlock.getAttachedDirection(attachedState)
							.getOpposite() == d)
							attachedWhistles++;
					}
				}
			}
		}

		needsHeatLevelUpdate = true;
		return prevEngines != attachedEngines || prevWhistles != attachedWhistles;
	}

	public void checkPipeOrganAdvancement(FluidTankBlockEntity controller) {
		if (!controller.getBehaviour(AdvancementBehaviour.TYPE)
			.isOwnerPresent())
			return;

		BlockPos controllerPos = controller.getBlockPos();
		Level level = controller.getLevel();
		Set<Integer> whistlePitches = new HashSet<>();

		for (int yOffset = 0; yOffset < controller.height; yOffset++) {
			for (int xOffset = 0; xOffset < controller.width; xOffset++) {
				for (int zOffset = 0; zOffset < controller.width; zOffset++) {

					BlockPos pos = controllerPos.offset(xOffset, yOffset, zOffset);
					BlockState blockState = level.getBlockState(pos);
					if (!FluidTankBlock.isTank(blockState))
						continue;
					for (Direction d : Iterate.directions) {
						BlockPos attachedPos = pos.relative(d);
						BlockState attachedState = level.getBlockState(attachedPos);
						if (AllBlocks.STEAM_WHISTLE.has(attachedState)
							&& WhistleBlock.getAttachedDirection(attachedState)
							.getOpposite() == d) {
							if (level.getBlockEntity(attachedPos) instanceof WhistleBlockEntity wbe)
								whistlePitches.add(wbe.getPitchId());
						}
					}
				}
			}
		}

		if (whistlePitches.size() >= 12)
			controller.award(AllAdvancements.PIPE_ORGAN);
	}

	public boolean updateTemperature(FluidTankBlockEntity controller) {
		BlockPos controllerPos = controller.getBlockPos();
		Level level = controller.getLevel();
		needsHeatLevelUpdate = false;

		boolean prevPassive = passiveHeat;
		int prevActive = activeHeat;
		passiveHeat = false;
		activeHeat = 0;

		for (int xOffset = 0; xOffset < controller.width; xOffset++) {
			for (int zOffset = 0; zOffset < controller.width; zOffset++) {
				BlockPos pos = controllerPos.offset(xOffset, -1, zOffset);
				BlockState blockState = level.getBlockState(pos);
				float heat = BoilerHeater.findHeat(level, pos, blockState);
				if (heat == 0) {
					passiveHeat = true;
				} else if (heat > 0) {
					activeHeat += heat;
				}
			}
		}

		passiveHeat &= activeHeat == 0;

		return prevActive != activeHeat || prevPassive != passiveHeat;
	}

	public boolean isActive() {
		return attachedEngines > 0 || attachedWhistles > 0;
	}

	public void clear() {
		waterSupply = 0;
		activeHeat = 0;
		passiveHeat = false;
		attachedEngines = 0;
		Arrays.fill(supplyOverTime, 0);
	}

	public CompoundTag write() {
		CompoundTag nbt = new CompoundTag();
		nbt.putFloat("Supply", waterSupply);
		nbt.putInt("ActiveHeat", activeHeat);
		nbt.putBoolean("PassiveHeat", passiveHeat);
		nbt.putInt("Engines", attachedEngines);
		nbt.putInt("Whistles", attachedWhistles);
		nbt.putBoolean("Update", needsHeatLevelUpdate);
		return nbt;
	}

	public void read(CompoundTag nbt, int boilerSize) {
		waterSupply = nbt.getFloat("Supply");
		activeHeat = nbt.getInt("ActiveHeat");
		passiveHeat = nbt.getBoolean("PassiveHeat");
		attachedEngines = nbt.getInt("Engines");
		attachedWhistles = nbt.getInt("Whistles");
		needsHeatLevelUpdate = nbt.getBoolean("Update");
		Arrays.fill(supplyOverTime, (int) waterSupply);

		int forBoilerSize = getMaxHeatLevelForBoilerSize(boilerSize);
		int forWaterSupply = getMaxHeatLevelForWaterSupply();
		int actualHeat = Math.min(activeHeat, Math.min(forWaterSupply, forBoilerSize));
		float target = isPassive(boilerSize) ? 1 / 8f : forBoilerSize == 0 ? 0 : actualHeat / (forBoilerSize * 1f);
		gauge.chase(target, 0.125f, Chaser.EXP);
	}

	public BoilerFluidHandler createHandler() {
		return new BoilerFluidHandler();
	}

	public class BoilerFluidHandler implements IFluidHandler {

		@Override
		public int getTanks() {
			return 1;
		}

		@Override
		public FluidStack getFluidInTank(int tank) {
			return FluidStack.EMPTY;
		}

		@Override
		public int getTankCapacity(int tank) {
			return 10000;
		}

		@Override
		public boolean isFluidValid(int tank, FluidStack stack) {
			return FluidHelper.isWater(stack.getFluid());
		}

		@Override
		public int fill(FluidStack resource, FluidAction action) {
			if (!isFluidValid(0, resource))
				return 0;
			int amount = resource.getAmount();
			if (action.execute())
				gatheredSupply += amount;
			return amount;
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction action) {
			return FluidStack.EMPTY;
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction action) {
			return FluidStack.EMPTY;
		}

	}

}
