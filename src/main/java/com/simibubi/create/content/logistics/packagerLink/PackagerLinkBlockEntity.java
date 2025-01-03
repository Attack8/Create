package com.simibubi.create.content.logistics.packagerLink;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagingRequest;
import com.simibubi.create.content.logistics.packager.repackager.RepackagerBlockEntity;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.utility.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.items.IItemHandler;

public class PackagerLinkBlockEntity extends SmartBlockEntity {

	public LogisticallyLinkedBehaviour behaviour;
	public UUID placedBy;

	public PackagerLinkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		setLazyTickRate(10);
		placedBy = null;
	}

	public InventorySummary fetchSummaryFromPackager(@Nullable IItemHandler ignoredHandler) {
		PackagerBlockEntity packager = getPackager();
		if (packager == null)
			return InventorySummary.EMPTY;
		if (packager.isTargetingSameInventory(ignoredHandler))
			return InventorySummary.EMPTY;
		return packager.getAvailableItems();
	}

	public void playEffect() {
		AllSoundEvents.STOCK_LINK.playAt(level, worldPosition, 1.0f, 1.0f, false);
		Vec3 vec3 = Vec3.atCenterOf(worldPosition);
		level.addParticle(new VibrationParticleOption(new BlockPositionSource(worldPosition.above(3)), 6), vec3.x,
			vec3.y, vec3.z, 1, 1, 1);
	}

	public Pair<PackagerBlockEntity, PackagingRequest> processRequest(ItemStack stack, int amount, String address,
		int linkIndex, MutableBoolean finalLink, int orderId, @Nullable PackageOrder orderContext,
		@Nullable IItemHandler ignoredHandler) {
		PackagerBlockEntity packager = getPackager();
		if (packager == null)
			return null;
		if (packager.isTargetingSameInventory(ignoredHandler))
			return null;

		InventorySummary summary = packager.getAvailableItems();
		int availableCount = summary.getCountOf(stack);
		if (availableCount == 0)
			return null;
		int toWithdraw = Math.min(amount, availableCount);
		if (toWithdraw != 0 && level instanceof ServerLevel serverLevel)
			CatnipServices.NETWORK.sendToClientsAround(serverLevel, worldPosition, 32, new PackagerLinkEffectPacket(worldPosition));
		return Pair.of(packager,
			PackagingRequest.create(stack, toWithdraw, address, linkIndex, finalLink, 0, orderId, orderContext));
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		if (placedBy != null)
			tag.putUUID("PlacedBy", placedBy);
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		placedBy = tag.contains("PlacedBy") ? tag.getUUID("PlacedBy") : null;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(behaviour = new LogisticallyLinkedBehaviour(this, true));
	}

	@Override
	public void initialize() {
		super.initialize();
		behaviour.redstonePowerChanged(PackagerLinkBlock.getPower(getBlockState(), level, worldPosition));
		PackagerBlockEntity packager = getPackager();
		if (packager != null)
			packager.recheckIfLinksPresent();
	}

	@Nullable
	public PackagerBlockEntity getPackager() {
		BlockState blockState = getBlockState();
		if (behaviour.redstonePower == 15)
			return null;
		BlockPos source = worldPosition.relative(PackagerLinkBlock.getConnectedDirection(blockState)
			.getOpposite());
		if (!(level.getBlockEntity(source) instanceof PackagerBlockEntity packager))
			return null;
		if (packager instanceof RepackagerBlockEntity)
			return null;
		return packager;
	}

}
