package com.simibubi.create.content.logistics.box;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;

import net.createmod.catnip.utility.VecHelper;
import net.createmod.catnip.utility.lang.Components;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public class PackageItem extends Item {

	public static final List<PackageItem> ALL_BOXES = new ArrayList<>();
	public static ItemStack FALLBACK_BOX = ItemStack.EMPTY;
	public static final int SLOTS = 9;

	int width, height;
	float hookDistance;

	public PackageItem(Properties properties, int width, int height, float hookDistance) {
		super(properties);
		this.width = width;
		this.height = height;
		this.hookDistance = hookDistance;
		ALL_BOXES.add(this);
	}

	public static ItemStack getFallbackBox() {
		if (FALLBACK_BOX.isEmpty())
			FALLBACK_BOX = new ItemStack(ALL_BOXES.get(0));
		return FALLBACK_BOX;
	}

	public static boolean isPackage(ItemStack stack) {
		return stack.getItem() instanceof PackageItem;
	}

	@Override
	public boolean canFitInsideContainerItems() {
		return false;
	}

	@Override
	public boolean hasCustomEntity(ItemStack stack) {
		return true;
	}

	@Override
	public Entity createEntity(Level world, Entity location, ItemStack itemstack) {
		return PackageEntity.fromDroppedItem(world, location, itemstack);
	}

	public static ItemStack containing(List<ItemStack> stacks) {
		ItemStackHandler newInv = new ItemStackHandler(9);
		stacks.forEach(s -> ItemHandlerHelper.insertItemStacked(newInv, s, false));
		return containing(newInv);
	}

	public static ItemStack containing(ItemStackHandler stacks) {
		ItemStack box = new ItemStack(randomBox());
		CompoundTag compound = new CompoundTag();
		compound.put("Items", stacks.serializeNBT());
		box.setTag(compound);
		return box;
	}

	public static void clearAddress(ItemStack box) {
		if (box.hasTag())
			box.getTag()
				.remove("Address");
	}

	public static void addAddress(ItemStack box, String address) {
		box.getOrCreateTag()
			.putString("Address", address);
	}

	public static void setOrder(ItemStack box, int orderId, int linkIndex, boolean isFinalLink, int fragmentIndex,
		boolean isFinal, @Nullable PackageOrder orderContext) {
		CompoundTag tag = new CompoundTag();
		tag.putInt("OrderId", orderId);
		tag.putInt("LinkIndex", linkIndex);
		tag.putBoolean("IsFinalLink", isFinalLink);
		tag.putInt("Index", fragmentIndex);
		tag.putBoolean("IsFinal", isFinal);
		if (orderContext != null)
			tag.put("OrderContext", orderContext.write());
		box.getOrCreateTag()
			.put("Fragment", tag);
	}

	public static int getOrderId(ItemStack box) {
		CompoundTag tag = box.getTag();
		if (tag == null || !tag.contains("Fragment"))
			return -1;
		return tag.getCompound("Fragment")
			.getInt("OrderId");
	}

	public static PackageOrder getOrderContext(ItemStack box) {
		CompoundTag tag = box.getTag();
		if (tag == null || !tag.contains("Fragment"))
			return null;
		CompoundTag frag = tag.getCompound("Fragment");
		if (!frag.contains("OrderContext"))
			return null;
		return PackageOrder.read(frag.getCompound("OrderContext"));
	}
	
	public static void addOrderContext(ItemStack box, PackageOrder orderContext) {
		CompoundTag tag = box.getOrCreateTagElement("Fragment");
		if (orderContext != null)
			tag.put("OrderContext", orderContext.write());
		box.getOrCreateTag()
			.put("Fragment", tag);
	}

	public static boolean matchAddress(ItemStack box, String address) {
		return matchAddress(getAddress(box), address);
	}

	public static boolean matchAddress(String boxAddress, String address) {
		if (address.isBlank())
			return boxAddress.isBlank();
		if (address.equals("*") || boxAddress.equals("*"))
			return true;
		String matcher = "\\Q" + address.replace("*", "\\E.*\\Q") + "\\E";
		String boxMatcher = "\\Q" + boxAddress.replace("*", "\\E.*\\Q") + "\\E";
		return address.matches(boxMatcher) || boxAddress.matches(matcher);
	}

	public static String getAddress(ItemStack box) {
		String boxAddress = !box.hasTag() ? ""
			: box.getTag()
				.getString("Address");
		return boxAddress;
	}

	public static float getWidth(ItemStack box) {
		if (box.getItem() instanceof PackageItem pi)
			return pi.width / 16f;
		return 1;
	}

	public static float getHeight(ItemStack box) {
		if (box.getItem() instanceof PackageItem pi)
			return pi.height / 16f;
		return 1;
	}

	public static float getHookDistance(ItemStack box) {
		if (box.getItem() instanceof PackageItem pi)
			return pi.hookDistance / 16f;
		return 1;
	}

	public static ItemStackHandler getContents(ItemStack box) {
		ItemStackHandler newInv = new ItemStackHandler(9);
		CompoundTag invNBT = box.getOrCreateTagElement("Items");
		if (!invNBT.isEmpty())
			newInv.deserializeNBT(invNBT);
		return newInv;
	}

	public static PackageItem randomBox() {
		return ALL_BOXES.get(new Random().nextInt(ALL_BOXES.size()));
	}

	@Override
	public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents,
		TooltipFlag pIsAdvanced) {
		super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
		CompoundTag compoundnbt = pStack.getOrCreateTag();

		if (compoundnbt.contains("Address", Tag.TAG_STRING) && !compoundnbt.getString("Address")
			.isBlank())
			pTooltipComponents.add(Components.literal("-> " + compoundnbt.getString("Address"))
				.withStyle(ChatFormatting.GOLD));

		/*
		 * Debug Fragmentation Data if (compoundnbt.contains("Fragment")) { CompoundTag
		 * fragTag = compoundnbt.getCompound("Fragment");
		 * pTooltipComponents.add(Components.literal("Order Information (Temporary)")
		 * .withStyle(ChatFormatting.GREEN)); pTooltipComponents.add(Components
		 * .literal(" Link " + fragTag.getInt("LinkIndex") +
		 * (fragTag.getBoolean("IsFinalLink") ? " Final" : "") + " | Fragment " +
		 * fragTag.getInt("Index") + (fragTag.getBoolean("IsFinal") ? " Final" : ""))
		 * .withStyle(ChatFormatting.DARK_GREEN)); if (fragTag.contains("OrderContext"))
		 * pTooltipComponents.add(Components.literal("Has Context!")
		 * .withStyle(ChatFormatting.DARK_GREEN)); }
		 */

		if (!compoundnbt.contains("Items", Tag.TAG_COMPOUND))
			return;

		int visibleNames = 0;
		int skippedNames = 0;
		ItemStackHandler contents = getContents(pStack);
		for (int i = 0; i < contents.getSlots(); i++) {
			ItemStack itemstack = contents.getStackInSlot(i);
			if (itemstack.isEmpty())
				continue;
			if (visibleNames > 2) {
				skippedNames++;
				continue;
			}

			visibleNames++;
			pTooltipComponents.add(itemstack.getHoverName()
				.copy()
				.append(" x")
				.append(String.valueOf(itemstack.getCount()))
				.withStyle(ChatFormatting.GRAY));
		}

		if (skippedNames > 0)
			pTooltipComponents.add(Components.translatable("container.shulkerBox.more", skippedNames)
				.withStyle(ChatFormatting.ITALIC));
	}

	// Throwing stuff

	@Override
	public int getUseDuration(ItemStack p_77626_1_) {
		return 72000;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack pStack) {
		return UseAnim.BOW;
	}

	public InteractionResultHolder<ItemStack> open(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack box = playerIn.getItemInHand(handIn);
		ItemStackHandler contents = getContents(box);
		ItemStack particle = box.copy();

		box = box.copyWithCount(box.getCount() - 1);
		if (box.isEmpty())
			box = ItemStack.EMPTY;

		playerIn.setItemInHand(handIn, box);

		if (!worldIn.isClientSide()) {
			for (int i = 0; i < contents.getSlots(); i++) {
				ItemStack itemstack = contents.getStackInSlot(i);

				if (itemstack.getItem() instanceof SpawnEggItem sei && worldIn instanceof ServerLevel sl) {
					EntityType<?> entitytype = sei.getType(itemstack.getTag());
					Entity entity = entitytype.spawn(sl, itemstack, null, BlockPos.containing(playerIn.position()
						.add(playerIn.getLookAngle()
							.multiply(1, 0, 1)
							.normalize())),
						MobSpawnType.SPAWN_EGG, false, false);
					if (entity != null)
						itemstack.shrink(1);
				}

				if (itemstack.isEmpty())
					continue;
				playerIn.getInventory()
					.placeItemBackInInventory(itemstack);
			}
		}

		Vec3 position = playerIn.position();
		worldIn.playSound((Player) null, position.x, position.y, position.z, SoundEvents.ARMOR_STAND_BREAK,
			SoundSource.PLAYERS, 0.5F, 1.0F);

		if (worldIn.isClientSide()) {
			for (int i = 0; i < 10; i++) {
				Vec3 motion = VecHelper.offsetRandomly(Vec3.ZERO, worldIn.getRandom(), .125f);
				Vec3 pos = position.add(0, 0.5, 0)
					.add(playerIn.getLookAngle()
						.scale(.5))
					.add(motion.scale(4));
				worldIn.addParticle(new ItemParticleOption(ParticleTypes.ITEM, particle), pos.x, pos.y, pos.z, motion.x,
					motion.y, motion.z);
			}
		}

		return new InteractionResultHolder<>(InteractionResult.SUCCESS, playerIn.getItemInHand(handIn));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (context.getPlayer()
			.isShiftKeyDown())
			return open(context.getLevel(), context.getPlayer(), context.getHand()).getResult();

		Vec3 point = context.getClickLocation();
		float h = height / 16f;
		float r = width / 2f / 16f;

		if (context.getClickedFace() == Direction.DOWN)
			point = point.subtract(0, h + .25f, 0);
		else if (context.getClickedFace()
			.getAxis()
			.isHorizontal())
			point = point.add(Vec3.atLowerCornerOf(context.getClickedFace()
				.getNormal())
				.scale(r));

		AABB scanBB = new AABB(point, point).inflate(r, 0, r)
			.expandTowards(0, h, 0);
		Level world = context.getLevel();
		if (!world.getEntities(AllEntityTypes.PACKAGE.get(), scanBB, e -> true)
			.isEmpty())
			return super.useOn(context);

		PackageEntity packageEntity = new PackageEntity(world, point.x, point.y, point.z);
		ItemStack itemInHand = context.getItemInHand();
		packageEntity.setBox(itemInHand.copy());
		world.addFreshEntity(packageEntity);
		itemInHand.shrink(1);
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		if (player.isShiftKeyDown())
			return open(world, player, hand);
		ItemStack itemstack = player.getItemInHand(hand);
		player.startUsingItem(hand);
		return InteractionResultHolder.success(itemstack);
	}

	@Override
	public void releaseUsing(ItemStack stack, Level world, LivingEntity entity, int ticks) {
		if (!(entity instanceof Player))
			return;
		Player playerentity = (Player) entity;
		int i = this.getUseDuration(stack) - ticks;
		if (i < 0)
			return;

		float f = getPackageVelocity(i);
		if (f < 0.1D)
			return;
		if (world.isClientSide)
			return;

		world.playSound(null, playerentity.getX(), playerentity.getY(), playerentity.getZ(), SoundEvents.SNOWBALL_THROW,
			SoundSource.NEUTRAL, 0.5F, 0.5F);

		ItemStack copy = stack.copy();
		stack.shrink(1);

		if (stack.isEmpty())
			playerentity.getInventory()
				.removeItem(stack);

		Vec3 vec = new Vec3(entity.getX(), entity.getY() + entity.getBoundingBox()
			.getYsize() / 2f, entity.getZ());
		Vec3 motion = entity.getLookAngle()
			.scale(f * 2);
		vec = vec.add(motion);

		PackageEntity packageEntity = new PackageEntity(world, vec.x, vec.y, vec.z);
		packageEntity.setBox(copy);
		packageEntity.setDeltaMovement(motion);
		world.addFreshEntity(packageEntity);
	}

	public static float getPackageVelocity(int p_185059_0_) {
		float f = (float) p_185059_0_ / 20.0F;
		f = (f * f + f * 2.0F) / 3.0F;
		if (f > 1.0F)
			f = 1.0F;
		return f;
	}

}