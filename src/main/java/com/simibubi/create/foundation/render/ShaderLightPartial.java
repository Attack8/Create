package com.simibubi.create.foundation.render;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.material.LightShader;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.material.LightShaders;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import dev.engine_room.flywheel.lib.model.ModelUtil;
import dev.engine_room.flywheel.lib.model.baked.BakedModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.util.RendererReloadCache;
import net.minecraft.client.renderer.RenderType;

public class ShaderLightPartial {
	private static final RendererReloadCache<PartialModel, Model> FLAT = new RendererReloadCache<>(it -> new BakedModelBuilder(it.get())
		.materialFunc((renderType, aBoolean) -> getMaterial(renderType, aBoolean, LightShaders.FLAT))
		.build());

	private static final RendererReloadCache<PartialModel, Model> SMOOTH = new RendererReloadCache<>(it -> new BakedModelBuilder(it.get())
		.materialFunc((renderType, aBoolean) -> getMaterial(renderType, aBoolean, LightShaders.SMOOTH))
		.build());

	public static Model flat(PartialModel partial) {
		return FLAT.get(partial);
	}

	@Nullable
	private static SimpleMaterial getMaterial(RenderType renderType, Boolean aBoolean, LightShader lightShader) {
		var material = ModelUtil.getMaterial(renderType, aBoolean);
		if (material == null) {
			return null;
		}
		return SimpleMaterial.builderOf(material)
			.light(lightShader)
			.build();
	}
}
