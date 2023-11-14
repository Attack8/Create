package com.simibubi.create.content.logistics.packager;

import java.util.LinkedList;
import java.util.List;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.crate.BottomlessItemHandler;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.CapManipulationBehaviourBase.InterfaceProvider;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.VersionedInventoryTrackerBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.IntAttached;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public class PackagerBlockEntity extends SmartBlockEntity {

	enum RedstoneMode {
		SEND_ONE, KEEP_SENDING
	}

	public InvManipulationBehaviour targetInventory;
	public RedstoneMode mode;
	public boolean active;
	public ItemStack heldBox;
	public ItemStack previouslyUnwrapped;

	public List<IntAttached<ItemStack>> queuedRequests; // TODO: needs to be saved to disk

	public PackagerItemHandler inventory;
	private final LazyOptional<IItemHandler> invProvider;

	public static final int CYCLE = 30;
	public int animationTicks;
	public boolean animationInward;

	private InventorySummary availableItems;
	private VersionedInventoryTrackerBehaviour invVersionTracker;

	public PackagerBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
		mode = RedstoneMode.SEND_ONE;
		active = state.getOptionalValue(PackagerBlock.POWERED)
			.orElse(false);
		heldBox = ItemStack.EMPTY;
		previouslyUnwrapped = ItemStack.EMPTY;
		inventory = new PackagerItemHandler(this);
		invProvider = LazyOptional.of(() -> inventory);
		animationTicks = 0;
		animationInward = true;
		queuedRequests = new LinkedList<>();
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(targetInventory = new InvManipulationBehaviour(this, InterfaceProvider.oppositeOfBlockFacing()));
		behaviours.add(invVersionTracker = new VersionedInventoryTrackerBehaviour(this));
	}

	@Override
	public void tick() {
		super.tick();
		if (!level.isClientSide() && !queuedRequests.isEmpty())
			attemptToSend(true);
		if (animationTicks == 0) {
			previouslyUnwrapped = ItemStack.EMPTY;
			return;
		}
		animationTicks--;
	}

	public void queueRequest(IntAttached<ItemStack> requestedItem) {
		queuedRequests.add(requestedItem);
		level.blockEntityChanged(worldPosition);
	}

	public InventorySummary getAvailableItems() {
		if (availableItems != null && invVersionTracker.stillWaiting(targetInventory.getInventory()))
			return availableItems;
		availableItems = new InventorySummary();

		IItemHandler targetInv = targetInventory.getInventory();
		if (targetInv == null)
			return availableItems;

		for (int slot = 0; slot < targetInv.getSlots(); slot++)
			availableItems.add(targetInv.getStackInSlot(slot));

		invVersionTracker.awaitNewVersion(targetInventory.getInventory());
		return availableItems;
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (!active || level.isClientSide())
			return;
		attemptToSend(false);
	}

	public void activate() {
		active = true;
		setChanged();
	}

	public boolean unwrapBox(ItemStack box, boolean simulate) {
		if (animationTicks > 0)
			return false;

		ItemStackHandler contents = PackageItem.getContents(box);
		IItemHandler targetInv = targetInventory.getInventory();
		if (targetInv == null)
			return false;

		for (int slot = 0; slot < targetInv.getSlots(); slot++) {
			ItemStack itemInSlot = targetInv.getStackInSlot(slot);
			int itemsAddedToSlot = 0;
			for (int boxSlot = 0; boxSlot < contents.getSlots(); boxSlot++) {
				ItemStack toInsert = contents.getStackInSlot(boxSlot);
				if (toInsert.isEmpty())
					continue;
				if (targetInv.insertItem(slot, toInsert, true)
					.getCount() == toInsert.getCount())
					continue;
				if (itemInSlot.isEmpty()) {
					itemInSlot = toInsert;
					targetInv.insertItem(slot, toInsert, simulate);
					contents.setStackInSlot(boxSlot, ItemStack.EMPTY);
					continue;
				}
				if (!ItemHandlerHelper.canItemStacksStack(toInsert, itemInSlot))
					continue;
				int insertedAmount = toInsert.getCount() - targetInv.insertItem(slot, toInsert, simulate)
					.getCount();
				int slotLimit = (int) ((targetInv.getStackInSlot(slot)
					.isEmpty() ? itemInSlot.getMaxStackSize() / 64f : 1) * targetInv.getSlotLimit(slot));
				int insertableAmountWithPreviousItems =
					Math.min(toInsert.getCount(), slotLimit - itemInSlot.getCount() - itemsAddedToSlot);
				int added = Math.min(insertedAmount, insertableAmountWithPreviousItems);
				contents.setStackInSlot(boxSlot,
					ItemHandlerHelper.copyStackWithSize(toInsert, toInsert.getCount() - added));
			}
		}

		if (!(targetInv instanceof BottomlessItemHandler))
			for (int boxSlot = 0; boxSlot < contents.getSlots(); boxSlot++)
				if (!contents.getStackInSlot(boxSlot)
					.isEmpty())
					return false;

		if (simulate)
			return true;

		previouslyUnwrapped = box;
		animationInward = true;
		animationTicks = CYCLE;
		notifyUpdate();
		return true;
	}

	public void attemptToSend(boolean requestQueue) {
		if (!requestQueue && (mode == RedstoneMode.SEND_ONE || !isBlockPowered()))
			active = false;
		if (!heldBox.isEmpty() || animationTicks != 0)
			return;
		IItemHandler targetInv = targetInventory.getInventory();
		if (targetInv == null)
			return;

		boolean anyItemPresent = false;
		ItemStackHandler extractedItems = new ItemStackHandler(PackageItem.SLOTS);
		ItemStack extractedPackageItem = ItemStack.EMPTY;
		IntAttached<ItemStack> nextRequest = null;

		if (requestQueue && !queuedRequests.isEmpty())
			nextRequest = queuedRequests.get(0);

		Outer: for (int i = 0; i < PackageItem.SLOTS; i++)
			for (int slot = 0; slot < targetInv.getSlots(); slot++) {
				int initialCount = requestQueue ? Math.min(64, nextRequest.getFirst()) : 64;
				ItemStack extracted = targetInv.extractItem(slot, initialCount, true);
				if (extracted.isEmpty())
					continue;
				if (requestQueue && !ItemHandlerHelper.canItemStacksStack(extracted, nextRequest.getSecond()))
					continue;

				boolean bulky = !extracted.getItem()
					.canFitInsideContainerItems();
				if (bulky && anyItemPresent)
					continue;

				anyItemPresent = true;
				int leftovers = ItemHandlerHelper.insertItemStacked(extractedItems, extracted.copy(), false)
					.getCount();
				int transferred = extracted.getCount() - leftovers;
				targetInv.extractItem(slot, transferred, false);

				if (requestQueue) {
					nextRequest.setFirst(nextRequest.getFirst() - transferred);
					if (nextRequest.isZero()) {
						queuedRequests.remove(0);
						if (queuedRequests.isEmpty())
							break Outer;
						nextRequest = queuedRequests.get(0);
						break;
					}
				}

				if (extracted.getItem() instanceof PackageItem)
					extractedPackageItem = extracted;
				if (bulky)
					break Outer;
			}

		if (!anyItemPresent) {
			if (nextRequest != null)
				queuedRequests.remove(0);
			return;
		}

		heldBox = extractedPackageItem.isEmpty() ? PackageItem.containing(extractedItems) : extractedPackageItem.copy();
		animationInward = false;
		animationTicks = CYCLE;
		notifyUpdate();
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		active = compound.getBoolean("Active");
		animationInward = compound.getBoolean("AnimationInward");
		animationTicks = compound.getInt("AnimationTicks");
		mode = NBTHelper.readEnum(compound, "Mode", RedstoneMode.class);
		heldBox = ItemStack.of(compound.getCompound("HeldBox"));
		previouslyUnwrapped = ItemStack.of(compound.getCompound("InsertedBox"));
	}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.putBoolean("Active", active);
		compound.putBoolean("AnimationInward", animationInward);
		compound.putInt("AnimationTicks", animationTicks);
		NBTHelper.writeEnum(compound, "Mode", mode);
		compound.put("HeldBox", heldBox.serializeNBT());
		compound.put("InsertedBox", previouslyUnwrapped.serializeNBT());
	}

	@Override
	public void invalidate() {
		super.invalidate();
		invProvider.invalidate();
	}

	@Override
	public void destroy() {
		super.destroy();
		ItemHelper.dropContents(level, worldPosition, inventory);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == ForgeCapabilities.ITEM_HANDLER)
			return invProvider.cast();
		return super.getCapability(cap, side);
	}

	private boolean isBlockPowered() {
		return getBlockState().getOptionalValue(PackagerBlock.POWERED)
			.orElse(false);
	}

	public float getTrayOffset(float partialTicks) {
		float progress = Mth.clamp(Math.max(0, animationTicks - 5 - partialTicks) / (CYCLE * .75f) * 2 - 1, -1, 1);
		progress = 1 - progress * progress;
		return progress * progress;
	}

	public ItemStack getRenderedBox() {
		if (animationInward)
			return animationTicks <= CYCLE / 2 ? ItemStack.EMPTY : previouslyUnwrapped;
		return animationTicks >= CYCLE / 2 ? ItemStack.EMPTY : heldBox;
	}

}
