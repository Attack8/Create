package com.simibubi.create.content.kinetics.base;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import dev.engine_room.flywheel.api.instance.InstanceHandle;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.lib.instance.ColoredLitInstance;
import net.createmod.catnip.utility.theme.Color;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

public class RotatingInstance extends ColoredLitInstance {
	public static final float SPEED_MULTIPLIER = 6;

    public byte rotationAxisX;
    public byte rotationAxisY;
    public byte rotationAxisZ;
	public float x;
	public float y;
	public float z;
	/**
	 * Speed in degrees per second
	 */
	public float rotationalSpeed;
	/**
	 * Offset in degrees
	 */
	public float rotationOffset;

	/**
	 * Base rotation of the instance, applied before kinetic rotation
	 */
	public final Quaternionf rotation = new Quaternionf();

	public RotatingInstance(InstanceType<? extends RotatingInstance> type, InstanceHandle handle) {
		super(type, handle);
	}

	public static int colorFromBE(KineticBlockEntity be) {
		if (be.hasNetwork())
			return Color.generateFromLong(be.network).getRGB();
		return 0xFFFFFF;
	}

	public RotatingInstance setRotationAxis(Direction.Axis axis) {
        Direction orientation = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        return setRotationAxis(orientation.step());
    }

    public RotatingInstance setRotationAxis(Vector3f axis) {
        return setRotationAxis(axis.x(), axis.y(), axis.z());
	}

	public RotatingInstance setRotationAxis(float rotationAxisX, float rotationAxisY, float rotationAxisZ) {
		this.rotationAxisX = (byte) (rotationAxisX * 127);
		this.rotationAxisY = (byte) (rotationAxisY * 127);
		this.rotationAxisZ = (byte) (rotationAxisZ * 127);
		return this;
	}

	public RotatingInstance setPosition(Vec3i pos) {
        return setPosition(pos.getX(), pos.getY(), pos.getZ());
    }

	public RotatingInstance setPosition(Vector3f pos) {
		return setPosition(pos.x(), pos.y(), pos.z());
	}

	public RotatingInstance setPosition(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	public RotatingInstance nudge(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public RotatingInstance setColor(KineticBlockEntity blockEntity) {
		colorRgb(colorFromBE(blockEntity));
		return this;
	}

	public RotatingInstance setColor(Color c) {
		color(c.getRed(), c.getGreen(), c.getBlue());
		return this;
	}

	public RotatingInstance setRotationalSpeed(float rotationalSpeed) {
		this.rotationalSpeed = rotationalSpeed;
		return this;
	}

	public RotatingInstance setRotationOffset(float rotationOffset) {
		this.rotationOffset = rotationOffset;
		return this;
	}
}
