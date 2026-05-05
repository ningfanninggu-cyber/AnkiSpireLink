package com.ankispirelink.game;

public final class AnkiRunChoiceState {
    private Object playerReference;
    private boolean choiceResolved;
    private boolean promptOpen;
    private boolean enabled;
    private boolean loadedChoiceKnown;
    private boolean loadedEnabled;

    public Decision onDungeonInitialize(Object player, boolean hasAnkiRelic) {
        if (player == null) {
            return Decision.DO_NOT_OPEN;
        }
        if (player != playerReference) {
            playerReference = player;
            if (loadedChoiceKnown) {
                choiceResolved = true;
                enabled = loadedEnabled;
                loadedChoiceKnown = false;
                loadedEnabled = false;
            } else {
                choiceResolved = hasAnkiRelic;
                enabled = hasAnkiRelic;
            }
            promptOpen = false;
        }
        if (hasAnkiRelic && enabled) {
            choiceResolved = true;
            enabled = true;
            promptOpen = false;
            return Decision.DO_NOT_OPEN;
        }
        if (!choiceResolved && !promptOpen) {
            promptOpen = true;
            return Decision.OPEN_CHOICE;
        }
        return Decision.DO_NOT_OPEN;
    }

    public void markEnabled() {
        choiceResolved = true;
        enabled = true;
        promptOpen = false;
    }

    public void markDisabled() {
        choiceResolved = true;
        enabled = false;
        promptOpen = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String saveValue() {
        if (!choiceResolved) {
            return "unresolved";
        }
        return enabled ? "enabled" : "disabled";
    }

    public void loadValue(String value) {
        boolean known;
        boolean savedEnabled;
        if ("enabled".equals(value)) {
            known = true;
            savedEnabled = true;
        } else if ("disabled".equals(value)) {
            known = true;
            savedEnabled = false;
        } else {
            known = false;
            savedEnabled = false;
        }

        if (playerReference != null) {
            choiceResolved = known;
            enabled = known && savedEnabled;
            promptOpen = false;
            loadedChoiceKnown = false;
            loadedEnabled = false;
            return;
        }

        loadedChoiceKnown = known;
        loadedEnabled = savedEnabled;
        choiceResolved = false;
        promptOpen = false;
        enabled = false;
    }

    public enum Decision {
        OPEN_CHOICE,
        DO_NOT_OPEN
    }
}
