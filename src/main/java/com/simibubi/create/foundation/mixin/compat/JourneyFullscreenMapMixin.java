package com.simibubi.create.foundation.mixin.compat;

import java.awt.geom.Point2D;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.compat.trainmap.JourneyTrainMap;

import journeymap.client.render.map.GridRenderer;
import journeymap.client.ui.fullscreen.Fullscreen;
import net.minecraft.client.gui.GuiGraphics;

@Mixin(Fullscreen.class)
public abstract class JourneyFullscreenMapMixin {
	@Final
	@Shadow(remap = false)
	private static GridRenderer gridRenderer;

	@Shadow(remap = false)
	private Boolean isScrolling;

	@Shadow(remap = false)
	protected abstract Point2D.Double getMouseDrag();

	@Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At(target = "Ljourneymap/client/ui/fullscreen/Fullscreen;drawMap(Lnet/minecraft/client/gui/GuiGraphics;II)V", value = "INVOKE", shift = Shift.AFTER))
	public void create$journeyMapFullscreenRender(GuiGraphics graphics, int mouseX, int mouseY, float pt,
		CallbackInfo ci) {
		boolean dragging = isScrolling;
		Point2D.Double mouseDrag = getMouseDrag();
		double x = gridRenderer.getCenterBlockX() - (dragging ? mouseDrag.x : 0);
		double z = gridRenderer.getCenterBlockZ() - (dragging ? mouseDrag.y : 0);
		JourneyTrainMap.onRender(graphics, (Fullscreen) (Object) this, x, z, mouseX, mouseY, pt);
	}
}
