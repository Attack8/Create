package com.simibubi.create.content.logistics.packager;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

import com.simibubi.create.content.logistics.stockTicker.PackageOrder;

import net.minecraft.world.item.ItemStack;

public record PackagingRequest(ItemStack item, MutableInt count, String address, int linkIndex,
	MutableBoolean finalLink, MutableInt packageCounter, int orderId, @Nullable PackageOrder context) {
	public static PackagingRequest create(ItemStack item, int count, String address, int linkIndex,
		MutableBoolean finalLink, int packageCount, int orderId, @Nullable PackageOrder context) {
		return new PackagingRequest(item, new MutableInt(count), address, linkIndex, finalLink,
			new MutableInt(packageCount), orderId, context);
	}

	public int getCount() {
		return count.intValue();
	}

	public void subtract(int toSubtract) {
		count.setValue(getCount() - toSubtract);
	}

	public boolean isEmpty() {
		return getCount() == 0;
	}
}
