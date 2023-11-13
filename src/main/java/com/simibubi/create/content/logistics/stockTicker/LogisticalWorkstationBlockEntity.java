package com.simibubi.create.content.logistics.stockTicker;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.simibubi.create.content.logistics.logisticalLink.LogisticalLinkBlockEntity;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.utility.IntAttached;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class LogisticalWorkstationBlockEntity extends SmartBlockEntity {

	private Map<Integer, IntAttached<WeakReference<LogisticalLinkBlockEntity>>> connectedLinks = new HashMap<>();
	private InventorySummary summaryOfLinks;
	private int ticksSinceLastSummary;

	public LogisticalWorkstationBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		setLazyTickRate(10);
		ticksSinceLastSummary = 15;
	}

	@Override
	public void tick() {
		super.tick();
		if (level.isClientSide())
			return;
		if (ticksSinceLastSummary < 15)
			ticksSinceLastSummary++;
	}
	
	public InventorySummary getRecentSummary() {
		if (summaryOfLinks == null || ticksSinceLastSummary >= 15)
			refreshInventorySummary();
		return summaryOfLinks;
	}

	protected void refreshInventorySummary() {
		ticksSinceLastSummary = 0;
		summaryOfLinks = new InventorySummary();
		connectedLinks.forEach(($, entry) -> {
			LogisticalLinkBlockEntity link = entry.getSecond()
				.get();
			if (link != null && !link.isRemoved() && !link.isChunkUnloaded())
				summaryOfLinks.add(link.fetchSummaryFromPackager());
		});
	}

	@Override
	public void lazyTick() {
		if (level.isClientSide())
			return;
		for (Iterator<Integer> iterator = connectedLinks.keySet()
			.iterator(); iterator.hasNext();) {
			Integer id = iterator.next();
			IntAttached<WeakReference<LogisticalLinkBlockEntity>> entry = connectedLinks.get(id);
			entry.decrement();
			if (entry.isOrBelowZero()) {
				iterator.remove();
				continue;
			}
			LogisticalLinkBlockEntity link = entry.getSecond()
				.get();
			if (link == null || link.isRemoved() || link.isChunkUnloaded()) {
				iterator.remove();
				continue;
			}
		}
	}

	public void keepConnected(LogisticalLinkBlockEntity link) {
		connectedLinks.computeIfAbsent(link.linkId, $ -> IntAttached.withZero(new WeakReference<>(link)))
			.setFirst(3);
	}

}
