package com.github.sweetsnowywitch.csmprpgkit.classes;

import com.github.sweetsnowywitch.csmprpgkit.JSONParameters;
import com.google.gson.JsonObject;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

 /**
  * Perk is a special ability that can be unlocked via class level
  * progression.
  * <p>
  *     Perk itself lacks any logic or interfaces other
  *     than serialization. Consumers will be probably use instanceof
  *     to check for particular interfaces or even implementations
  *     where appropriate. E.g. {@link ServerTickablePerk} defines {@link ServerTickablePerk#tick(ServerPlayerEntity) tick()}
  *     method which is called on each tick by {@link com.github.sweetsnowywitch.csmprpgkit.components.ClassComponent ClassComponent}.
  * </p>
  * <p>
  *     Perk instances with appropriate default (empty) values
  *     should be registered in {@link com.github.sweetsnowywitch.csmprpgkit.ModRegistries#CLASS_PERKS ModRegistries.CLASS_PERKS}.
  * </p>
 */
public abstract class Perk implements JSONParameters<Perk> {
    public final Identifier typeId;

    public Perk(Identifier typeId) {
        this.typeId = typeId;
    }

    public abstract Perk withParametersFromJSON(JsonObject jsonObject);
    public abstract JsonObject parametersToJSON();

    @Override
    public String toString() {
        return "Perk{"+this.typeId.toString()+"}";
    }
}
