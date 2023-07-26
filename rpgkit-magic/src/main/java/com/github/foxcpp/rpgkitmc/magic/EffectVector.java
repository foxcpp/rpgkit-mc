package com.github.foxcpp.rpgkitmc.magic;

import com.github.foxcpp.rpgkitmc.JsonHelpers;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public enum EffectVector implements JsonHelpers.JsonElementSerializable {
    TOWARDS_ORIGIN,
    FROM_ORIGIN,
    FORWARD,
    COUNTERCLOCKWISE,
    CLOCKWISE,
    UP,
    DOWN,
    ZERO;

    public static final TrackedDataHandler<EffectVector> TRACKED_HANDLER = TrackedDataHandler.ofEnum(EffectVector.class);
    public static final TrackedDataHandler<Optional<EffectVector>> OPTIONAL_TRACKED_HANDLER = TrackedDataHandler.ofOptional(TRACKED_HANDLER::write, TRACKED_HANDLER::read);

    public Vec3d direction(Vec3d target, Vec3d origin, Vec3d spellDirection) {
        return switch (this) {
            case TOWARDS_ORIGIN -> origin.subtract(target);
            case FROM_ORIGIN -> target.subtract(origin);
            case FORWARD -> spellDirection;
            case COUNTERCLOCKWISE -> target.subtract(origin).crossProduct(new Vec3d(0, -1, 0));
            case CLOCKWISE -> target.subtract(origin).crossProduct(new Vec3d(0, 1, 0));
            case UP -> new Vec3d(0, 0.5, 0);
            case DOWN -> new Vec3d(0, -1, 0);
            case ZERO -> Vec3d.ZERO;
        };
    }

    public static EffectVector fromJson(JsonElement el) {
        if (el instanceof JsonPrimitive) {
            return EffectVector.valueOf(el.getAsString().toUpperCase());
        } else {
            throw new IllegalArgumentException("effect vector value is not a string");
        }
    }

    @Override
    public @NotNull JsonElement toJson() {
        return new JsonPrimitive(this.name().toLowerCase());
    }
}
