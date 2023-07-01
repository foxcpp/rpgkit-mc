package com.github.sweetsnowywitch.rpgkit.magic;

import net.minecraft.util.math.Vec3d;

public enum EffectVector {
    TOWARDS_ORIGIN,
    FROM_ORIGIN,
    FORWARD,
    COUNTERCLOCKWISE,
    CLOCKWISE,
    UP,
    DOWN,
    ZERO;

    public Vec3d direction(Vec3d target, Vec3d origin, Vec3d direction) {
        return switch (this) {
            case TOWARDS_ORIGIN -> origin.subtract(target);
            case FROM_ORIGIN -> target.subtract(origin);
            case FORWARD -> direction;
            case COUNTERCLOCKWISE -> target.subtract(origin).crossProduct(new Vec3d(0, -1, 0));
            case CLOCKWISE -> target.subtract(origin).crossProduct(new Vec3d(0, 1, 0));
            case UP -> new Vec3d(0, 0.5, 0);
            case DOWN -> new Vec3d(0, -1, 0);
            case ZERO -> Vec3d.ZERO;
        };
    }
}
