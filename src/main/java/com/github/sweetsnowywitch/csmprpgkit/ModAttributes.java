package com.github.sweetsnowywitch.csmprpgkit;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModAttributes {
    public static final EntityAttribute MAX_MANA = make("max_mana", 0, 0, 2000);
    public static final EntityAttribute MANA_REGEN = make("mana_regen", 1, 0, 200);
    public static final EntityAttribute MANA_REGEN_SPEED = make("mana_regen_speed", 0.005, 0, 1);
    public static final EntityAttribute MANA_HEALTH_FACTOR = make("mana_health_factor", 10, 0, 10);

    private static EntityAttribute make(final String name, final double base, final double min, final double max) {
        return new ClampedEntityAttribute("attribute.name.player." + RPGKitMod.MOD_ID + '.' + name, base, min, max).setTracked(true);
    }

    public static void register() {
        Registry.register(Registry.ATTRIBUTE, new Identifier(RPGKitMod.MOD_ID, "max_mana"), MAX_MANA);
        Registry.register(Registry.ATTRIBUTE, new Identifier(RPGKitMod.MOD_ID, "mana_regen"), MANA_REGEN);
        Registry.register(Registry.ATTRIBUTE, new Identifier(RPGKitMod.MOD_ID, "mana_regen_speed"), MANA_REGEN_SPEED);
        Registry.register(Registry.ATTRIBUTE, new Identifier(RPGKitMod.MOD_ID, "mana_health_factor"), MANA_HEALTH_FACTOR);

        FabricDefaultAttributeRegistry.register(EntityType.PLAYER,
                PlayerEntity.createPlayerAttributes().
                        add(MAX_MANA).
                        add(MANA_REGEN).
                        add(MANA_REGEN_SPEED).
                        add(MANA_HEALTH_FACTOR));
    }
}
