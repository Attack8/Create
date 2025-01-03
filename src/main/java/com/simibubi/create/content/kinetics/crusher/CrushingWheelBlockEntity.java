package com.simibubi.create.content.kinetics.crusher;

import java.util.List;

import com.simibubi.create.AllDamageTypes;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.events.custom.EntityLootEnchantmentLevelEvent;

import net.createmod.catnip.utility.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

@EventBusSubscriber
public class CrushingWheelBlockEntity extends KineticBlockEntity {
	public CrushingWheelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		setLazyTickRate(20);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		registerAwardables(behaviours, AllAdvancements.CRUSHING_WHEEL, AllAdvancements.CRUSHER_MAXED);
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		super.onSpeedChanged(prevSpeed);
		fixControllers();
	}

	public void fixControllers() {
		for (Direction d : Iterate.directions)
			((CrushingWheelBlock) getBlockState().getBlock()).updateControllers(getBlockState(), getLevel(), getBlockPos(),
					d);
	}

	@Override
	protected AABB createRenderBoundingBox() {
		return new AABB(worldPosition).inflate(1);
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		fixControllers();
	}

	@SubscribeEvent
	public static void crushingIsFortunate(EntityLootEnchantmentLevelEvent event) {
		DamageSource damageSource = event.getDamageSource();
		if (damageSource == null || !damageSource.is(AllDamageTypes.CRUSH))
			return;
		// TODO 1.21: check if this works now
		event.setLevel(2);		//This does not currently increase mob drops. It seems like this only works for damage done by an entity.
	}

	@SubscribeEvent
	public static void handleCrushedMobDrops(LivingDropsEvent event) {
		DamageSource damageSource = event.getSource();
		if (damageSource == null || !damageSource.is(AllDamageTypes.CRUSH))
			return;
		Vec3 outSpeed = Vec3.ZERO;
		for (ItemEntity outputItem : event.getDrops()) {
			outputItem.setDeltaMovement(outSpeed);
		}
	}

}
