package com.simibubi.create.content.processing.burner;

import org.joml.Quaternionf;
import org.joml.Quaternionfc;

import dev.engine_room.flywheel.api.instance.InstanceHandle;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.lib.instance.ColoredLitInstance;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.minecraft.core.Vec3i;

public class ScrollInstance extends ColoredLitInstance {
	public float x;
	public float y;
	public float z;
	public final Quaternionf rotation = new Quaternionf();

	public float speedU;
	public float speedV;

	public float offsetU;
	public float offsetV;

	public float diffU;
	public float diffV;

	public float scaleU;
	public float scaleV;

	public ScrollInstance(InstanceType<? extends ColoredLitInstance> type, InstanceHandle handle) {
		super(type, handle);
	}

	public ScrollInstance position(Vec3i position) {
		this.x = position.getX();
		this.y = position.getY();
		this.z = position.getZ();
		return this;
	}

	public ScrollInstance position(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	public ScrollInstance shift(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public ScrollInstance rotation(Quaternionfc rotation) {
		this.rotation.set(rotation);
		return this;
	}

	public ScrollInstance setSpriteShift(SpriteShiftEntry spriteShift) {
		float spriteWidth = spriteShift.getTarget()
			.getU1()
			- spriteShift.getTarget()
			.getU0();

		float spriteHeight = spriteShift.getTarget()
			.getV1()
			- spriteShift.getTarget()
			.getV0();

		scaleU = spriteWidth / 2;
		scaleV = spriteHeight / 2;

		diffU = spriteShift.getTarget().getU0() - spriteShift.getOriginal().getU0();
		diffV = spriteShift.getTarget().getV0() - spriteShift.getOriginal().getV0();

		return this;
	}
}
