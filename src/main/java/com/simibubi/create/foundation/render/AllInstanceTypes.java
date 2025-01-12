package com.simibubi.create.foundation.render;

import static com.simibubi.create.Create.asResource;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.lwjgl.system.MemoryUtil;

import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.processing.burner.ScrollInstance;

import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.layout.FloatRepr;
import dev.engine_room.flywheel.api.layout.IntegerRepr;
import dev.engine_room.flywheel.api.layout.LayoutBuilder;
import dev.engine_room.flywheel.lib.instance.SimpleInstanceType;
import dev.engine_room.flywheel.lib.util.ExtraMemoryOps;

@OnlyIn(Dist.CLIENT)
public class AllInstanceTypes {
	public static final InstanceType<RotatingInstance> ROTATING = SimpleInstanceType.builder(RotatingInstance::new)
			.cullShader(asResource("instance/cull/rotating.glsl"))
			.vertexShader(asResource("instance/rotating.vert"))
			.layout(LayoutBuilder.create()
					.vector("color", FloatRepr.NORMALIZED_UNSIGNED_BYTE, 4)
					.vector("light", IntegerRepr.SHORT, 2)
					.vector("overlay", IntegerRepr.SHORT, 2)
					.vector("rotation", FloatRepr.FLOAT, 4)
					.vector("pos", FloatRepr.FLOAT, 3)
					.scalar("speed", FloatRepr.FLOAT)
					.scalar("offset", FloatRepr.FLOAT)
					.vector("axis", FloatRepr.NORMALIZED_BYTE, 3)
					.build())
			.writer((ptr, instance) -> {
				MemoryUtil.memPutByte(ptr, instance.red);
				MemoryUtil.memPutByte(ptr + 1, instance.green);
				MemoryUtil.memPutByte(ptr + 2, instance.blue);
				MemoryUtil.memPutByte(ptr + 3, instance.alpha);
				ExtraMemoryOps.put2x16(ptr + 4, instance.light);
				ExtraMemoryOps.put2x16(ptr + 8, instance.overlay);
				ExtraMemoryOps.putQuaternionf(ptr + 12, instance.rotation);
				MemoryUtil.memPutFloat(ptr + 28, instance.x);
				MemoryUtil.memPutFloat(ptr + 32, instance.y);
				MemoryUtil.memPutFloat(ptr + 36, instance.z);
				MemoryUtil.memPutFloat(ptr + 40, instance.rotationalSpeed);
				MemoryUtil.memPutFloat(ptr + 44, instance.rotationOffset);
				MemoryUtil.memPutByte(ptr + 48, instance.rotationAxisX);
				MemoryUtil.memPutByte(ptr + 49, instance.rotationAxisY);
				MemoryUtil.memPutByte(ptr + 50, instance.rotationAxisZ);
			})
			.build();

	public static final InstanceType<ScrollInstance> SCROLLING = SimpleInstanceType.builder(ScrollInstance::new)
			.cullShader(asResource("instance/cull/scrolling.glsl"))
			.vertexShader(asResource("instance/scrolling.vert"))
			.layout(LayoutBuilder.create()
					.vector("color", FloatRepr.NORMALIZED_UNSIGNED_BYTE, 4)
					.vector("light", IntegerRepr.SHORT, 2)
					.vector("overlay", IntegerRepr.SHORT, 2)
					.vector("pos", FloatRepr.FLOAT, 3)
					.vector("rotation", FloatRepr.FLOAT, 4)
					.vector("speed", FloatRepr.FLOAT, 2)
					.vector("diff", FloatRepr.FLOAT, 2)
					.vector("scale", FloatRepr.FLOAT, 2)
					.vector("offset", FloatRepr.FLOAT, 2)
					.build())
			.writer((ptr, instance) -> {
				MemoryUtil.memPutByte(ptr, instance.red);
				MemoryUtil.memPutByte(ptr + 1, instance.green);
				MemoryUtil.memPutByte(ptr + 2, instance.blue);
				MemoryUtil.memPutByte(ptr + 3, instance.alpha);
				ExtraMemoryOps.put2x16(ptr + 4, instance.light);
				ExtraMemoryOps.put2x16(ptr + 8, instance.overlay);
				MemoryUtil.memPutFloat(ptr + 12, instance.x);
				MemoryUtil.memPutFloat(ptr + 16, instance.y);
				MemoryUtil.memPutFloat(ptr + 20, instance.z);
				ExtraMemoryOps.putQuaternionf(ptr + 24, instance.rotation);
				MemoryUtil.memPutFloat(ptr + 40, instance.speedU);
				MemoryUtil.memPutFloat(ptr + 44, instance.speedV);
				MemoryUtil.memPutFloat(ptr + 48, instance.diffU);
				MemoryUtil.memPutFloat(ptr + 52, instance.diffV);
				MemoryUtil.memPutFloat(ptr + 56, instance.scaleU);
				MemoryUtil.memPutFloat(ptr + 60, instance.scaleV);
				MemoryUtil.memPutFloat(ptr + 64, instance.offsetU);
				MemoryUtil.memPutFloat(ptr + 68, instance.offsetV);
			})
			.build();

	public static void init() {
		// noop
	}
}
