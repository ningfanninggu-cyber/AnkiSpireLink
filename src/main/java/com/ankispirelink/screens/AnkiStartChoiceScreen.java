package com.ankispirelink.screens;

import com.ankispirelink.AnkiSpireLinkMod;
import basemod.BaseMod;
import basemod.abstracts.CustomScreen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireEnum;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

public class AnkiStartChoiceScreen extends CustomScreen {
    private static final UIStrings UI_STRINGS =
            CardCrawlGame.languagePack.getUIString(AnkiSpireLinkMod.JSON_KEY + "StartChoiceScreen");
    private static final String[] TEXT = UI_STRINGS == null || UI_STRINGS.TEXT == null
            ? new String[0]
            : UI_STRINGS.TEXT;

    private static final float PANEL_W = 980.0F * Settings.xScale;
    private static final float PANEL_H = 420.0F * Settings.yScale;
    private static final float PANEL_X = 0.5F * (Settings.WIDTH - PANEL_W);
    private static final float PANEL_Y = 0.5F * (Settings.HEIGHT - PANEL_H);
    private static final float BUTTON_W = 280.0F * Settings.xScale;
    private static final float BUTTON_H = 68.0F * Settings.yScale;
    private static final float BUTTON_Y = PANEL_Y + 58.0F * Settings.yScale;

    private final Hitbox enableHb = new Hitbox(
            PANEL_X + PANEL_W * 0.5F - BUTTON_W - 32.0F * Settings.xScale,
            BUTTON_Y, BUTTON_W, BUTTON_H);
    private final Hitbox disableHb = new Hitbox(
            PANEL_X + PANEL_W * 0.5F + 32.0F * Settings.xScale,
            BUTTON_Y, BUTTON_W, BUTTON_H);

    private boolean completed;

    public static boolean openChoiceScreen() {
        return BaseMod.openCustomScreen(Enum.ANKI_START_CHOICE_SCREEN);
    }

    @Override
    public void open(Object... params) {
        completed = false;
        reopen();
    }

    @Override
    public AbstractDungeon.CurrentScreen curScreen() {
        return Enum.ANKI_START_CHOICE_SCREEN;
    }

    @Override
    public void reopen() {
        AbstractDungeon.isScreenUp = true;
        AbstractDungeon.screen = curScreen();
        if (AbstractDungeon.overlayMenu != null) {
            AbstractDungeon.overlayMenu.proceedButton.hide();
            AbstractDungeon.overlayMenu.cancelButton.hide();
            AbstractDungeon.overlayMenu.hideBlackScreen();
        }
    }

    @Override
    public void close() {
        if (!completed) {
            reopen();
            return;
        }
        if (AbstractDungeon.previousScreen == null) {
            AbstractDungeon.isScreenUp = false;
            if (AbstractDungeon.overlayMenu != null) {
                AbstractDungeon.overlayMenu.hideBlackScreen();
            }
        }
        if (AbstractDungeon.getCurrRoom() != null
                && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT
                && AbstractDungeon.player != null
                && !AbstractDungeon.player.isDead
                && AbstractDungeon.overlayMenu != null) {
            AbstractDungeon.overlayMenu.showCombatPanels();
        }
    }

    @Override
    public void update() {
        if (InputHelper.pressedEscape || CInputActionSet.cancel.isJustPressed()) {
            InputHelper.pressedEscape = false;
            CInputActionSet.cancel.unpress();
            chooseDisabled();
            return;
        }

        if (clicked(enableHb)) {
            chooseEnabled();
            return;
        }
        if (clicked(disableHb)) {
            chooseDisabled();
        }
    }

    private void chooseEnabled() {
        completed = true;
        AnkiSpireLinkMod.enableForCurrentRun();
        AbstractDungeon.closeCurrentScreen();
    }

    private void chooseDisabled() {
        completed = true;
        AnkiSpireLinkMod.disableForCurrentRun();
        AbstractDungeon.closeCurrentScreen();
    }

    @Override
    public void render(SpriteBatch sb) {
        renderPanel(sb);
        FontHelper.renderFontCentered(sb, FontHelper.cardTitleFont,
                text(0, "Anki Spire Link"),
                Settings.WIDTH * 0.5F,
                PANEL_Y + PANEL_H - 60.0F * Settings.yScale,
                Settings.CREAM_COLOR);
        FontHelper.renderSmartText(sb, FontHelper.cardDescFont_N,
                text(1, "Use Anki Spire Link for this run?"),
                PANEL_X + 86.0F * Settings.xScale,
                PANEL_Y + PANEL_H - 130.0F * Settings.yScale,
                PANEL_W - 172.0F * Settings.xScale,
                30.0F * Settings.yScale,
                Settings.CREAM_COLOR);
        renderButton(sb, enableHb, text(2, "Use this run"), Settings.GOLD_COLOR);
        renderButton(sb, disableHb, text(3, "Not this run"), Color.DARK_GRAY.cpy());
        if (Settings.isDebug) {
            enableHb.render(sb);
            disableHb.render(sb);
        }
    }

    @Override
    public void openingSettings() {
        AbstractDungeon.previousScreen = curScreen();
    }

    private static void renderPanel(SpriteBatch sb) {
        sb.setColor(new Color(0.0F, 0.0F, 0.0F, 0.48F));
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, 0.0F, 0.0F, Settings.WIDTH, Settings.HEIGHT);
        sb.setColor(new Color(0.10F, 0.11F, 0.13F, 0.97F));
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, PANEL_X, PANEL_Y, PANEL_W, PANEL_H);
        sb.setColor(new Color(0.85F, 0.78F, 0.54F, 1.0F));
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, PANEL_X, PANEL_Y + PANEL_H - 4.0F * Settings.yScale,
                PANEL_W, 4.0F * Settings.yScale);
    }

    private static void renderButton(SpriteBatch sb, Hitbox hb, String label, Color baseColor) {
        Color color = baseColor.cpy();
        color.a = hb.hovered ? 1.0F : 0.78F;
        sb.setColor(color);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, hb.x, hb.y, hb.width, hb.height);
        FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont, label,
                hb.cX, hb.cY, Color.WHITE);
    }

    private static String text(int index, String fallback) {
        return index >= 0 && index < TEXT.length && TEXT[index] != null
                ? TEXT[index]
                : fallback;
    }

    private static boolean clicked(Hitbox hb) {
        hb.update();
        if (hb.hovered && InputHelper.justClickedLeft) {
            InputHelper.justClickedLeft = false;
            return true;
        }
        if (hb.hovered && CInputActionSet.select.isJustPressed()) {
            CInputActionSet.select.unpress();
            return true;
        }
        if (hb.clicked) {
            hb.clicked = false;
            return true;
        }
        return false;
    }

    public static class Enum {
        @SpireEnum
        public static AbstractDungeon.CurrentScreen ANKI_START_CHOICE_SCREEN;
    }
}
