package com.ankispirelink.anki;

public enum AnkiEase {
    AGAIN(1, "Again"),
    HARD(2, "Hard"),
    GOOD(3, "Good"),
    EASY(4, "Easy");

    private final int ankiValue;
    private final String displayName;

    AnkiEase(int ankiValue, String displayName) {
        this.ankiValue = ankiValue;
        this.displayName = displayName;
    }

    public int getAnkiValue() {
        return ankiValue;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static AnkiEase fromAnkiValue(int value) {
        for (AnkiEase ease : values()) {
            if (ease.ankiValue == value) {
                return ease;
            }
        }
        return GOOD;
    }

    public static boolean isKnownAnkiValue(int value) {
        for (AnkiEase ease : values()) {
            if (ease.ankiValue == value) {
                return true;
            }
        }
        return false;
    }
}
