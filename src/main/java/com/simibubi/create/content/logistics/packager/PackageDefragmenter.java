package com.simibubi.create.content.logistics.packager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageItem.PackageOrderData;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public class PackageDefragmenter {

	protected Map<Integer, List<ItemStack>> collectedPackages = new HashMap<>();

	public void clear() {
		collectedPackages.clear();
	}

	public boolean isFragmented(ItemStack box) {
		if (!box.has(AllDataComponents.PACKAGE_ORDER_DATA))
			return false;

		PackageOrderData data = box.get(AllDataComponents.PACKAGE_ORDER_DATA);

		return !(data.linkIndex() == 0 && data.isFinalLink() && data.fragmentIndex() == 0 && data.isFinal());
	}

	public int addPackageFragment(ItemStack box) {
		int collectedOrderId = PackageItem.getOrderId(box);
		if (collectedOrderId == -1)
			return -1;

		List<ItemStack> collectedOrder = collectedPackages.computeIfAbsent(collectedOrderId, $ -> Lists.newArrayList());
		collectedOrder.add(box);

		if (!isOrderComplete(collectedOrderId))
			return -1;

		return collectedOrderId;
	}

	public List<ItemStack> repack(int orderId) {
		List<ItemStack> exportingPackages = new ArrayList<>();
		String address = "";
		PackageOrder orderContext = null;
		List<BigItemStack> allItems = new ArrayList<>();

		for (ItemStack box : collectedPackages.get(orderId)) {
			address = PackageItem.getAddress(box);
			if (box.has(AllDataComponents.PACKAGE_ORDER_DATA))
				orderContext = box.get(AllDataComponents.PACKAGE_ORDER_DATA).orderContext();
			ItemStackHandler contents = PackageItem.getContents(box);
			Slots: for (int slot = 0; slot < contents.getSlots(); slot++) {
				ItemStack stackInSlot = contents.getStackInSlot(slot);
				for (BigItemStack existing : allItems) {
					if (!ItemStack.isSameItemSameComponents(stackInSlot, existing.stack))
						continue;
					existing.count += stackInSlot.getCount();
					continue Slots;
				}
				allItems.add(new BigItemStack(stackInSlot, stackInSlot.getCount()));
			}
		}

		List<BigItemStack> orderedStacks =
			orderContext == null ? Collections.emptyList() : new ArrayList<>(orderContext.stacks());
		List<ItemStack> outputSlots = new ArrayList<>();

		Repack: while (true) {
			allItems.removeIf(e -> e.count == 0);
			if (allItems.isEmpty())
				break;

			BigItemStack targetedEntry = null;
			if (!orderedStacks.isEmpty())
				targetedEntry = orderedStacks.remove(0);

			ItemSearch: for (BigItemStack entry : allItems) {
				int targetAmount = entry.count;
				if (targetAmount == 0)
					continue;
				if (targetedEntry != null) {
					targetAmount = targetedEntry.count;
					if (!ItemStack.isSameItemSameComponents(entry.stack, targetedEntry.stack))
						continue;
				}

				while (targetAmount > 0) {
					int removedAmount = Math.min(Math.min(targetAmount, entry.stack.getMaxStackSize()), entry.count);
					if (removedAmount == 0)
						continue ItemSearch;

					ItemStack output = entry.stack.copyWithCount(removedAmount);
					targetAmount -= removedAmount;
					targetedEntry.count = targetAmount;
					entry.count -= removedAmount;
					outputSlots.add(output);
				}

				continue Repack;
			}
		}

		int currentSlot = 0;
		ItemStackHandler target = new ItemStackHandler(PackageItem.SLOTS);

		for (ItemStack item : outputSlots) {
			target.setStackInSlot(currentSlot++, item);
			if (currentSlot < PackageItem.SLOTS)
				continue;
			exportingPackages.add(PackageItem.containing(target));
			target = new ItemStackHandler(PackageItem.SLOTS);
			currentSlot = 0;
		}

		for (int slot = 0; slot < target.getSlots(); slot++)
			if (!target.getStackInSlot(slot)
				.isEmpty()) {
				exportingPackages.add(PackageItem.containing(target));
				break;
			}

		for (ItemStack box : exportingPackages)
			PackageItem.addAddress(box, address);

		if (!exportingPackages.isEmpty())
			PackageItem.addOrderContext(exportingPackages.get(0), orderContext);

		return exportingPackages;
	}

	private boolean isOrderComplete(int orderId) {
		boolean finalLinkReached = false;
		Links: for (int linkCounter = 0; linkCounter < 1000; linkCounter++) {
			if (finalLinkReached)
				break;
			Packages: for (int packageCounter = 0; packageCounter < 1000; packageCounter++) {
				for (ItemStack box : collectedPackages.get(orderId)) {
					PackageOrderData data = box.get(AllDataComponents.PACKAGE_ORDER_DATA);
					if (linkCounter != data.linkIndex())
						continue;
					if (packageCounter != data.fragmentIndex())
						continue;
					finalLinkReached = data.isFinalLink();
					if (data.isFinal())
						continue Links;
					continue Packages;
				}
				return false;
			}
		}
		return true;
	}

}
