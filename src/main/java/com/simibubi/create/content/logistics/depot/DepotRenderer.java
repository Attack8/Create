package com.simibubi.create.content.logistics.depot;

import java.util.Random;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DepotRenderer extends SafeBlockEntityRenderer<DepotBlockEntity> {

	public DepotRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(DepotBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		renderItemsOf(be, partialTicks, ms, buffer, light, overlay, be.depotBehaviour);
	}

	public static void renderItemsOf(SmartBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay, DepotBehaviour depotBehaviour) {

		TransportedItemStack transported = depotBehaviour.heldItem;
		var msr = TransformStack.of(ms);
		Vec3 itemPosition = VecHelper.getCenterOf(be.getBlockPos());

		ms.pushPose();
		ms.translate(.5f, 15 / 16f, .5f);

		if (transported != null)
			depotBehaviour.incoming.add(transported);

		// Render main items
		for (TransportedItemStack tis : depotBehaviour.incoming) {
			ms.pushPose();
			msr.nudge(0);
			float offset = Mth.lerp(partialTicks, tis.prevBeltPosition, tis.beltPosition);
			float sideOffset = Mth.lerp(partialTicks, tis.prevSideOffset, tis.sideOffset);

			if (tis.insertedFrom.getAxis()
				.isHorizontal()) {
				Vec3 offsetVec = Vec3.atLowerCornerOf(tis.insertedFrom.getOpposite()
					.getNormal())
					.scale(.5f - offset);
				ms.translate(offsetVec.x, offsetVec.y, offsetVec.z);
				boolean alongX = tis.insertedFrom.getClockWise()
					.getAxis() == Direction.Axis.X;
				if (!alongX)
					sideOffset *= -1;
				ms.translate(alongX ? sideOffset : 0, 0, alongX ? 0 : sideOffset);
			}

			ItemStack itemStack = tis.stack;
			int angle = tis.angle;
			Random r = new Random(0);
			renderItem(be.getLevel(), ms, buffer, light, overlay, itemStack, angle, r, itemPosition, false);
			ms.popPose();
		}

		if (transported != null)
			depotBehaviour.incoming.remove(transported);

		// Render output items
		for (int i = 0; i < depotBehaviour.processingOutputBuffer.getSlots(); i++) {
			ItemStack stack = depotBehaviour.processingOutputBuffer.getStackInSlot(i);
			if (stack.isEmpty())
				continue;
			ms.pushPose();
			msr.nudge(i);

			boolean renderUpright = BeltHelper.isItemUpright(stack);
			msr.rotateYDegrees(360 / 8f * i);
			ms.translate(.35f, 0, 0);
			if (renderUpright)
				msr.rotateYDegrees(-(360 / 8f * i));
			Random r = new Random(i + 1);
			int angle = (int) (360 * r.nextFloat());
			renderItem(be.getLevel(), ms, buffer, light, overlay, stack, renderUpright ? angle + 90 : angle, r,
				itemPosition, false);
			ms.popPose();
		}

		ms.popPose();
	}

	public static void renderItem(Level level, PoseStack ms, MultiBufferSource buffer, int light, int overlay,
		ItemStack itemStack, int angle, Random r, Vec3 itemPosition, boolean alwaysUpright) {
		ItemRenderer itemRenderer = Minecraft.getInstance()
			.getItemRenderer();
		var msr = TransformStack.of(ms);
		int count = (int) (Mth.log2((int) (itemStack.getCount()))) / 2;
		BakedModel bakedModel = itemRenderer.getModel(itemStack, null, null, 0);
		boolean blockItem = bakedModel.isGui3d();
		boolean renderUpright = BeltHelper.isItemUpright(itemStack) || alwaysUpright && !blockItem;

		ms.pushPose();
		msr.rotateYDegrees(angle);

		if (renderUpright) {
			Entity renderViewEntity = Minecraft.getInstance().cameraEntity;
			if (renderViewEntity != null) {
				Vec3 positionVec = renderViewEntity.position();
				Vec3 vectorForOffset = itemPosition;
				Vec3 diff = vectorForOffset.subtract(positionVec);
				float yRot = (float) (Mth.atan2(diff.x, diff.z) + Math.PI);
				ms.mulPose(Axis.YP.rotation(yRot));
			}
			ms.translate(0, 3 / 32d, -1 / 16f);
		}

		for (int i = 0; i <= count; i++) {
			ms.pushPose();
			if (blockItem && r != null)
				ms.translate(r.nextFloat() * .0625f * i, 0, r.nextFloat() * .0625f * i);

			if (PackageItem.isPackage(itemStack) && !alwaysUpright) {
				ms.translate(0, 4 / 16f, 0);
				ms.scale(1.5f, 1.5f, 1.5f);
			} else if (blockItem && alwaysUpright) {
				ms.translate(0, 1 / 16f, 0);
				ms.scale(.755f, .755f, .755f);
			} else
				ms.scale(.5f, .5f, .5f);

			if (!blockItem && !renderUpright) {
				ms.translate(0, -3 / 16f, 0);
				msr.rotateXDegrees(90);
			}
			itemRenderer.render(itemStack, ItemDisplayContext.FIXED, false, ms, buffer, light, overlay, bakedModel);
			ms.popPose();

			if (!renderUpright) {
				if (!blockItem)
					msr.rotateYDegrees(10);
				ms.translate(0, blockItem ? 1 / 64d : 1 / 16d, 0);
			} else
				ms.translate(0, 0, -1 / 16f);
		}

		ms.popPose();
	}

}
