package com.simibubi.create.foundation.mixin.accessor;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.LevelRenderer;

import net.minecraft.client.renderer.culling.Frustum;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {
	@Accessor("cullingFrustum")
	Frustum create$getCullingFrustum();

	@Nullable
	@Accessor("capturedFrustum")
	Frustum create$getCapturedFrustum();
}
