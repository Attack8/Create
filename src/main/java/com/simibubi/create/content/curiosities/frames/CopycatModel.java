package com.simibubi.create.content.curiosities.frames;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.model.BakedModelWrapperWithData;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap.Builder;
import net.minecraftforge.client.model.data.ModelProperty;

public abstract class CopycatModel extends BakedModelWrapperWithData {

	public static final ModelProperty<BlockState> MATERIAL_PROPERTY = new ModelProperty<>();
	private static final ModelProperty<OcclusionData> OCCLUSION_PROPERTY = new ModelProperty<>();
	private static final ModelProperty<IModelData> WRAPPED_DATA_PROPERTY = new ModelProperty<>();

	public CopycatModel(BakedModel originalModel) {
		super(originalModel);
	}

	@Override
	protected void gatherModelData(Builder builder, BlockAndTintGetter world, BlockPos pos, BlockState state,
		IModelData blockEntityData) {
		BlockState material = getMaterial(blockEntityData);
		if (material == null)
			return;

		builder.withInitial(MATERIAL_PROPERTY, material);

		OcclusionData occlusionData = new OcclusionData();
		if (state.getBlock() instanceof CopycatBlock copycatBlock) {
			MutableBlockPos mutablePos = new MutableBlockPos();
			for (Direction face : Iterate.directions)
				if (copycatBlock.canFaceBeOccluded(state, face))
					if (!Block.shouldRenderFace(material, world, pos, face, mutablePos.setWithOffset(pos, face)))
						occlusionData.occlude(face);
			builder.withInitial(OCCLUSION_PROPERTY, occlusionData);
		}

		IModelData wrappedData = getModelOf(material).getModelData(world, pos, material, EmptyModelData.INSTANCE);
		builder.withInitial(WRAPPED_DATA_PROPERTY, wrappedData);
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData data) {
		BlockState material = getMaterial(data);

		if (material == null)
			return super.getQuads(state, side, rand, data);

		OcclusionData occlusionData = data.getData(OCCLUSION_PROPERTY);
		if (occlusionData != null && occlusionData.isOccluded(side))
			return super.getQuads(state, side, rand, data);

		RenderType renderType = MinecraftForgeClient.getRenderType();
		if (renderType != null && !ItemBlockRenderTypes.canRenderInLayer(material, renderType))
			return super.getQuads(state, side, rand, data);

		IModelData wrappedData = data.getData(WRAPPED_DATA_PROPERTY);
		if (wrappedData == null)
			wrappedData = EmptyModelData.INSTANCE;

		return getCroppedQuads(state, side, rand, material, wrappedData);
	}

	protected abstract List<BakedQuad> getCroppedQuads(BlockState state, Direction side, Random rand,
		BlockState material, IModelData wrappedData);

	@Override
	public TextureAtlasSprite getParticleIcon(IModelData data) {
		BlockState material = getMaterial(data);

		if (material == null)
			return super.getParticleIcon(data);

		IModelData wrappedData = data.getData(WRAPPED_DATA_PROPERTY);
		if (wrappedData == null)
			wrappedData = EmptyModelData.INSTANCE;

		return getModelOf(material).getParticleIcon(wrappedData);
	}

	@Nullable
	public static BlockState getMaterial(IModelData data) {
		BlockState material = data.getData(MATERIAL_PROPERTY);
		return material == null ? AllBlocks.COPYCAT_BASE.getDefaultState() : material;
	}

	public static BakedModel getModelOf(BlockState state) {
		return Minecraft.getInstance()
			.getBlockRenderer()
			.getBlockModel(state);
	}

	private static class OcclusionData {
		private final boolean[] occluded;

		public OcclusionData() {
			occluded = new boolean[6];
		}

		public void occlude(Direction face) {
			occluded[face.get3DDataValue()] = true;
		}

		public boolean isOccluded(Direction face) {
			return face == null ? false : occluded[face.get3DDataValue()];
		}
	}

}
