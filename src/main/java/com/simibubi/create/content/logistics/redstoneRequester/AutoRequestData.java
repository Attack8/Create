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

public record AutoRequestData(PackageOrder encodedRequest, String encodedTargetAddress, BlockPos targetOffset,
							  String targetDim, boolean isValid, PackageOrder encodedRequestContext) {
	public static final Codec<AutoRequestData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		PackageOrder.CODEC.fieldOf("encoded_request").forGetter(i -> i.encodedRequest),
		Codec.STRING.fieldOf("encoded_target_address").forGetter(i -> i.encodedTargetAddress),
		BlockPos.CODEC.fieldOf("target_offset").forGetter(i -> i.targetOffset),
		Codec.STRING.fieldOf("target_dim").forGetter(i -> i.targetDim),
		Codec.BOOL.fieldOf("is_valid").forGetter(i -> i.isValid),
		PackageOrder.CODEC.fieldOf("encoded_request_context").forGetter(i -> i.encodedRequestContext)
	).apply(instance, AutoRequestData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, AutoRequestData> STREAM_CODEC = StreamCodec.composite(
	    PackageOrder.STREAM_CODEC, i -> i.encodedRequest,
		ByteBufCodecs.STRING_UTF8, i -> i.encodedTargetAddress,
	    BlockPos.STREAM_CODEC, i -> i.targetOffset,
	    ByteBufCodecs.STRING_UTF8, i -> i.targetDim,
	    ByteBufCodecs.BOOL, i -> i.isValid,
	    PackageOrder.STREAM_CODEC, i -> i.encodedRequestContext,
	    AutoRequestData::new
	);

	public AutoRequestData() {
		this(PackageOrder.empty(), "", BlockPos.ZERO, "null", false, PackageOrder.empty());
	}

	public AutoRequestData copy() {
		return new AutoRequestData(
			encodedRequest,
			encodedTargetAddress,
			targetOffset,
			targetDim,
			isValid,
			encodedRequestContext
		);
	}

	public AutoRequestData copyWithOffset(BlockPos position) {
		return new AutoRequestData(
			encodedRequest,
			encodedTargetAddress,
			position.offset(targetOffset),
			targetDim,
			isValid,
			encodedRequestContext
		);
	}

	public void writeToItem(BlockPos position, ItemStack itemStack) {
		itemStack.set(AllDataComponents.AUTO_REQUEST_DATA, copyWithOffset(position));
	}

	public static AutoRequestData readFromItem(Level level, Player player, BlockPos position, ItemStack itemStack) {
		AutoRequestData requestData = itemStack.get(AllDataComponents.AUTO_REQUEST_DATA);
		if (requestData == null)
			return null;

		BlockPos targetOffset = requestData.targetOffset.subtract(position);
		boolean isValid =
			requestData.targetOffset.closerThan(BlockPos.ZERO, 128) && requestData.targetDim.equals(level.dimension()
				.location()
				.toString());

		requestData = new AutoRequestData(
			requestData.encodedRequest,
			requestData.encodedTargetAddress,
			targetOffset,
			requestData.targetDim,
			isValid,
			requestData.encodedRequestContext
		);

		if (player != null)
			CreateLang
				.translate(requestData.isValid ? "redstone_requester.keeper_connected"
					: "redstone_requester.keeper_too_far_away")
				.style(requestData.isValid ? ChatFormatting.WHITE : ChatFormatting.RED)
				.sendStatus(player);

		return requestData;
	}

}
