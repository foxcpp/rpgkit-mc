package com.github.sweetsnowywitch.csmprpgkit.abilities;

public class ModAbilities {
    public static final Ability STRENGTH = new Ability("strength");
    public static final Ability AGILITY = new Ability("agility");
    public static final Ability CONSTITUTION = new Ability("constitution");
    public static final Ability INTELLIGENCE = new Ability("intelligence");
    public static final Ability WISDOM = new Ability("wisdom");
    public static final Ability CHARISMA = new Ability("charisma");

    public static final Ability SURVIVAL = new Ability("survival", WISDOM);
    public static final Ability HAND_AGILITY = new Ability("hand_agility", AGILITY);
}
