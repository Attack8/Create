package com.simibubi.create.content.redstone.displayLink;

import java.util.List;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.compat.computercraft.ComputerCraftProxy;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelSupportBehaviour;
import com.simibubi.create.content.redstone.displayLink.source.DisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTarget;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.nbt.NBTHelper;
import dan200.computercraft.api.peripheral.PeripheralCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class DisplayLinkBlockEntity extends LinkWithBulbBlockEntity {

	protected BlockPos targetOffset;

	public DisplaySource activeSource;
	private CompoundTag sourceConfig;

	public DisplayTarget activeTarget;
	public int targetLine;

	public int refreshTicks;
	public AbstractComputerBehaviour computerBehaviour;
	public FactoryPanelSupportBehaviour factoryPanelSupport;

	public DisplayLinkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		targetOffset = BlockPos.ZERO;
		sourceConfig = new CompoundTag();
		targetLine = 0;
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		if (Mods.COMPUTERCRAFT.isLoaded()) {
			event.registerBlockEntity(
					PeripheralCapability.get(),
					AllBlockEntityTypes.DISPLAY_LINK.get(),
					(be, context) -> be.computerBehaviour.getPeripheralCapability()
			);
		}
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(computerBehaviour = ComputerCraftProxy.behaviour(this));
		behaviours.add(factoryPanelSupport = new FactoryPanelSupportBehaviour(this, () -> false, () -> false, () -> {
			updateGatheredData();
		}));
		registerAwardables(behaviours, AllAdvancements.DISPLAY_LINK, AllAdvancements.DISPLAY_BOARD);
	}

	@Override
	public void tick() {
		super.tick();

		if (isVirtual())
			return;
		if (activeSource == null)
			return;
		if (level.isClientSide)
			return;

		refreshTicks++;
		if (refreshTicks < activeSource.getPassiveRefreshTicks() || !activeSource.shouldPassiveReset())
			return;
		tickSource();
	}

	public void tickSource() {
		refreshTicks = 0;
		if (getBlockState().getOptionalValue(DisplayLinkBlock.POWERED)
			.orElse(true))
			return;
		if (!level.isClientSide)
			updateGatheredData();
	}

	public void onNoLongerPowered() {
		if (activeSource == null)
			return;
		refreshTicks = 0;
		activeSource.onSignalReset(new DisplayLinkContext(level, this));
		updateGatheredData();
	}

	public void updateGatheredData() {
		BlockPos sourcePosition = getSourcePosition();
		BlockPos targetPosition = getTargetPosition();

		if (!level.isLoaded(targetPosition) || !level.isLoaded(sourcePosition))
			return;

		DisplayTarget target = AllDisplayBehaviours.targetOf(level, targetPosition);
		List<DisplaySource> sources = AllDisplayBehaviours.sourcesOf(level, sourcePosition);
		boolean notify = false;

		if (activeTarget != target) {
			activeTarget = target;
			notify = true;
		}

		if (activeSource != null && !sources.contains(activeSource)) {
			activeSource = null;
			sourceConfig = new CompoundTag();
			notify = true;
		}

		if (notify)
			notifyUpdate();
		if (activeSource == null || activeTarget == null)
			return;

		DisplayLinkContext context = new DisplayLinkContext(level, this);
		activeSource.transferData(context, activeTarget, targetLine);
		sendPulseNextSync();
		sendData();

		award(AllAdvancements.DISPLAY_LINK);
	}

	@Override
	public void writeSafe(CompoundTag tag, HolderLookup.Provider registries) {
		super.writeSafe(tag, registries);
		writeGatheredData(tag);
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		writeGatheredData(tag);
		if (clientPacket && activeTarget != null)
			tag.putString("TargetType", activeTarget.id.toString());
	}

	private void writeGatheredData(CompoundTag tag) {
		tag.put("TargetOffset", NbtUtils.writeBlockPos(targetOffset));
		tag.putInt("TargetLine", targetLine);

		if (activeSource != null) {
			CompoundTag data = sourceConfig.copy();
			data.putString("Id", activeSource.id.toString());
			tag.put("Source", data);
		}
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		targetOffset = NBTHelper.readBlockPos(tag, "TargetOffset");
		targetLine = tag.getInt("TargetLine");

		if (clientPacket && tag.contains("TargetType"))
			activeTarget = AllDisplayBehaviours.getTarget(ResourceLocation.parse(tag.getString("TargetType")));
		if (!tag.contains("Source"))
			return;

		CompoundTag data = tag.getCompound("Source");
		activeSource = AllDisplayBehaviours.getSource(ResourceLocation.parse(data.getString("Id")));
		sourceConfig = new CompoundTag();
		if (activeSource != null)
			sourceConfig = data.copy();
	}

	@Override
	public void invalidate() {
		super.invalidate();
		computerBehaviour.removePeripheral();
	}

	public void target(BlockPos targetPosition) {
		this.targetOffset = targetPosition.subtract(worldPosition);
	}

	public BlockPos getSourcePosition() {
		for (FactoryPanelPosition position : factoryPanelSupport.getLinkedPanels())
			return position.pos();
		return worldPosition.relative(getDirection());
	}

	public CompoundTag getSourceConfig() {
		return sourceConfig;
	}

	public void setSourceConfig(CompoundTag sourceConfig) {
		this.sourceConfig = sourceConfig;
	}

	public Direction getDirection() {
		return getBlockState().getOptionalValue(DisplayLinkBlock.FACING)
			.orElse(Direction.UP)
			.getOpposite();
	}

	public BlockPos getTargetPosition() {
		return worldPosition.offset(targetOffset);
	}

	private static final Vec3 bulbOffset = VecHelper.voxelSpace(11, 7, 5);

	@Override
	public Vec3 getBulbOffset(BlockState state) {
		return bulbOffset;
	}

}
