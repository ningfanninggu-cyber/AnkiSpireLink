package com.ankispirelink;

import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

public final class AnkiSpireLinkConfig {
    public static final String DECK_NAME_KEY = "deckName";
    public static final String SWITCH_FOCUS_TO_ANKI_KEY = "switchFocusToAnki";

    private static final Logger logger = LogManager.getLogger(AnkiSpireLinkConfig.class.getName());
    private static SpireConfig config;
    private static String deckName = "";
    private static boolean switchFocusToAnki = true;

    private AnkiSpireLinkConfig() {
    }

    public static synchronized void load() {
        try {
            Properties defaults = new Properties();
            defaults.setProperty(DECK_NAME_KEY, "");
            defaults.setProperty(SWITCH_FOCUS_TO_ANKI_KEY, "true");
            config = new SpireConfig(AnkiSpireLinkMod.MOD_ID, "config", defaults);
            deckName = clean(config.getString(DECK_NAME_KEY));
            switchFocusToAnki = !config.has(SWITCH_FOCUS_TO_ANKI_KEY)
                    || config.getBool(SWITCH_FOCUS_TO_ANKI_KEY);
        } catch (IOException e) {
            logger.error("Failed to load AnkiSpireLink config.", e);
            deckName = "";
            switchFocusToAnki = true;
        }
    }

    public static synchronized String deckName() {
        return deckName;
    }

    public static synchronized boolean switchFocusToAnki() {
        return switchFocusToAnki;
    }

    public static synchronized void setDeckName(String value) {
        String cleaned = clean(value);
        if (cleaned.equals(deckName)) {
            return;
        }
        deckName = cleaned;
        if (config == null) {
            return;
        }
        try {
            config.setString(DECK_NAME_KEY, deckName);
            config.save();
        } catch (IOException e) {
            logger.error("Failed to save Anki deck name.", e);
        }
    }

    public static synchronized void setSwitchFocusToAnki(boolean value) {
        if (switchFocusToAnki == value) {
            return;
        }
        switchFocusToAnki = value;
        if (config == null) {
            return;
        }
        try {
            config.setBool(SWITCH_FOCUS_TO_ANKI_KEY, switchFocusToAnki);
            config.save();
        } catch (IOException e) {
            logger.error("Failed to save Anki focus switching setting.", e);
        }
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
