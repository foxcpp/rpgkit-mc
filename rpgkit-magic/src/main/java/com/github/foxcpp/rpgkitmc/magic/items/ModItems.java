package com.github.foxcpp.rpgkitmc.magic.items;

import com.github.foxcpp.rpgkitmc.magic.RPGKitMagicMod;
import com.google.common.collect.ImmutableMap;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    // Technical items.
    public static final Item SPELL_ITEM = new SpellItem(new FabricItemSettings().maxCount(1));
    public static final Item MAGIC_FUEL = new MagicFuelItem(new FabricItemSettings().maxCount(1).fireproof());

    // Actual items.
    public static final Item CATALYST_BAG = new CatalystBagItem(new FabricItemSettings().maxCount(1));

    public static final Item COPPER_LAPIS_AMULET = new ManaAccessoryItem(
            new FabricItemSettings().maxCount(1).maxDamage(70), ImmutableMap.of(),
            30, 10, 0.002f
    );
    public static final Item COPPER_AMETHYST_AMULET = new ManaAccessoryItem(
            new FabricItemSettings().maxCount(1).maxDamage(70), ImmutableMap.of(),
            60, 10, 0.002f
    );
    public static final Item COPPER_QUARTZ_AMULET = new ManaAccessoryItem(
            new FabricItemSettings().maxCount(1).maxDamage(70), ImmutableMap.of(),
            80, 10, 0.003f
    );
    public static final Item COPPER_REDSTONE_AMULET = new ManaAccessoryItem(
            new FabricItemSettings().maxCount(1).maxDamage(70), ImmutableMap.of(),
            70, 10, 0.007f
    );
    public static final Item COPPER_DIAMOND_AMULET = new ManaAccessoryItem(
            new FabricItemSettings().maxCount(1).maxDamage(70), ImmutableMap.of(),
            90, 10, 0.003f
    );
    public static final Item COPPER_EMERALD_AMULET = new ManaAccessoryItem(
            new FabricItemSettings().maxCount(1).maxDamage(70), ImmutableMap.of(),
            110, 10, 0.003f
    );

    public static final Item IRON_LAPIS_AMULET = new ManaAccessoryItem(
            new FabricItemSettings().maxCount(1).maxDamage(150), ImmutableMap.of(),
            40, 10, 0.001f
    );
    public static final Item IRON_AMETHYST_AMULET = new ManaAccessoryItem(
            new FabricItemSettings().maxCount(1).maxDamage(150), ImmutableMap.of(),
            70, 10, 0.001f
    );
    public static final Item IRON_QUARTZ_AMULET = new ManaAccessoryItem(
            new FabricItemSettings().maxCount(1).maxDamage(150), ImmutableMap.of(),
            90, 10, 0.003f
    );
    public static final Item IRON_REDSTONE_AMULET = new ManaAccessoryItem(
            new FabricItemSettings().maxCount(1).maxDamage(150), ImmutableMap.of(),
            50, 10, 0.006f
    );
    public static final Item IRON_DIAMOND_AMULET = new ManaAccessoryItem(
            new FabricItemSettings().maxCount(1).maxDamage(150), ImmutableMap.of(),
            100, 10, 0.002f
    );
    public static final Item IRON_EMERALD_AMULET = new ManaAccessoryItem(
            new FabricItemSettings().maxCount(1).maxDamage(150), ImmutableMap.of(),
            120, 10, 0.002f
    );

    public static final Item GOLD_LAPIS_AMULET = new ManaAccessoryItem(
            new FabricItemSettings().maxCount(1).maxDamage(20), ImmutableMap.of(),
            50, 10, 0.002f
    );
    public static final Item GOLD_AMETHYST_AMULET = new ManaAccessoryItem(
            new FabricItemSettings().maxCount(1).maxDamage(20), ImmutableMap.of(),
            80, 10, 0.002f
    );
    public static final Item GOLD_QUARTZ_AMULET = new ManaAccessoryItem(
            new FabricItemSettings().maxCount(1).maxDamage(20), ImmutableMap.of(),
            90, 10, 0.006f
    );
    public static final Item GOLD_REDSTONE_AMULET = new ManaAccessoryItem(
            new FabricItemSettings().maxCount(1).maxDamage(20), ImmutableMap.of(),
            90, 10, 0.012f
    );
    public static final Item GOLD_DIAMOND_AMULET = new ManaAccessoryItem(
            new FabricItemSettings().maxCount(1).maxDamage(20), ImmutableMap.of(),
            100, 10, 0.004f
    );
    public static final Item GOLD_EMERALD_AMULET = new ManaAccessoryItem(
            new FabricItemSettings().maxCount(1).maxDamage(20), ImmutableMap.of(),
            120, 10, 0.002f
    );

    public static final Item NETHERITE_LAPIS_AMULET = new ManaAccessoryItem(
            new FabricItemSettings().maxCount(1).maxDamage(300), ImmutableMap.of(),
            70, 10, 0.001f
    );
    public static final Item NETHERITE_AMETHYST_AMULET = new ManaAccessoryItem(
            new FabricItemSettings().maxCount(1).maxDamage(300), ImmutableMap.of(),
            90, 10, 0.001f
    );
    public static final Item NETHERITE_QUARTZ_AMULET = new ManaAccessoryItem(
            new FabricItemSettings().maxCount(1).maxDamage(300), ImmutableMap.of(),
            100, 10, 0.003f
    );
    public static final Item NETHERITE_REDSTONE_AMULET = new ManaAccessoryItem(
            new FabricItemSettings().maxCount(1).maxDamage(300), ImmutableMap.of(),
            110, 10, 0.006f
    );
    public static final Item NETHERITE_DIAMOND_AMULET = new ManaAccessoryItem(
            new FabricItemSettings().maxCount(1).maxDamage(300), ImmutableMap.of(),
            120, 10, 0.002f
    );
    public static final Item NETHERITE_EMERALD_AMULET = new ManaAccessoryItem(
            new FabricItemSettings().maxCount(1).maxDamage(300), ImmutableMap.of(),
            140, 10, 0.002f
    );

    public static final ItemGroup MAGIC_TOOLING_GROUP = FabricItemGroup.builder(new Identifier(RPGKitMagicMod.MOD_ID, "magic_tooling"))
            .icon(() -> new ItemStack(COPPER_AMETHYST_AMULET))
            .build();

    public static void register() {
        Registry.register(Registries.ITEM, new Identifier(RPGKitMagicMod.MOD_ID, "spell"), SPELL_ITEM);
        Registry.register(Registries.ITEM, Identifier.of(RPGKitMagicMod.MOD_ID, "catalyst_bag"), CATALYST_BAG);
        Registry.register(Registries.ITEM, new Identifier(RPGKitMagicMod.MOD_ID, "magic_fuel"), MAGIC_FUEL);

        Registry.register(Registries.ITEM, new Identifier(RPGKitMagicMod.MOD_ID, "amulets/copper_lapis"), COPPER_LAPIS_AMULET);
        Registry.register(Registries.ITEM, new Identifier(RPGKitMagicMod.MOD_ID, "amulets/copper_amethyst"), COPPER_AMETHYST_AMULET);
        Registry.register(Registries.ITEM, new Identifier(RPGKitMagicMod.MOD_ID, "amulets/copper_quartz"), COPPER_QUARTZ_AMULET);
        Registry.register(Registries.ITEM, new Identifier(RPGKitMagicMod.MOD_ID, "amulets/copper_redstone"), COPPER_REDSTONE_AMULET);
        Registry.register(Registries.ITEM, new Identifier(RPGKitMagicMod.MOD_ID, "amulets/copper_diamond"), COPPER_DIAMOND_AMULET);
        Registry.register(Registries.ITEM, new Identifier(RPGKitMagicMod.MOD_ID, "amulets/copper_emerald"), COPPER_EMERALD_AMULET);
        Registry.register(Registries.ITEM, new Identifier(RPGKitMagicMod.MOD_ID, "amulets/iron_lapis"), IRON_LAPIS_AMULET);
        Registry.register(Registries.ITEM, new Identifier(RPGKitMagicMod.MOD_ID, "amulets/iron_amethyst"), IRON_AMETHYST_AMULET);
        Registry.register(Registries.ITEM, new Identifier(RPGKitMagicMod.MOD_ID, "amulets/iron_quartz"), IRON_QUARTZ_AMULET);
        Registry.register(Registries.ITEM, new Identifier(RPGKitMagicMod.MOD_ID, "amulets/iron_redstone"), IRON_REDSTONE_AMULET);
        Registry.register(Registries.ITEM, new Identifier(RPGKitMagicMod.MOD_ID, "amulets/iron_diamond"), IRON_DIAMOND_AMULET);
        Registry.register(Registries.ITEM, new Identifier(RPGKitMagicMod.MOD_ID, "amulets/iron_emerald"), IRON_EMERALD_AMULET);
        Registry.register(Registries.ITEM, new Identifier(RPGKitMagicMod.MOD_ID, "amulets/gold_lapis"), GOLD_LAPIS_AMULET);
        Registry.register(Registries.ITEM, new Identifier(RPGKitMagicMod.MOD_ID, "amulets/gold_amethyst"), GOLD_AMETHYST_AMULET);
        Registry.register(Registries.ITEM, new Identifier(RPGKitMagicMod.MOD_ID, "amulets/gold_quartz"), GOLD_QUARTZ_AMULET);
        Registry.register(Registries.ITEM, new Identifier(RPGKitMagicMod.MOD_ID, "amulets/gold_redstone"), GOLD_REDSTONE_AMULET);
        Registry.register(Registries.ITEM, new Identifier(RPGKitMagicMod.MOD_ID, "amulets/gold_diamond"), GOLD_DIAMOND_AMULET);
        Registry.register(Registries.ITEM, new Identifier(RPGKitMagicMod.MOD_ID, "amulets/gold_emerald"), GOLD_EMERALD_AMULET);
        Registry.register(Registries.ITEM, new Identifier(RPGKitMagicMod.MOD_ID, "amulets/netherite_lapis"), NETHERITE_LAPIS_AMULET);
        Registry.register(Registries.ITEM, new Identifier(RPGKitMagicMod.MOD_ID, "amulets/netherite_amethyst"), NETHERITE_AMETHYST_AMULET);
        Registry.register(Registries.ITEM, new Identifier(RPGKitMagicMod.MOD_ID, "amulets/netherite_quartz"), NETHERITE_QUARTZ_AMULET);
        Registry.register(Registries.ITEM, new Identifier(RPGKitMagicMod.MOD_ID, "amulets/netherite_redstone"), NETHERITE_REDSTONE_AMULET);
        Registry.register(Registries.ITEM, new Identifier(RPGKitMagicMod.MOD_ID, "amulets/netherite_diamond"), NETHERITE_DIAMOND_AMULET);
        Registry.register(Registries.ITEM, new Identifier(RPGKitMagicMod.MOD_ID, "amulets/netherite_emerald"), NETHERITE_EMERALD_AMULET);

        FuelRegistry.INSTANCE.add(MAGIC_FUEL, 2400 /* same as blaze rod */);

        ItemGroupEvents.modifyEntriesEvent(MAGIC_TOOLING_GROUP).register(content -> {
            content.add(new ItemStack(CATALYST_BAG));
            content.add(MagicChargeableItem.getChargedStack(COPPER_LAPIS_AMULET, 11f));
            content.add(MagicChargeableItem.getChargedStack(COPPER_AMETHYST_AMULET, 11f));
            content.add(MagicChargeableItem.getChargedStack(COPPER_QUARTZ_AMULET, 11f));
            content.add(MagicChargeableItem.getChargedStack(COPPER_REDSTONE_AMULET, 11f));
            content.add(MagicChargeableItem.getChargedStack(COPPER_DIAMOND_AMULET, 11f));
            content.add(MagicChargeableItem.getChargedStack(COPPER_EMERALD_AMULET, 11f));
            content.add(MagicChargeableItem.getChargedStack(IRON_LAPIS_AMULET, 11f));
            content.add(MagicChargeableItem.getChargedStack(IRON_AMETHYST_AMULET, 11f));
            content.add(MagicChargeableItem.getChargedStack(IRON_QUARTZ_AMULET, 11f));
            content.add(MagicChargeableItem.getChargedStack(IRON_REDSTONE_AMULET, 11f));
            content.add(MagicChargeableItem.getChargedStack(IRON_DIAMOND_AMULET, 11f));
            content.add(MagicChargeableItem.getChargedStack(IRON_EMERALD_AMULET, 11f));
            content.add(MagicChargeableItem.getChargedStack(GOLD_LAPIS_AMULET, 11f));
            content.add(MagicChargeableItem.getChargedStack(GOLD_AMETHYST_AMULET, 11f));
            content.add(MagicChargeableItem.getChargedStack(GOLD_QUARTZ_AMULET, 11f));
            content.add(MagicChargeableItem.getChargedStack(GOLD_REDSTONE_AMULET, 11f));
            content.add(MagicChargeableItem.getChargedStack(GOLD_DIAMOND_AMULET, 11f));
            content.add(MagicChargeableItem.getChargedStack(GOLD_EMERALD_AMULET, 11f));
            content.add(MagicChargeableItem.getChargedStack(NETHERITE_LAPIS_AMULET, 11f));
            content.add(MagicChargeableItem.getChargedStack(NETHERITE_AMETHYST_AMULET, 11f));
            content.add(MagicChargeableItem.getChargedStack(NETHERITE_QUARTZ_AMULET, 11f));
            content.add(MagicChargeableItem.getChargedStack(NETHERITE_REDSTONE_AMULET, 11f));
            content.add(MagicChargeableItem.getChargedStack(NETHERITE_DIAMOND_AMULET, 11f));
            content.add(MagicChargeableItem.getChargedStack(NETHERITE_EMERALD_AMULET, 11f));
        });
    }
}
