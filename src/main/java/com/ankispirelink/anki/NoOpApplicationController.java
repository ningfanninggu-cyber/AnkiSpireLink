package com.ankispirelink.anki;

public final class NoOpApplicationController implements ExternalApplicationController {
    @Override
    public boolean switchesFocus() {
        return false;
    }

    @Override
    public void focusAnki() {
    }

    @Override
    public void focusGame() {
    }
}
