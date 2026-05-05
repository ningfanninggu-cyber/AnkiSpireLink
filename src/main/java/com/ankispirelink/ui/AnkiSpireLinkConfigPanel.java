package com.ankispirelink.ui;

import com.ankispirelink.AnkiSpireLinkConfig;
import com.ankispirelink.AnkiSpireLinkMod;
import basemod.ModLabeledToggleButton;
import basemod.ModLabel;
import basemod.ModPanel;
import basemod.ModTextInput;
import com.badlogic.gdx.graphics.Color;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.localization.UIStrings;

public class AnkiSpireLinkConfigPanel extends ModPanel {
    private static final UIStrings UI_STRINGS =
            CardCrawlGame.languagePack.getUIString(AnkiSpireLinkMod.JSON_KEY + "ConfigPanel");

    public AnkiSpireLinkConfigPanel() {
        addUIElement(new ModLabel(text(0, "Anki deck name"),
                360.0F, 720.0F, Color.WHITE, FontHelper.charDescFont, this, label -> {}));
        addUIElement(new ModTextInput(AnkiSpireLinkConfig.deckName(),
                360.0F, 660.0F, 640.0F, 48.0F, this, input -> {
            AnkiSpireLinkConfig.setDeckName(input.getCurrentText());
            AnkiSpireLinkMod.ankiClient().setDeckName(AnkiSpireLinkConfig.deckName());
        }).setCharacterLimit(120));
        addUIElement(new ModLabel(text(1, "Leave empty to review all due/new cards."),
                360.0F, 610.0F, Color.LIGHT_GRAY, FontHelper.charDescFont, this, label -> {}));
        addUIElement(new ModLabeledToggleButton(text(2, "Switch focus to Anki"),
                360.0F, 550.0F, Color.WHITE, FontHelper.charDescFont,
                AnkiSpireLinkConfig.switchFocusToAnki(), this, label -> {},
                button -> AnkiSpireLinkConfig.setSwitchFocusToAnki(button.enabled)));
        addUIElement(new ModLabel(text(3, "Turn off for manual split-screen: the mod keeps waiting and polling without app focus changes."),
                360.0F, 500.0F, Color.LIGHT_GRAY, FontHelper.charDescFont, this, label -> {}));
    }

    private static String text(int index, String fallback) {
        return UI_STRINGS != null && UI_STRINGS.TEXT != null
                && index >= 0 && index < UI_STRINGS.TEXT.length
                ? UI_STRINGS.TEXT[index]
                : fallback;
    }
}
