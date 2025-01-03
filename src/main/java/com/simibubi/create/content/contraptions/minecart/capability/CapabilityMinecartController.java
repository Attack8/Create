package com.simibubi.create.content.contraptions.minecart.capability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.simibubi.create.AllAttachmentTypes;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.minecart.CouplingHandler;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.createmod.catnip.utility.Iterate;
import net.createmod.catnip.utility.WorldAttached;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;

public class CapabilityMinecartController {

	/* Global map of loaded carts */

	public static WorldAttached<Map<UUID, MinecartController>> loadedMinecartsByUUID;
	public static WorldAttached<Set<UUID>> loadedMinecartsWithCoupling;
	static WorldAttached<List<AbstractMinecart>> queuedAdditions;
	static WorldAttached<List<UUID>> queuedUnloads;

	static {
		loadedMinecartsByUUID = new WorldAttached<>($ -> new HashMap<>());
		loadedMinecartsWithCoupling = new WorldAttached<>($ -> new HashSet<>());
		queuedAdditions = new WorldAttached<>($ -> ObjectLists.synchronize(new ObjectArrayList<>()));
		queuedUnloads = new WorldAttached<>($ -> ObjectLists.synchronize(new ObjectArrayList<>()));
	}

	public static void tick(Level world) {
		Map<UUID, MinecartController> carts = loadedMinecartsByUUID.get(world);
		List<AbstractMinecart> queued = queuedAdditions.get(world);
		List<UUID> queuedRemovals = queuedUnloads.get(world);
		Set<UUID> cartsWithCoupling = loadedMinecartsWithCoupling.get(world);
		Set<UUID> keySet = carts.keySet();

		for (UUID removal : queuedRemovals) {
			keySet.remove(removal);
			cartsWithCoupling.remove(removal);
		}

		for (AbstractMinecart cart : queued) {
			UUID uniqueID = cart.getUUID();

			if (world.isClientSide && carts.containsKey(uniqueID)) {
				MinecartController minecartController = carts.get(uniqueID);
				if (minecartController != null) {
					AbstractMinecart minecartEntity = minecartController.cart();
					if (minecartEntity != null && minecartEntity.getId() != cart.getId())
						continue; // Away with you, Fake Entities!
				}
			}

			cartsWithCoupling.remove(uniqueID);

			if (cart.hasData(AllAttachmentTypes.MINECART_CONTROLLER)) {
				MinecartController controller = cart.getData(AllAttachmentTypes.MINECART_CONTROLLER);
				carts.put(uniqueID, controller);

				if (cart.hasData(AllAttachmentTypes.MINECART_CONTROLLER)) {
					MinecartController mc = cart.getData(AllAttachmentTypes.MINECART_CONTROLLER);
					if (mc.isLeadingCoupling()) {
						cartsWithCoupling.add(uniqueID);
					}
				}
				if (!world.isClientSide && controller != null)
					controller.sendData();
			}

			queuedRemovals.clear();
			queued.clear();

			List<UUID> toRemove = new ArrayList<>();

			for (Entry<UUID, MinecartController> entry : carts.entrySet()) {
				MinecartController controller = entry.getValue();
				if (controller != null) {
					if (controller.isPresent()) {
						controller.tick();
						continue;
					}
				}
				toRemove.add(entry.getKey());
			}

			for (UUID uuid : toRemove) {
				keySet.remove(uuid);
				cartsWithCoupling.remove(uuid);
			}
		}
	}

	public static void onChunkUnloaded(ChunkEvent.Unload event) {
		ChunkPos chunkPos = event.getChunk()
			.getPos();
		Map<UUID, MinecartController> carts = loadedMinecartsByUUID.get(event.getLevel());
		for (MinecartController minecartController : carts.values()) {
			if (minecartController == null)
				continue;
			if (!minecartController.isPresent())
				continue;
			AbstractMinecart cart = minecartController.cart();
			if (cart.chunkPosition()
				.equals(chunkPos))
				queuedUnloads.get(event.getLevel())
					.add(cart.getUUID());
		}
	}

	protected static void onCartRemoved(Level world, AbstractMinecart entity) {
		entity.removeData(AllAttachmentTypes.MINECART_CONTROLLER);

		Map<UUID, MinecartController> carts = loadedMinecartsByUUID.get(world);
		List<UUID> unloads = queuedUnloads.get(world);
		UUID uniqueID = entity.getUUID();
		if (!carts.containsKey(uniqueID) || unloads.contains(uniqueID))
			return;
		if (world.isClientSide)
			return;
		handleKilledMinecart(world, carts.get(uniqueID), entity.position());
	}

	protected static void handleKilledMinecart(Level world, MinecartController controller, Vec3 removedPos) {
		if (controller == null)
			return;
		for (boolean forward : Iterate.trueAndFalse) {
			MinecartController next = CouplingHandler.getNextInCouplingChain(world, controller, forward);
			if (next == null || next == MinecartController.EMPTY)
				continue;

			next.removeConnection(!forward);
			if (controller.hasContraptionCoupling(forward))
				continue;
			AbstractMinecart cart = next.cart();
			if (cart == null)
				continue;

			Vec3 itemPos = cart.position()
				.add(removedPos)
				.scale(.5f);
			ItemEntity itemEntity =
				new ItemEntity(world, itemPos.x, itemPos.y, itemPos.z, AllItems.MINECART_COUPLING.asStack());
			itemEntity.setDefaultPickUpDelay();
			world.addFreshEntity(itemEntity);
		}
	}

	@Nullable
	public static MinecartController getIfPresent(Level world, UUID cartId) {
		Map<UUID, MinecartController> carts = loadedMinecartsByUUID.get(world);
		if (carts == null)
			return null;
		if (!carts.containsKey(cartId))
			return null;
		return carts.get(cartId);
	}

	/* Capability management */

	public static void attach(EntityEvent.EntityConstructing event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof AbstractMinecart abstractMinecart))
			return;

		MinecartController controller = new MinecartController(abstractMinecart);
		abstractMinecart.setData(AllAttachmentTypes.MINECART_CONTROLLER, controller);

		queuedAdditions.get(entity.level()).add(abstractMinecart);
	}

	public static void onEntityDeath(EntityLeaveLevelEvent event) {
		if (event.getEntity() instanceof AbstractMinecart abstractMinecart)
			onCartRemoved(event.getLevel(), abstractMinecart);
	}

	public static void startTracking(PlayerEvent.StartTracking event) {
		Entity entity = event.getTarget();
		if (!(entity instanceof AbstractMinecart abstractMinecart))
			return;

		if (entity.hasData(AllAttachmentTypes.MINECART_CONTROLLER)) {
			entity.getData(AllAttachmentTypes.MINECART_CONTROLLER).sendData(abstractMinecart);
		}
	}
}
