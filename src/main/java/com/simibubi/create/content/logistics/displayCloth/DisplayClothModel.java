package com.simibubi.create.content.logistics.displayCloth;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.model.BakedModelWrapperWithData;
import com.simibubi.create.foundation.model.BakedQuadHelper;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.createmod.catnip.utility.Iterate;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelData.Builder;
import net.minecraftforge.client.model.data.ModelProperty;

public class DisplayClothModel extends BakedModelWrapperWithData {

	private static final ModelProperty<CullData> CULL_PROPERTY = new ModelProperty<>();

	private static final Map<DisplayClothBlock, List<List<BakedQuad>>> CORNERS = new HashMap<>();

	public DisplayClothModel(BakedModel originalModel) {
		super(originalModel);
	}

	public static void reload() {
		CORNERS.clear();
	}
	
	@Override
	public boolean useAmbientOcclusion() {
		return false;
	}

	private List<BakedQuad> getCorner(DisplayClothBlock block, int corner, @NotNull RandomSource rand,
		@Nullable RenderType renderType) {

		List<List<BakedQuad>> corners = CORNERS.computeIfAbsent(block, b -> {
			TextureAtlasSprite targetSprite = getParticleIcon(ModelData.EMPTY);
			List<List<BakedQuad>> list = new ArrayList<>();

			for (PartialModel pm : List.of(AllPartialModels.DISPLAY_CLOTH_SW, AllPartialModels.DISPLAY_CLOTH_NW,
				AllPartialModels.DISPLAY_CLOTH_NE, AllPartialModels.DISPLAY_CLOTH_SE)) {
				List<BakedQuad> quads = new ArrayList<>();

				for (BakedQuad quad : pm.get()
					.getQuads(null, null, rand, ModelData.EMPTY, renderType)) {
					TextureAtlasSprite original = quad.getSprite();
					BakedQuad newQuad = BakedQuadHelper.clone(quad);
					int[] vertexData = newQuad.getVertices();
					for (int vertex = 0; vertex < 4; vertex++) {
						BakedQuadHelper.setU(vertexData, vertex, targetSprite.getU(
							SpriteShiftEntry.getUnInterpolatedU(original, BakedQuadHelper.getU(vertexData, vertex))));
						BakedQuadHelper.setV(vertexData, vertex, targetSprite.getV(
							SpriteShiftEntry.getUnInterpolatedV(original, BakedQuadHelper.getV(vertexData, vertex))));
					}
					quads.add(newQuad);
				}
				list.add(quads);
			}
			return list;
		});

		return corners.get(corner);
	}

	@Override
	protected Builder gatherModelData(Builder builder, BlockAndTintGetter world, BlockPos pos, BlockState state,
		ModelData blockEntityData) {
		List<Direction> culledSides = new ArrayList<>();
		for (Direction side : Iterate.horizontalDirections)
			if (!Block.shouldRenderFace(state, world, pos, side, pos.relative(side)))
				culledSides.add(side);
		if (culledSides.isEmpty())
			return builder;
		return builder.with(CULL_PROPERTY, new CullData(EnumSet.copyOf(culledSides)));
	}

	@Override
	public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
		@NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
		@NotNull
		List<BakedQuad> mainQuads = super.getQuads(state, side, rand, extraData, renderType);
		if (side == null || side.getAxis() == Axis.Y)
			return mainQuads;

		CullData cullData = extraData.get(CULL_PROPERTY);
		if (cullData != null && cullData.culled()
			.contains(side.getClockWise()))
			return mainQuads;
		if (state == null || !(state.getBlock() instanceof DisplayClothBlock dcb))
			return mainQuads;

		List<BakedQuad> copyOf = new ArrayList<>(mainQuads);
		copyOf.addAll(getCorner(dcb, side.get2DDataValue(), rand, renderType));
		return copyOf;
	}

	private static record CullData(EnumSet<Direction> culled) {
	}

}
