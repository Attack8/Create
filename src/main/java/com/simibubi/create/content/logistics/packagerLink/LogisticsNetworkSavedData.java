package com.simibubi.create.content.logistics.packagerLink;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.simibubi.create.Create;

import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public class LogisticsNetworkSavedData extends SavedData {

	private Map<UUID, LogisticsNetwork> logisticsNetworks = new HashMap<>();

	public static SavedData.Factory<LogisticsNetworkSavedData> factory() {
		return new SavedData.Factory<>(LogisticsNetworkSavedData::new, LogisticsNetworkSavedData::load);
	}

	@Override
	public CompoundTag save(CompoundTag nbt, HolderLookup.Provider registries) {
		GlobalLogisticsManager logistics = Create.LOGISTICS;
		nbt.put("LogisticsNetworks",
			NBTHelper.writeCompoundList(logistics.logisticsNetworks.values(), LogisticsNetwork::write));
		return nbt;
	}

	private static LogisticsNetworkSavedData load(CompoundTag nbt, HolderLookup.Provider registries) {
		LogisticsNetworkSavedData sd = new LogisticsNetworkSavedData();
		sd.logisticsNetworks = new HashMap<>();
		NBTHelper.iterateCompoundList(nbt.getList("LogisticsNetworks", Tag.TAG_COMPOUND), c -> {
			LogisticsNetwork network = LogisticsNetwork.read(c);
			sd.logisticsNetworks.put(network.id, network);
		});
		return sd;
	}

	// TODO - Uncomment on fabric, it doesn't have atomic writes
//	@Override
//	public void save(@NotNull File file, HolderLookup.Provider registries) {
//		SavedDataUtil.saveWithDatOld(this, file, registries);
//	}

	public Map<UUID, LogisticsNetwork> getLogisticsNetworks() {
		return logisticsNetworks;
	}

	private LogisticsNetworkSavedData() {}

	public static LogisticsNetworkSavedData load(MinecraftServer server) {
		return server.overworld()
			.getDataStorage()
			.computeIfAbsent(factory(), "create_logistics");
	}

}
