package com.github.sweetsnowywitch.csmprpgkit;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class MovementUtils {
    public static Vec3d rotationVector(float pitch, float yaw) {
        float f = pitch * ((float) Math.PI / 180);
        float g = -yaw * ((float) Math.PI / 180);
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d(i * j, -k, h * j);
    }
}
