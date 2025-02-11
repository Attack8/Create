package com.simibubi.create.content.contraptions.mounted;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.MutablePair;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.ContraptionMovementSetting;
import com.simibubi.create.content.contraptions.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceMovement;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.data.ContraptionPickupLimiting;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.AbstractMinecart.Type;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber
public class MinecartContraptionItem extends Item {

	private final AbstractMinecart.Type minecartType;

	public static MinecartContraptionItem rideable(Properties builder) {
		return new MinecartContraptionItem(Type.RIDEABLE, builder);
	}

	public static MinecartContraptionItem furnace(Properties builder) {
		return new MinecartContraptionItem(Type.FURNACE, builder);
	}

	public static MinecartContraptionItem chest(Properties builder) {
		return new MinecartContraptionItem(Type.CHEST, builder);
	}

	@Override
	public boolean canFitInsideContainerItems() {
		return AllConfigs.server().kinetics.minecartContraptionInContainers.get();
	}

	private MinecartContraptionItem(Type minecartTypeIn, Properties builder) {
		super(builder);
		this.minecartType = minecartTypeIn;
		DispenserBlock.registerBehavior(this, DISPENSER_BEHAVIOR);
	}

	// Taken and adjusted from MinecartItem
	private static final DispenseItemBehavior DISPENSER_BEHAVIOR = new DefaultDispenseItemBehavior() {
		private final DefaultDispenseItemBehavior behaviourDefaultDispenseItem = new DefaultDispenseItemBehavior();
		@Override
		public ItemStack execute(BlockSource source, ItemStack stack) {
			Direction direction = source.state()
				.getValue(DispenserBlock.FACING);
			ServerLevel world = source.level();
			Vec3 vec3 = source.center();
			double d0 = vec3.x() + (double) direction.getStepX() * 1.125D;
			double d1 = Math.floor(vec3.y()) + (double) direction.getStepY();
			double d2 = vec3.z() + (double) direction.getStepZ() * 1.125D;
			BlockPos blockpos = source.pos()
				.relative(direction);
			BlockState blockstate = world.getBlockState(blockpos);
			RailShape railshape = blockstate.getBlock() instanceof BaseRailBlock
				? ((BaseRailBlock) blockstate.getBlock()).getRailDirection(blockstate, world, blockpos, null)
				: RailShape.NORTH_SOUTH;
			double d3;
			if (blockstate.is(BlockTags.RAILS)) {
				if (railshape.isAscending()) {
					d3 = 0.6D;
				} else {
					d3 = 0.1D;
				}
			} else {
				if (!blockstate.isAir() || !world.getBlockState(blockpos.below())
					.is(BlockTags.RAILS)) {
					return this.behaviourDefaultDispenseItem.dispense(source, stack);
				}

				BlockState blockstate1 = world.getBlockState(blockpos.below());
				RailShape railshape1 = blockstate1.getBlock() instanceof BaseRailBlock
					? ((BaseRailBlock) blockstate1.getBlock()).getRailDirection(blockstate1, world, blockpos.below(),
						null)
					: RailShape.NORTH_SOUTH;
				if (direction != Direction.DOWN && railshape1.isAscending()) {
					d3 = -0.4D;
				} else {
					d3 = -0.9D;
				}
			}

			AbstractMinecart abstractminecartentity = AbstractMinecart.createMinecart(world, d0, d1 + d3, d2,
				((MinecartContraptionItem) stack.getItem()).minecartType, stack, null);
			if (stack.has(DataComponents.CUSTOM_NAME))
				abstractminecartentity.setCustomName(stack.getHoverName());
			world.addFreshEntity(abstractminecartentity);
			addContraptionToMinecart(world, stack, abstractminecartentity, direction);

			stack.shrink(1);
			return stack;
		}

		@Override
		protected void playSound(BlockSource source) {
			source.level()
				.levelEvent(1000, source.pos(), 0);
		}
	};

