package com.github.sweetsnowywitch.csmprpgkit.magic;

import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ChanneledForm {
    int getMaxChannelDuration(Spell cast, List<SpellReaction> reactions);
    void channelTick(ServerSpellCast cast, @NotNull Entity caster);
}
