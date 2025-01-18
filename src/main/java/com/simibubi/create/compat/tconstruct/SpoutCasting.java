package com.simibubi.create.compat.tconstruct;

import com.simibubi.create.api.behaviour.BlockSpoutingBehaviour;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

public class SpoutCasting extends BlockSpoutingBehaviour {

	private static final boolean TICON_PRESENT = Mods.TCONSTRUCT.isLoaded();

	ResourceLocation TABLE = ResourceLocation.fromNamespaceAndPath("tconstruct", "table");
	ResourceLocation BASIN = ResourceLocation.fromNamespaceAndPath("tconstruct", "basin");

	@Override
	public int fillBlock(Level level, BlockPos pos, SpoutBlockEntity spout, FluidStack availableFluid,
		boolean simulate) {
		if (!enabled())
			return 0;

		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity == null)
			return 0;

		IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, blockEntity.getBlockPos(), Direction.UP);
		if (handler == null)
			return 0;
		if (handler.getTanks() != 1)
			return 0;

		ResourceLocation registryName = RegisteredObjectsHelper.getKeyOrThrow(blockEntity.getType());
		if (!registryName.equals(TABLE) && !registryName.equals(BASIN))
			return 0;
		if (!handler.isFluidValid(0, availableFluid))
			return 0;

		FluidStack containedFluid = handler.getFluidInTank(0);
		if (!(containedFluid.isEmpty() || FluidStack.isSameFluidSameComponents(containedFluid, availableFluid)))
			return 0;

		// Do not fill if it would only partially fill the table (unless > 1000mb)
		int amount = availableFluid.getAmount();
		if (amount < 1000
			&& handler.fill(FluidHelper.copyStackWithAmount(availableFluid, amount + 1), FluidAction.SIMULATE) > amount)
			return 0;

		// Return amount filled into the table/basin
		return handler.fill(availableFluid, simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE);
	}

	private boolean enabled() {
		if (!TICON_PRESENT)
			return false;
		return AllConfigs.server().recipes.allowCastingBySpout.get();
	}

}
