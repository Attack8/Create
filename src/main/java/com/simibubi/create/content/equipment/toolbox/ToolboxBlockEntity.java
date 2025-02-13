package com.simibubi.create.content.equipment.toolbox;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.animatedContainer.AnimatedContainerBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.ResetableLazy;

import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class ToolboxBlockEntity extends SmartBlockEntity implements MenuProvider, Nameable {

	public LerpedFloat lid = LerpedFloat.linear()
		.startWithValue(0);

	public LerpedFloat drawers = LerpedFloat.linear()
		.startWithValue(0);

	UUID uniqueId;
	ToolboxInventory inventory;
	ResetableLazy<DyeColor> colorProvider;

	Map<Integer, WeakHashMap<Player, Integer>> connectedPlayers;

	private Component customName;

	private AnimatedContainerBehaviour<ToolboxMenu> openTracker;

	public ToolboxBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		connectedPlayers = new HashMap<>();
		inventory = new ToolboxInventory(this);
		colorProvider = ResetableLazy.of(() -> {
			BlockState blockState = getBlockState();
			if (blockState != null && blockState.getBlock() instanceof ToolboxBlock)
				return ((ToolboxBlock) blockState.getBlock()).getColor();
			return DyeColor.BROWN;
		});
		setLazyTickRate(10);
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(
				Capabilities.ItemHandler.BLOCK,
				AllBlockEntityTypes.TOOLBOX.get(),
				(be, context) -> be.inventory
		);
	}

	public DyeColor getColor() {
		return colorProvider.get();
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(openTracker = new AnimatedContainerBehaviour<>(this, ToolboxMenu.class));
	}

	@Override
	public void initialize() {
		super.initialize();
		ToolboxHandler.onLoad(this);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		ToolboxHandler.onUnload(this);
	}

	@Override
	public void tick() {
		super.tick();

		if (level.isClientSide)
			tickAudio();
		if (!level.isClientSide)
			tickPlayers();

		lid.chase(openTracker.openCount > 0 ? 1 : 0, 0.2f, Chaser.LINEAR);
		drawers.chase(openTracker.openCount > 0 ? 1 : 0, 0.2f, Chaser.EXP);
		lid.tickChaser();
		drawers.tickChaser();
	}

	private void tickPlayers() {
		boolean update = false;

		for (Iterator<Entry<Integer, WeakHashMap<Player, Integer>>> toolboxSlots = connectedPlayers.entrySet()
			.iterator(); toolboxSlots.hasNext(); ) {

			Entry<Integer, WeakHashMap<Player, Integer>> toolboxSlotEntry = toolboxSlots.next();
			WeakHashMap<Player, Integer> set = toolboxSlotEntry.getValue();
			int slot = toolboxSlotEntry.getKey();

			ItemStack referenceItem = inventory.filters.get(slot);
			boolean clear = referenceItem.isEmpty();

			for (Iterator<Entry<Player, Integer>> playerEntries = set.entrySet()
				.iterator(); playerEntries.hasNext(); ) {
				Entry<Player, Integer> playerEntry = playerEntries.next();

				Player player = playerEntry.getKey();
				int hotbarSlot = playerEntry.getValue();

				if (!clear && !ToolboxHandler.withinRange(player, this))
					continue;

				Inventory playerInv = player.getInventory();
				ItemStack playerStack = playerInv.getItem(hotbarSlot);

				if (clear || !playerStack.isEmpty()
					&& !ToolboxInventory.canItemsShareCompartment(playerStack, referenceItem)) {
					player.getPersistentData()
						.getCompound("CreateToolboxData")
						.remove(String.valueOf(hotbarSlot));
					playerEntries.remove();
					if (player instanceof ServerPlayer)
						ToolboxHandler.syncData(player);
					continue;
				}

				int count = playerStack.getCount();
				int targetAmount = (referenceItem.getOrDefault(DataComponents.MAX_STACK_SIZE, 64) + 1) / 2;

				if (count < targetAmount) {
					int amountToReplenish = targetAmount - count;

					if (isOpenInContainer(player)) {
						ItemStack extracted = inventory.takeFromCompartment(amountToReplenish, slot, true);
						if (!extracted.isEmpty()) {
							ToolboxHandler.unequip(player, hotbarSlot, false);
							ToolboxHandler.syncData(player);
							continue;
						}
					}

					ItemStack extracted = inventory.takeFromCompartment(amountToReplenish, slot, false);
					if (!extracted.isEmpty()) {
						update = true;
						ItemStack template = playerStack.isEmpty() ? extracted : playerStack;
						playerInv.setItem(hotbarSlot,
							template.copyWithCount(count + extracted.getCount()));
					}
				}

				if (count > targetAmount) {
					int amountToDeposit = count - targetAmount;
					ItemStack toDistribute = playerStack.copyWithCount(amountToDeposit);

					if (isOpenInContainer(player)) {
						int deposited = amountToDeposit - inventory.distributeToCompartment(toDistribute, slot, true)
							.getCount();
						if (deposited > 0) {
							ToolboxHandler.unequip(player, hotbarSlot, true);
							ToolboxHandler.syncData(player);
							continue;
						}
					}

					int deposited = amountToDeposit - inventory.distributeToCompartment(toDistribute, slot, false)
						.getCount();
					if (deposited > 0) {
						update = true;
						playerInv.setItem(hotbarSlot,
							playerStack.copyWithCount(count - deposited));
					}
				}
			}

			if (clear)
				toolboxSlots.remove();
		}

		if (update)

			sendData();

	}

	private boolean isOpenInContainer(Player player) {
		return player.containerMenu instanceof ToolboxMenu
			&& ((ToolboxMenu) player.containerMenu).contentHolder == this;
	}

	public void unequipTracked() {
		if (level.isClientSide)
			return;

		Set<ServerPlayer> affected = new HashSet<>();

		for (Entry<Integer, WeakHashMap<Player, Integer>> toolboxSlotEntry : connectedPlayers.entrySet()) {

			WeakHashMap<Player, Integer> set = toolboxSlotEntry.getValue();

			for (Entry<Player, Integer> playerEntry : set.entrySet()) {
				Player player = playerEntry.getKey();
				int hotbarSlot = playerEntry.getValue();

				ToolboxHandler.unequip(player, hotbarSlot, false);
				if (player instanceof ServerPlayer)
					affected.add((ServerPlayer) player);
			}
		}

		for (ServerPlayer player : affected)
			ToolboxHandler.syncData(player);
		connectedPlayers.clear();
	}

	public void unequip(int slot, Player player, int hotbarSlot, boolean keepItems) {
		if (!connectedPlayers.containsKey(slot))
			return;
		connectedPlayers.get(slot)
			.remove(player);
		if (keepItems)
			return;

		Inventory playerInv = player.getInventory();
		ItemStack playerStack = playerInv.getItem(hotbarSlot);
		ItemStack toInsert = ToolboxInventory.cleanItemNBT(playerStack.copy());
		ItemStack remainder = inventory.distributeToCompartment(toInsert, slot, false);

		if (remainder.getCount() != toInsert.getCount())
			playerInv.setItem(hotbarSlot, remainder);
	}

	private void tickAudio() {
		Vec3 vec = VecHelper.getCenterOf(worldPosition);
		if (lid.settled()) {
			if (openTracker.openCount > 0 && lid.getChaseTarget() == 0) {
				level.playLocalSound(vec.x, vec.y, vec.z, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 0.25F,
					level.random.nextFloat() * 0.1F + 1.2F, true);
				level.playLocalSound(vec.x, vec.y, vec.z, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.1F,
					level.random.nextFloat() * 0.1F + 1.1F, true);
			}
			if (openTracker.openCount == 0 && lid.getChaseTarget() == 1)
				level.playLocalSound(vec.x, vec.y, vec.z, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.1F,
					level.random.nextFloat() * 0.1F + 1.1F, true);

		} else if (openTracker.openCount == 0 && lid.getChaseTarget() == 0 && lid.getValue(0) > 1 / 16f
			&& lid.getValue(1) < 1 / 16f)
			level.playLocalSound(vec.x, vec.y, vec.z, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 0.25F,
				level.random.nextFloat() * 0.1F + 1.2F, true);
	}

	@Override
	protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		inventory.deserializeNBT(registries, compound.getCompound("Inventory"));
		super.read(compound, registries, clientPacket);
		if (compound.contains("UniqueId", 11))
			this.uniqueId = compound.getUUID("UniqueId");
		if (compound.contains("CustomName", 8))
			this.customName = Component.Serializer.fromJson(compound.getString("CustomName"), registries);
	}

	@Override
	protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		if (uniqueId == null)
			uniqueId = UUID.randomUUID();

		compound.put("Inventory", inventory.serializeNBT(registries));
		compound.putUUID("UniqueId", uniqueId);

		if (customName != null)
			compound.putString("CustomName", Component.Serializer.toJson(customName, registries));
		super.write(compound, registries, clientPacket);
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return ToolboxMenu.create(id, inv, this);
	}

	@Override
	public void lazyTick() {
		// keep re-advertising active TEs
		ToolboxHandler.onLoad(this);
		super.lazyTick();
	}

	public void connectPlayer(int slot, Player player, int hotbarSlot) {
		if (level.isClientSide)
			return;
		WeakHashMap<Player, Integer> map = connectedPlayers.computeIfAbsent(slot, WeakHashMap::new);
		Integer previous = map.get(player);
		if (previous != null) {
			if (previous == hotbarSlot)
				return;
			ToolboxHandler.unequip(player, previous, false);
		}
		map.put(player, hotbarSlot);
	}

	public void readInventory(ItemContainerContents contents) {
		ItemHelper.fillItemStackHandler(contents, inventory);
	}

	public void setUniqueId(UUID uniqueId) {
		this.uniqueId = uniqueId;
	}

	public UUID getUniqueId() {
		return uniqueId;
	}

	public boolean isFullyInitialized() {
		// returns true when uniqueId has been initialized
		return uniqueId != null;
	}

	public void setCustomName(Component customName) {
		this.customName = customName;
	}

	@Override
	public Component getDisplayName() {
		return customName != null ? customName
			: AllBlocks.TOOLBOXES.get(getColor())
			.get()
			.getName();
	}

	@Override
	public Component getCustomName() {
		return customName;
	}

	@Override
	public boolean hasCustomName() {
		return customName != null;
	}

	@Override
	public Component getName() {
		return customName;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setBlockState(BlockState state) {
		super.setBlockState(state);
		colorProvider.reset();
	}

	@Override
	protected void applyImplicitComponents(DataComponentInput componentInput) {
		setUniqueId(componentInput.get(AllDataComponents.TOOLBOX_UUID));
		readInventory(componentInput.getOrDefault(AllDataComponents.TOOLBOX_INVENTORY, ItemContainerContents.EMPTY));
	}

	@Override
	protected void collectImplicitComponents(Builder components) {
		components.set(AllDataComponents.TOOLBOX_UUID, uniqueId);
		components.set(AllDataComponents.TOOLBOX_INVENTORY, ItemHelper.containerContentsFromHandler(inventory));
	}
}
