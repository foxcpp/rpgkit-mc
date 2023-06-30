package com.github.sweetsnowywitch.csmprpgkit.magic;

import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface ChanneledForm {
    int getMaxChannelDuration(Spell cast);

    void channelTick(ServerSpellCast cast, @NotNull Entity caster);
}