	// Taken and adjusted from MinecartItem
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level world = context.getLevel();
		BlockPos blockpos = context.getClickedPos();
		BlockState blockstate = world.getBlockState(blockpos);
		if (!blockstate.is(BlockTags.RAILS)) {
			return InteractionResult.FAIL;
		} else {
			ItemStack itemstack = context.getItemInHand();
			if (world instanceof ServerLevel serverlevel) {
				RailShape railshape = blockstate.getBlock() instanceof BaseRailBlock
					? ((BaseRailBlock) blockstate.getBlock()).getRailDirection(blockstate, world, blockpos, null)
					: RailShape.NORTH_SOUTH;
				double d0 = 0.0D;
				if (railshape.isAscending()) {
					d0 = 0.5D;
				}

				AbstractMinecart abstractminecartentity =
					AbstractMinecart.createMinecart(serverlevel, (double) blockpos.getX() + 0.5D,
						(double) blockpos.getY() + 0.0625D + d0, (double) blockpos.getZ() + 0.5D, this.minecartType, itemstack, null);
				if (itemstack.has(DataComponents.CUSTOM_NAME))
					abstractminecartentity.setCustomName(itemstack.getHoverName());
				Player player = context.getPlayer();
				world.addFreshEntity(abstractminecartentity);
				addContraptionToMinecart(world, itemstack, abstractminecartentity,
					player == null ? null : player.getDirection());
			}

			itemstack.shrink(1);
			return InteractionResult.SUCCESS;
		}
	}

	public static void addContraptionToMinecart(Level world, ItemStack itemstack, AbstractMinecart cart,
		@Nullable Direction newFacing) {
		if (itemstack.has(AllDataComponents.MINECRAFT_CONTRAPTION_DATA)) {
			CompoundTag contraptionTag = itemstack.get(AllDataComponents.MINECRAFT_CONTRAPTION_DATA);

			Direction intialOrientation = NBTHelper.readEnum(contraptionTag, "InitialOrientation", Direction.class);

			Contraption mountedContraption = Contraption.fromNBT(world, contraptionTag, false);
			OrientedContraptionEntity contraptionEntity =
				newFacing == null ? OrientedContraptionEntity.create(world, mountedContraption, intialOrientation)
					: OrientedContraptionEntity.createAtYaw(world, mountedContraption, intialOrientation,
						newFacing.toYRot());

			contraptionEntity.startRiding(cart);
			contraptionEntity.setPos(cart.getX(), cart.getY(), cart.getZ());
			world.addFreshEntity(contraptionEntity);
		}
	}

	@Override
	public String getDescriptionId(ItemStack stack) {
		return "item.create.minecart_contraption";
	}

	@SubscribeEvent
	public static void wrenchCanBeUsedToPickUpMinecartContraptions(PlayerInteractEvent.EntityInteract event) {
		Entity entity = event.getTarget();
		Player player = event.getEntity();
		if (player == null || entity == null)
			return;
		if (!AllConfigs.server().kinetics.survivalContraptionPickup.get() && !player.isCreative())
			return;

		ItemStack wrench = player.getItemInHand(event.getHand());
		if (!AllItems.WRENCH.isIn(wrench))
			return;
		if (entity instanceof AbstractContraptionEntity)
			entity = entity.getVehicle();
		if (!(entity instanceof AbstractMinecart))
			return;
		if (!entity.isAlive())
			return;
		if (player instanceof DeployerFakePlayer dfp && dfp.onMinecartContraption)
			return;
		AbstractMinecart cart = (AbstractMinecart) entity;
		Type type = cart.getMinecartType();
		if (type != Type.RIDEABLE && type != Type.FURNACE && type != Type.CHEST)
			return;
		List<Entity> passengers = cart.getPassengers();
		if (passengers.isEmpty() || !(passengers.get(0) instanceof OrientedContraptionEntity))
			return;
		OrientedContraptionEntity oce = (OrientedContraptionEntity) passengers.get(0);
		Contraption contraption = oce.getContraption();

		if (ContraptionMovementSetting.isNoPickup(contraption.getBlocks()
			.values())) {
			player.displayClientMessage(CreateLang.translateDirect("contraption.minecart_contraption_illegal_pickup")
				.withStyle(ChatFormatting.RED), true);
			return;
		}

		if (event.getLevel().isClientSide) {
			event.setCancellationResult(InteractionResult.SUCCESS);
			event.setCanceled(true);
			return;
		}

		contraption.stop(event.getLevel());

		for (MutablePair<StructureBlockInfo, MovementContext> pair : contraption.getActors())
			if (AllMovementBehaviours.getBehaviour(pair.left.state())instanceof PortableStorageInterfaceMovement psim)
				psim.reset(pair.right);

		ItemStack generatedStack = create(type, oce);
		generatedStack.set(DataComponents.CUSTOM_NAME, entity.getCustomName());

		if (ContraptionPickupLimiting.isTooLargeForPickup(generatedStack.saveOptional(event.getLevel().registryAccess()))) {
			MutableComponent message = CreateLang.translateDirect("contraption.minecart_contraption_too_big")
					.withStyle(ChatFormatting.RED);
			player.displayClientMessage(message, true);
			return;
		}

		if (contraption.getBlocks()
			.size() > 200)
			AllAdvancements.CART_PICKUP.awardTo(player);

		player.getInventory()
			.placeItemBackInInventory(generatedStack);
		oce.discard();
		entity.discard();
		event.setCancellationResult(InteractionResult.SUCCESS);
		event.setCanceled(true);
	}

	public static ItemStack create(Type type, OrientedContraptionEntity entity) {
		ItemStack stack = ItemStack.EMPTY;

		switch (type) {
		case RIDEABLE:
			stack = AllItems.MINECART_CONTRAPTION.asStack();
			break;
		case FURNACE:
			stack = AllItems.FURNACE_MINECART_CONTRAPTION.asStack();
			break;
		case CHEST:
			stack = AllItems.CHEST_MINECART_CONTRAPTION.asStack();
			break;
		default:
			break;
		}

		if (stack.isEmpty())
			return stack;

		CompoundTag tag = entity.getContraption()
			.writeNBT(entity.registryAccess(), false);
		tag.remove("UUID");
		tag.remove("Pos");
		tag.remove("Motion");

		NBTHelper.writeEnum(tag, "InitialOrientation", entity.getInitialOrientation());

		stack.set(AllDataComponents.MINECRAFT_CONTRAPTION_DATA, tag);
		return stack;
	}
}
