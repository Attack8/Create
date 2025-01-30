package com.simibubi.create.content.logistics.redstoneRequester;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour.RequestType;
import com.simibubi.create.content.logistics.packagerLink.WiFiParticle;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.content.logistics.stockTicker.StockCheckingBlockEntity;

import net.createmod.catnip.codecs.CatnipCodecUtils;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.common.util.FakePlayer;

public class RedstoneRequesterBlockEntity extends StockCheckingBlockEntity implements MenuProvider {

	public boolean allowPartialRequests;
	public PackageOrder encodedRequest = PackageOrder.empty();
	public PackageOrder encodedRequestContext = PackageOrder.empty();
	public String encodedTargetAdress = "";

	public boolean lastRequestSucceeded;

	protected boolean redstonePowered;

	public RedstoneRequesterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		allowPartialRequests = false;
	}

	protected void onRedstonePowerChanged() {
		boolean hasNeighborSignal = level.hasNeighborSignal(worldPosition);
		if (redstonePowered == hasNeighborSignal)
			return;

		lastRequestSucceeded = false;
		if (hasNeighborSignal)
			triggerRequest();

		redstonePowered = hasNeighborSignal;
		notifyUpdate();
	}

	public void triggerRequest() {
		if (encodedRequest.isEmpty())
			return;

		boolean anySucceeded = false;

		InventorySummary summaryOfOrder = new InventorySummary();
		encodedRequest.stacks()
			.forEach(summaryOfOrder::add);

		InventorySummary summary = getAccurateSummary();
		for (BigItemStack entry : summaryOfOrder.getStacks()) {
			if (summary.getCountOf(entry.stack) >= entry.count) {
				anySucceeded = true;
				continue;
			}
			if (!allowPartialRequests && level instanceof ServerLevel serverLevel) {
				CatnipServices.NETWORK.sendToClientsAround(serverLevel, worldPosition, 32,
					new RedstoneRequesterEffectPacket(worldPosition, false));
				return;
			}
		}

		broadcastPackageRequest(RequestType.REDSTONE, encodedRequest, null, encodedTargetAdress, encodedRequestContext.isEmpty() ? null : encodedRequestContext);
		if (level instanceof ServerLevel serverLevel)
			CatnipServices.NETWORK.sendToClientsAround(serverLevel, worldPosition, 32, new RedstoneRequesterEffectPacket(worldPosition, anySucceeded));
		lastRequestSucceeded = true;
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		redstonePowered = tag.getBoolean("Powered");
		lastRequestSucceeded = tag.getBoolean("Success");
		allowPartialRequests = tag.getBoolean("AllowPartial");
		encodedRequest = CatnipCodecUtils.decode(PackageOrder.CODEC, tag.getCompound("EncodedRequest")).orElse(PackageOrder.empty());
		encodedRequestContext = CatnipCodecUtils.decode(PackageOrder.CODEC, tag.getCompound("EncodedRequestContext")).orElse(PackageOrder.empty());
		encodedTargetAdress = tag.getString("EncodedAddress");
	}

	@Override
	public void writeSafe(CompoundTag tag, HolderLookup.Provider registries) {
		super.writeSafe(tag, registries);
		tag.putBoolean("AllowPartial", allowPartialRequests);
		tag.putString("EncodedAddress", encodedTargetAdress);
		tag.put("EncodedRequest", CatnipCodecUtils.encode(PackageOrder.CODEC, encodedRequest).orElseThrow());
		tag.put("EncodedRequestContext", CatnipCodecUtils.encode(PackageOrder.CODEC, encodedRequestContext).orElseThrow());
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		tag.putBoolean("Powered", redstonePowered);
		tag.putBoolean("Success", lastRequestSucceeded);
		tag.putBoolean("AllowPartial", allowPartialRequests);
		tag.putString("EncodedAddress", encodedTargetAdress);
		tag.put("EncodedRequest", CatnipCodecUtils.encode(PackageOrder.CODEC, encodedRequest).orElseThrow());
		tag.put("EncodedRequestContext", CatnipCodecUtils.encode(PackageOrder.CODEC, encodedRequestContext).orElseThrow());
	}

	public InteractionResult use(Player player) {
		if (player == null || player.isCrouching())
			return InteractionResult.PASS;
		if (player instanceof FakePlayer)
			return InteractionResult.PASS;
		if (level.isClientSide)
			return InteractionResult.SUCCESS;
		if (!behaviour.mayInteractMessage(player))
			return InteractionResult.SUCCESS;

		player.openMenu(this, worldPosition);
		return InteractionResult.SUCCESS;
	}

	@Override
	public Component getDisplayName() {
        return Component.empty();
    }

	@Override
	public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
		return RedstoneRequesterMenu.create(pContainerId, pPlayerInventory, this);
	}

	public void playEffect(boolean success) {
		Vec3 vec3 = Vec3.atCenterOf(worldPosition);
		if (success) {
			AllSoundEvents.CONFIRM.playAt(level, worldPosition, 0.5f, 1.5f, false);
			AllSoundEvents.STOCK_LINK.playAt(level, worldPosition, 1.0f, 1.0f, false);
			level.addParticle(new WiFiParticle.Data(), vec3.x, vec3.y, vec3.z, 1, 1, 1);
		} else {
			AllSoundEvents.DENY.playAt(level, worldPosition, 0.5f, 1, false);
			level.addParticle(ParticleTypes.ENCHANTED_HIT, vec3.x, vec3.y + 1, vec3.z, 0, 0, 0);
		}
	}

}
