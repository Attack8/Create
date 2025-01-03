package com.simibubi.create.content.equipment.potatoCannon;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllPackets;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.createmod.catnip.platform.CatnipServices;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PotatoProjectileTypeManager {

	private static final Map<ResourceLocation, PotatoCannonProjectileType> BUILTIN_TYPE_MAP = new HashMap<>();
	private static final Map<ResourceLocation, PotatoCannonProjectileType> CUSTOM_TYPE_MAP = new HashMap<>();
	private static final Map<Item, PotatoCannonProjectileType> ITEM_TO_TYPE_MAP = new IdentityHashMap<>();

	public static void registerBuiltinType(ResourceLocation id, PotatoCannonProjectileType type) {
		synchronized (BUILTIN_TYPE_MAP) {
			BUILTIN_TYPE_MAP.put(id, type);
		}
	}

	public static PotatoCannonProjectileType getBuiltinType(ResourceLocation id) {
		return BUILTIN_TYPE_MAP.get(id);
	}

	public static PotatoCannonProjectileType getCustomType(ResourceLocation id) {
		return CUSTOM_TYPE_MAP.get(id);
	}

	public static PotatoCannonProjectileType getTypeForItem(Item item) {
		return ITEM_TO_TYPE_MAP.get(item);
	}

	public static Set<Item> getItems() {
		return ITEM_TO_TYPE_MAP.keySet();
	}

	public static Optional<PotatoCannonProjectileType> getTypeForStack(ItemStack item) {
		if (item.isEmpty())
			return Optional.empty();
		return Optional.ofNullable(getTypeForItem(item.getItem()));
	}

	public static void clear() {
		CUSTOM_TYPE_MAP.clear();
		ITEM_TO_TYPE_MAP.clear();
	}

	public static void fillItemMap() {
		for (Map.Entry<ResourceLocation, PotatoCannonProjectileType> entry : BUILTIN_TYPE_MAP.entrySet()) {
			PotatoCannonProjectileType type = entry.getValue();
			for (Item item : type.getItems()) {
				ITEM_TO_TYPE_MAP.put(item, type);
			}
		}
		for (Map.Entry<ResourceLocation, PotatoCannonProjectileType> entry : CUSTOM_TYPE_MAP.entrySet()) {
			PotatoCannonProjectileType type = entry.getValue();
			for (Item item : type.getItems()) {
				ITEM_TO_TYPE_MAP.put(item, type);
			}
		}
		ITEM_TO_TYPE_MAP.remove(AllItems.POTATO_CANNON.get());
	}

	public static void syncTo(ServerPlayer player) {
		CatnipServices.NETWORK.sendToClient(player, new SyncPacket(CUSTOM_TYPE_MAP));
	}

	public static void syncToAll() {
		CatnipServices.NETWORK.sendToAllClients(new SyncPacket(CUSTOM_TYPE_MAP));
	}

	public static class ReloadListener extends SimpleJsonResourceReloadListener {

		private static final Gson GSON = new Gson();

		public static final ReloadListener INSTANCE = new ReloadListener();

		protected ReloadListener() {
			super(GSON, "potato_cannon_projectile_types");
		}

		@Override
		protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
			clear();

			for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
				PotatoCannonProjectileType.CODEC.decode(JsonOps.INSTANCE, entry.getValue()).result().ifPresent(p -> {
					CUSTOM_TYPE_MAP.put(entry.getKey(), p.getFirst());
				});
			}

			fillItemMap();
		}

	}

	public record SyncPacket(Map<ResourceLocation, PotatoCannonProjectileType> customTypes) implements ClientboundPacketPayload {
		public static final StreamCodec<RegistryFriendlyByteBuf, SyncPacket> STREAM_CODEC = ByteBufCodecs.map(
				SyncPacket::newMap, ResourceLocation.STREAM_CODEC, PotatoCannonProjectileType.STREAM_CODEC
		).map(SyncPacket::new, SyncPacket::customTypes);

		@Override
		@OnlyIn(Dist.CLIENT)
		public void handle(LocalPlayer player) {
			clear();

			CUSTOM_TYPE_MAP.putAll(customTypes);

			fillItemMap();
		}

		@Override
		public PacketTypeProvider getTypeProvider() {
			return AllPackets.SYNC_POTATO_PROJECTILE_TYPES;
		}

		// needed for generics
		private static <K, V> Map<K, V> newMap(int size) {
			return new HashMap<>(size);
		}
	}

}
