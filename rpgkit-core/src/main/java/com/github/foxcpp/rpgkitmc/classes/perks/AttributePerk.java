package com.github.foxcpp.rpgkitmc.classes.perks;

import com.github.foxcpp.rpgkitmc.RPGKitMod;
import com.github.foxcpp.rpgkitmc.classes.Perk;
import com.google.gson.JsonObject;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class AttributePerk extends Perk {
    private final EntityAttribute entityAttribute;
    private final EntityAttributeModifier.Operation operation;
    private final float amount;

    public AttributePerk(Identifier typeId) {
        super(typeId);
        this.entityAttribute = null;
        this.operation = null;
        this.amount = 0.0f;
    }

    public AttributePerk(Identifier typeId, EntityAttribute entityAttribute, EntityAttributeModifier.Operation operation, float amount) {
        super(typeId);
        this.entityAttribute = entityAttribute;
        this.operation = operation;
        this.amount = amount;
    }

    @Override
    public Perk withParametersFromJSON(JsonObject obj) {
        var attribute = this.entityAttribute;
        if (obj.has("entityAttribute")) {
            var id = new Identifier(obj.get("entityAttribute").getAsString());
            attribute = Registries.ATTRIBUTE.get(id);
            RPGKitMod.LOGGER.debug("AttributePerk populated with attribute {}", id);
            if (attribute == null)
            {
                throw new IllegalStateException("unknown attribute");
            }
        }

        var operation = this.operation;
        if (obj.has("attributeOperation")) {
            var id = obj.get("attributeOperation").getAsInt();
            operation = EntityAttributeModifier.Operation.fromId(id);
            RPGKitMod.LOGGER.debug("AttributePerk populated with attribute operation {}", operation);
            if (operation == null)
            {
                throw new IllegalStateException("unknown operation");
            }
        }

        var amount = this.amount;
        if (obj.has("attributeAmount")) {
            amount = obj.get("attributeAmount").getAsFloat();
        }
        return new AttributePerk(this.typeId, attribute, operation, amount);
    }

    @Override
    public JsonObject parametersToJSON() {
        var obj = new JsonObject();
        if (this.entityAttribute != null) {
            var id = Registries.ATTRIBUTE.getId(this.entityAttribute);
            if (id == null) {
                throw new IllegalStateException("attribute perk with unregistered effect");
            }
            obj.addProperty("entityAttribute", id.toString());
        }
        if (this.operation != null) {
            obj.addProperty("attributeOperation", this.operation.getId());
        }
        obj.addProperty("attributeAmount", this.amount);
        return obj;
    }

    public EntityAttribute getEntityAttribute() {
        return entityAttribute;
    }

    public EntityAttributeModifier.Operation getOperation() {
        return operation;
    }

    public float getAmount() {
        return amount;
    }
}
