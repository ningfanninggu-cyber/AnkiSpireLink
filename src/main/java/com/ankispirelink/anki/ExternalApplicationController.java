package com.ankispirelink.anki;

public interface ExternalApplicationController {
    default boolean switchesFocus() {
        return true;
    }

    void focusAnki();

    void focusGame();
}
