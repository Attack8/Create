package com.simibubi.create.content.logistics.redstoneRequester;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AutoRequestData {
	public static final Codec<AutoRequestData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		PackageOrder.CODEC.fieldOf("encoded_request").forGetter(i -> i.encodedRequest),
		Codec.STRING.fieldOf("encoded_target_address").forGetter(i -> i.encodedTargetAddress),
		BlockPos.CODEC.fieldOf("target_offset").forGetter(i -> i.targetOffset),
		Codec.STRING.fieldOf("target_dim").forGetter(i -> i.targetDim),
		Codec.BOOL.fieldOf("is_valid").forGetter(i -> i.isValid)
	).apply(instance, AutoRequestData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, AutoRequestData> STREAM_CODEC = StreamCodec.composite(
	    PackageOrder.STREAM_CODEC, i -> i.encodedRequest,
		ByteBufCodecs.STRING_UTF8, i -> i.encodedTargetAddress,
	    BlockPos.STREAM_CODEC, i -> i.targetOffset,
	    ByteBufCodecs.STRING_UTF8, i -> i.targetDim,
	    ByteBufCodecs.BOOL, i -> i.isValid,
	    AutoRequestData::new
	);

	public PackageOrder encodedRequest = PackageOrder.empty();
	public String encodedTargetAddress = "";
	public BlockPos targetOffset = BlockPos.ZERO;
	public String targetDim = "null";
	public boolean isValid = false;

	public AutoRequestData() {}

	public AutoRequestData(PackageOrder encodedRequest, String encodedTargetAdress, BlockPos targetOffset, String targetDim, boolean isValid) {
		this.encodedRequest = encodedRequest;
		this.encodedTargetAddress = encodedTargetAdress;
		this.targetOffset = targetOffset;
		this.targetDim = targetDim;
		this.isValid = isValid;
	}

	public AutoRequestData copy() {
		AutoRequestData data = new AutoRequestData();
		data.encodedRequest = encodedRequest;
		data.encodedTargetAddress = encodedTargetAddress;
		data.targetOffset = targetOffset;
		data.targetDim = targetDim;
		data.isValid = isValid;
		return data;
	}

	public void writeToItem(BlockPos position, ItemStack itemStack) {
		AutoRequestData copy = copy();
		copy.targetOffset = position.offset(targetOffset);
		itemStack.set(AllDataComponents.AUTO_REQUEST_DATA, copy);
	}

	public static AutoRequestData readFromItem(Level level, Player player, BlockPos position, ItemStack itemStack) {
		AutoRequestData requestData = itemStack.get(AllDataComponents.AUTO_REQUEST_DATA);
		if (requestData == null)
			return null;

		requestData.targetOffset = requestData.targetOffset.subtract(position);
		requestData.isValid =
			requestData.targetOffset.closerThan(BlockPos.ZERO, 128) && requestData.targetDim.equals(level.dimension()
				.location()
				.toString());

		if (player != null)
			CreateLang
				.translate(requestData.isValid ? "redstone_requester.keeper_connected"
					: "redstone_requester.keeper_too_far_away")
				.style(requestData.isValid ? ChatFormatting.WHITE : ChatFormatting.RED)
				.sendStatus(player);

		return requestData;
	}

}
