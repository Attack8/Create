package com.simibubi.create.foundation.mixin.accessor;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.neoforged.neoforge.client.model.generators.ModelBuilder;

@Mixin(ModelBuilder.class)
public interface ModelBuilderAccessor {
	@Accessor(value = "textures", remap = false)
	Map<String, String> create$getTextures();
}
