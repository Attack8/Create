package com.simibubi.create.content.logistics.box;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.utility.math.AngleHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

public class PackageRenderer extends EntityRenderer<PackageEntity> {

	public PackageRenderer(Context pContext) {
		super(pContext);
	}

	@Override
	public void render(PackageEntity entity, float yaw, float pt, PoseStack ms, MultiBufferSource buffer, int light) {
		ItemStack box = entity.box;
		if (box.isEmpty())
			box = PackageItem.getFallbackBox();
		PartialModel model = AllPartialModels.PACKAGES.get(ForgeRegistries.ITEMS.getKey(box.getItem()));
		SuperByteBuffer sbb = CachedBuffers.partial(model, Blocks.AIR.defaultBlockState());
		sbb.translate(-.5, 0, -.5)
			.rotateCentered(AngleHelper.rad(yaw), Direction.UP)
			.light(light)
			.nudge(entity.getId());
		sbb.renderInto(ms, buffer.getBuffer(RenderType.solid()));
		super.render(entity, yaw, pt, ms, buffer, light);
	}

	@Override
	public ResourceLocation getTextureLocation(PackageEntity pEntity) {
		return null;
	}

}
