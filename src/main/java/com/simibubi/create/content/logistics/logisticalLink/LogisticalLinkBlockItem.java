package com.simibubi.create.content.logistics.logisticalLink;

import com.simibubi.create.content.logistics.stockTicker.LogisticalWorkstationBlock;
import com.simibubi.create.content.redstone.displayLink.ClickToLinkBlockItem;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;

public class LogisticalLinkBlockItem extends ClickToLinkBlockItem {

	public LogisticalLinkBlockItem(Block pBlock, Properties pProperties) {
		super(pBlock, pProperties);
	}

	@Override
	public boolean isValidTarget(LevelAccessor level, BlockPos pos) {
		return level.getBlockState(pos)
			.getBlock() instanceof LogisticalWorkstationBlock;
	}

	@Override
	public int getMaxDistanceFromSelection() {
		return AllConfigs.server().logistics.logisticalLinkRange.get();
	}

	@Override
	public String getMessageTranslationKey() {
		return "logistical_link";
	}

}
