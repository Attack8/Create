package com.simibubi.create.content.contraptions.behaviour.dispenser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.foundation.mixin.accessor.DispenserBlockAccessor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.Vec3;

public class DispenserMovementBehaviour extends DropperMovementBehaviour {
	private static final Map<Item, IMovedDispenseItemBehaviour> movedDispenseItemBehaviors = new HashMap<>();
	private static final Set<Item> blacklist = new HashSet<>();

	private static boolean spawnEggsRegistered = false;

	public static void gatherMovedDispenseItemBehaviours() {
		IMovedDispenseItemBehaviour.init();
	}

	public static void registerMovedDispenseItemBehaviour(Item item,
		IMovedDispenseItemBehaviour movedDispenseItemBehaviour) {
		movedDispenseItemBehaviors.put(item, movedDispenseItemBehaviour);
	}

	public static DispenseItemBehavior getDispenseMethod(Level level, ItemStack itemstack) {
		return ((DispenserBlockAccessor) Blocks.DISPENSER).create$callGetDispenseMethod(level, itemstack);
	}

	@Override
	protected IMovedDispenseItemBehaviour getDispenseBehavior(MovementContext context, BlockPos pos, ItemStack stack) {
		if (!spawnEggsRegistered) {
			spawnEggsRegistered = true;
			IMovedDispenseItemBehaviour.initSpawnEggs();
		}

		Item item = stack.getItem();
		// return registered/cached behavior if present
		if (movedDispenseItemBehaviors.containsKey(item)) {
			return movedDispenseItemBehaviors.get(item);
		}

		// if there isn't one, try to create one from a vanilla behavior
		if (blacklist.contains(item)) {
			// unless it's been blacklisted, which means a behavior was created already and errored
			return MovedDefaultDispenseItemBehaviour.INSTANCE;
		}

		DispenseItemBehavior behavior = getDispenseMethod(context.world, stack);
		// no behavior or default, use the moved default
		if (behavior == null || behavior.getClass() == DefaultDispenseItemBehavior.class)
			return MovedDefaultDispenseItemBehaviour.INSTANCE;

		// projectile-specific behaviors are pretty straightforward to convert
		if (behavior instanceof ProjectileDispenseBehavior projectile) {
			IMovedDispenseItemBehaviour movedBehaviour = MovedProjectileDispenserBehaviour.of(projectile);
			// cache it for later
			registerMovedDispenseItemBehaviour(item, movedBehaviour);
			return movedBehaviour;
		}

		MinecraftServer server = context.world.getServer();
		ServerLevel serverLevel = server != null ? server.getLevel(context.world.dimension()) : null;

		DispenserBlockEntity blockEntity = null;
		if (context.world.getBlockEntity(pos) instanceof DispenserBlockEntity dispenserBlockEntity)
			blockEntity = dispenserBlockEntity;

		// other behaviors are more convoluted due to BlockSource providing a BlockEntity.
		Vec3 normal = getRotatedFacingNormal(context);
		Direction nearestFacing = Direction.getNearest(normal.x, normal.y, normal.z);
		BlockSource source = new BlockSource(serverLevel, pos, context.state, blockEntity);
		IMovedDispenseItemBehaviour movedBehavior = new FallbackMovedDispenseBehavior(item, behavior, source);
		registerMovedDispenseItemBehaviour(item, movedBehavior);
		return movedBehavior;
	}

	private static Vec3 getRotatedFacingNormal(MovementContext ctx) {
		Direction facing = ctx.state.getValue(DispenserBlock.FACING);
		Vec3 normal = Vec3.atLowerCornerOf(facing.getNormal());
		return ctx.rotation.apply(normal);
	}

	private record FallbackMovedDispenseBehavior(Item item, DispenseItemBehavior wrapped, BlockSource source) implements IMovedDispenseItemBehaviour {
		@Override
		public ItemStack dispense(ItemStack stack, MovementContext context, BlockPos pos) {
			ItemStack backup = stack.copy();
			try {
				return this.wrapped.dispense(this.source, stack);
			} catch (NullPointerException ignored) {
				// error due to lack of a BlockEntity. Un-register self to avoid continuing to fail
				movedDispenseItemBehaviors.remove(this.item);
				blacklist.add(this.item);
				return backup;
			}
		}
	}
}
