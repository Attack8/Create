package com.simibubi.create.content.kinetics.simpleRelays;

import java.util.function.Predicate;

import com.google.common.base.Predicates;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.decoration.encasing.EncasableBlock;
import com.simibubi.create.content.decoration.girder.GirderEncasedShaftBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlock;
import com.simibubi.create.foundation.placement.PoleHelper;

import net.createmod.catnip.utility.placement.IPlacementHelper;
import net.createmod.catnip.utility.placement.PlacementHelpers;
import net.createmod.catnip.utility.placement.PlacementOffset;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ShaftBlock extends AbstractSimpleShaftBlock implements EncasableBlock {

	public static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

	public ShaftBlock(Properties properties) {
		super(properties);
	}

	public static boolean isShaft(BlockState state) {
		return AllBlocks.SHAFT.has(state);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState stateForPlacement = super.getStateForPlacement(context);
		return pickCorrectShaftType(stateForPlacement, context.getLevel(), context.getClickedPos());
	}

	public static BlockState pickCorrectShaftType(BlockState stateForPlacement, Level level, BlockPos pos) {
		if (PoweredShaftBlock.stillValid(stateForPlacement, level, pos))
			return PoweredShaftBlock.getEquivalent(stateForPlacement);
		return stateForPlacement;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AllShapes.SIX_VOXEL_POLE.get(state.getValue(AXIS));
	}

	@Override
	public float getParticleTargetRadius() {
		return .35f;
	}

	@Override
	public float getParticleInitialRadius() {
		return .125f;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (player.isShiftKeyDown() || !player.mayBuild())
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		ItemInteractionResult result = tryEncase(state, level, pos, stack, player, hand, hitResult);
		if (result.consumesAction())
			return result;

		if (AllBlocks.METAL_GIRDER.isIn(stack) && state.getValue(AXIS) != Axis.Y) {
			KineticBlockEntity.switchToBlockState(level, pos, AllBlocks.METAL_GIRDER_ENCASED_SHAFT.getDefaultState()
				.setValue(WATERLOGGED, state.getValue(WATERLOGGED))
				.setValue(GirderEncasedShaftBlock.HORIZONTAL_AXIS, state.getValue(AXIS) == Axis.Z ? Axis.Z : Axis.X));
			if (!level.isClientSide && !player.isCreative()) {
				stack.shrink(1);
				if (stack.isEmpty())
					player.setItemInHand(hand, ItemStack.EMPTY);
			}
			return ItemInteractionResult.SUCCESS;
		}

		IPlacementHelper helper = PlacementHelpers.get(placementHelperId);
		if (helper.matchesItem(stack))
			return helper.getOffset(player, level, state, pos, hitResult)
				.placeInWorld(level, (BlockItem) stack.getItem(), player, hand, hitResult);

		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@MethodsReturnNonnullByDefault
	private static class PlacementHelper extends PoleHelper<Direction.Axis> {
		// used for extending a shaft in its axis, like the piston poles. works with
		// shafts and cogs

		private PlacementHelper() {
			super(state -> state.getBlock() instanceof AbstractSimpleShaftBlock
				|| state.getBlock() instanceof PoweredShaftBlock, state -> state.getValue(AXIS), AXIS);
		}

		@Override
		public Predicate<ItemStack> getItemPredicate() {
			return i -> i.getItem() instanceof BlockItem
				&& ((BlockItem) i.getItem()).getBlock() instanceof AbstractSimpleShaftBlock;
		}

		@Override
		public Predicate<BlockState> getStatePredicate() {
			return Predicates.or(AllBlocks.SHAFT::has, AllBlocks.POWERED_SHAFT::has);
		}

		@Override
		public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos,
										 BlockHitResult ray) {
			PlacementOffset offset = super.getOffset(player, world, state, pos, ray);
			if (offset.isSuccessful())
				offset.withTransform(offset.getTransform()
					.andThen(s -> ShaftBlock.pickCorrectShaftType(s, world, offset.getBlockPos())));
			return offset;
		}

	}
}
