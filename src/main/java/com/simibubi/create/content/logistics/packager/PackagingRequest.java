package com.simibubi.create.content.logistics.packager;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;

import net.createmod.catnip.codecs.CatnipCodecs;
import net.minecraft.world.item.ItemStack;

public record PackagingRequest(ItemStack item, MutableInt count, String address, int linkIndex,
	MutableBoolean finalLink, MutableInt packageCounter, int orderId, @Nullable PackageOrder context) {
	public static Codec<PackagingRequest> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ItemStack.CODEC.fieldOf("item").forGetter(PackagingRequest::item),
		CatnipCodecs.MUTABLE_INT_CODEC.fieldOf("count").forGetter(PackagingRequest::count),
		Codec.STRING.fieldOf("address").forGetter(PackagingRequest::address),
		Codec.INT.fieldOf("link_index").forGetter(PackagingRequest::linkIndex),
		CatnipCodecs.MUTABLE_BOOLEAN_CODEC.fieldOf("final_link").forGetter(PackagingRequest::finalLink),
		CatnipCodecs.MUTABLE_INT_CODEC.fieldOf("package_counter").forGetter(PackagingRequest::packageCounter),
		Codec.INT.fieldOf("order_id").forGetter(PackagingRequest::orderId),
		CatnipCodecs.nullableFieldOf(PackageOrder.CODEC, "context").forGetter(PackagingRequest::context)
	).apply(instance, PackagingRequest::new));

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
