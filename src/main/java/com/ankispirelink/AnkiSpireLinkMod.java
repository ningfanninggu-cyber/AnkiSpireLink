package com.ankispirelink;

import com.ankispirelink.anki.AnkiConnectClient;
import com.ankispirelink.anki.HttpAnkiConnectTransport;
import com.ankispirelink.game.AnkiRunChoiceState;
import com.ankispirelink.relics.AnkiLinkRelic;
import com.ankispirelink.screens.AnkiReviewScreen;
import com.ankispirelink.screens.AnkiStartChoiceScreen;
import com.ankispirelink.ui.AnkiSpireLinkConfigPanel;
import basemod.BaseMod;
import basemod.abstracts.CustomSavableRaw;
import basemod.helpers.RelicType;
import basemod.interfaces.EditRelicsSubscriber;
import basemod.interfaces.EditStringsSubscriber;
import basemod.interfaces.PostDungeonInitializeSubscriber;
import basemod.interfaces.PostDungeonUpdateSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SpireInitializer
public class AnkiSpireLinkMod implements EditRelicsSubscriber, EditStringsSubscriber,
        PostInitializeSubscriber, PostDungeonInitializeSubscriber, PostDungeonUpdateSubscriber {
    public static final String MOD_ID = "AnkiSpireLink";
    public static final String JSON_KEY = MOD_ID + ":";
    private static final String RUN_CHOICE_SAVE_KEY = MOD_ID + "_run_choice";

    private static final Logger logger = LogManager.getLogger(AnkiSpireLinkMod.class.getName());
    private static final AnkiConnectClient ANKI_CLIENT =
            new AnkiConnectClient(new HttpAnkiConnectTransport());
    private static final AnkiRunChoiceState RUN_CHOICE = new AnkiRunChoiceState();
    private static volatile boolean pendingStartChoice;

    public AnkiSpireLinkMod() {
        AnkiSpireLinkConfig.load();
        ANKI_CLIENT.setDeckName(AnkiSpireLinkConfig.deckName());
        BaseMod.subscribe(this);
    }

    public static void initialize() {
        new AnkiSpireLinkMod();
    }

    public static AnkiConnectClient ankiClient() {
        return ANKI_CLIENT;
    }

    public static void enableForCurrentRun() {
        RUN_CHOICE.markEnabled();
        pendingStartChoice = false;
        grantRelicForCurrentRun();
    }

    public static void disableForCurrentRun() {
        RUN_CHOICE.markDisabled();
        pendingStartChoice = false;
        removeRelicForCurrentRun();
        logger.info("{} disabled for this run by player choice.", MOD_ID);
    }

    public static boolean isEnabledForCurrentRun() {
        return RUN_CHOICE.isEnabled();
    }

    @Override
    public void receiveEditRelics() {
        BaseMod.addRelic(new AnkiLinkRelic(), RelicType.SHARED);
        UnlockTracker.markRelicAsSeen(AnkiLinkRelic.ID);
    }

    @Override
    public void receiveEditStrings() {
        String language = "eng";
        if (Settings.language == Settings.GameLanguage.ZHS) {
            language = "zhs";
        } else if (Settings.language == Settings.GameLanguage.ZHT) {
            language = "zht";
        }
        BaseMod.loadCustomStringsFile(RelicStrings.class,
                "AnkiSpireLinkResources/localization/relics_" + language + ".json");
        BaseMod.loadCustomStringsFile(UIStrings.class,
                "AnkiSpireLinkResources/localization/ui_" + language + ".json");
    }

    @Override
    public void receivePostInitialize() {
        BaseMod.addCustomScreen(new AnkiReviewScreen());
        BaseMod.addCustomScreen(new AnkiStartChoiceScreen());
        BaseMod.addSaveField(RUN_CHOICE_SAVE_KEY, new CustomSavableRaw() {
            @Override
            public JsonElement onSaveRaw() {
                return new JsonPrimitive(RUN_CHOICE.saveValue());
            }

            @Override
            public void onLoadRaw(JsonElement value) {
                try {
                    if (value == null || value.isJsonNull()) {
                        RUN_CHOICE.loadValue(null);
                    } else {
                        RUN_CHOICE.loadValue(value.getAsString());
                    }
                } catch (RuntimeException e) {
                    RUN_CHOICE.loadValue(null);
                    logger.warn("Ignored invalid {} save field.", RUN_CHOICE_SAVE_KEY, e);
                }
            }
        });
        BaseMod.registerModBadge(ImageMaster.loadImage("AnkiSpireLinkResources/images/ui/badge.png"),
                "Anki Spire Link", "a17 + Codex",
                "Play a card, review an Anki card, then get a combat multiplier.",
                new AnkiSpireLinkConfigPanel());
        logger.info("{} initialized.", MOD_ID);
    }

    @Override
    public void receivePostDungeonInitialize() {
        if (AbstractDungeon.player == null || AbstractDungeon.player.relics == null) {
            return;
        }
        AnkiRunChoiceState.Decision decision = RUN_CHOICE.onDungeonInitialize(
                AbstractDungeon.player,
                AbstractDungeon.player.hasRelic(AnkiLinkRelic.ID));
        pendingStartChoice = decision == AnkiRunChoiceState.Decision.OPEN_CHOICE;
        if (!pendingStartChoice && RUN_CHOICE.isEnabled()) {
            grantRelicForCurrentRun();
        } else if (!pendingStartChoice) {
            removeRelicForCurrentRun();
        }
    }

    @Override
    public void receivePostDungeonUpdate() {
        if (!pendingStartChoice || AbstractDungeon.player == null
                || AbstractDungeon.overlayMenu == null || AbstractDungeon.isScreenUp) {
            return;
        }
        if (AnkiStartChoiceScreen.openChoiceScreen()) {
            pendingStartChoice = false;
        }
    }

    private static void grantRelicForCurrentRun() {
        if (AbstractDungeon.player == null || AbstractDungeon.player.relics == null
                || AbstractDungeon.player.hasRelic(AnkiLinkRelic.ID)) {
            return;
        }
        new AnkiLinkRelic().instantObtain(AbstractDungeon.player, AbstractDungeon.player.relics.size(), true);
        logger.debug("Granted {} to the current run.", AnkiLinkRelic.ID);
    }

    private static void removeRelicForCurrentRun() {
        if (AbstractDungeon.player != null && AbstractDungeon.player.hasRelic(AnkiLinkRelic.ID)) {
            AbstractDungeon.player.loseRelic(AnkiLinkRelic.ID);
            logger.debug("Removed {} from the current run.", AnkiLinkRelic.ID);
        }
    }
}
