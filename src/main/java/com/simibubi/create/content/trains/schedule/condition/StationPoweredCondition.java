package com.simibubi.create.content.trains.schedule.condition;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.station.GlobalStation;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class StationPoweredCondition extends ScheduleWaitCondition {
	@Override
	public Pair<ItemStack, Component> getSummary() {
		return Pair.of(ItemStack.EMPTY, CreateLang.translateDirect("schedule.condition.powered"));
	}

	@Override
	public boolean tickCompletion(Level level, Train train, CompoundTag context) {
		GlobalStation currentStation = train.getCurrentStation();
		if (currentStation == null)
			return false;
		BlockPos stationPos = currentStation.getBlockEntityPos();
		ResourceKey<Level> stationDim = currentStation.getBlockEntityDimension();
		MinecraftServer server = level.getServer();
		if (server == null)
			return false;
		ServerLevel stationLevel = server.getLevel(stationDim);
		if (stationLevel == null || !stationLevel.isLoaded(stationPos))
			return false;
		return stationLevel.hasNeighborSignal(stationPos);
	}

	@Override
	protected void writeAdditional(CompoundTag tag) {}

	@Override
	protected void readAdditional(CompoundTag tag) {}

	@Override
	public ResourceLocation getId() {
		return Create.asResource("powered");
	}

	@Override
	public MutableComponent getWaitingStatus(Level level, Train train, CompoundTag tag) {
		return CreateLang.translateDirect("schedule.condition.powered.status");
	}
}
