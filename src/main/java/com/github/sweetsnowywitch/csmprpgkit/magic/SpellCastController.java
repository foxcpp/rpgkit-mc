package com.github.sweetsnowywitch.csmprpgkit.magic;

public interface SpellCastController {
    void startBuild();
    void addElement(int index);
    void switchCatalystBag();
    void performSelfCast();
    void performAreaCast();
    void performItemCast();
    void performUseCast();
}
