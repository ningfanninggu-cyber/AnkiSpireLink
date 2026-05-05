package com.ankispirelink.screens;

import com.ankispirelink.AnkiSpireLinkConfig;
import com.ankispirelink.AnkiSpireLinkMod;
import com.ankispirelink.anki.AnkiEase;
import com.ankispirelink.anki.DesktopApplicationController;
import com.ankispirelink.anki.ExternalApplicationController;
import com.ankispirelink.anki.ExternalAnkiReviewSession;
import com.ankispirelink.anki.ExternalAnkiReviewSession.FailureReason;
import com.ankispirelink.anki.NoOpApplicationController;
import com.ankispirelink.relics.AnkiLinkRelic;
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

public class AnkiReviewScreen extends CustomScreen {
    private static final UIStrings UI_STRINGS =
            CardCrawlGame.languagePack.getUIString(AnkiSpireLinkMod.JSON_KEY + "ReviewScreen");
    private static final String[] TEXT = UI_STRINGS == null || UI_STRINGS.TEXT == null
            ? new String[0]
            : UI_STRINGS.TEXT;

    private static final float PANEL_W = 900.0F * Settings.xScale;
    private static final float PANEL_H = 360.0F * Settings.yScale;
    private static final float PANEL_X = 0.5F * (Settings.WIDTH - PANEL_W);
    private static final float PANEL_Y = 0.5F * (Settings.HEIGHT - PANEL_H);
    private static final float BUTTON_W = 250.0F * Settings.xScale;
    private static final float BUTTON_H = 64.0F * Settings.yScale;

    private final Hitbox retryHb = new Hitbox(PANEL_X + PANEL_W * 0.5F - BUTTON_W * 0.5F,
            PANEL_Y + 44.0F * Settings.yScale, BUTTON_W, BUTTON_H);

    private AnkiLinkRelic relic;
    private ExternalAnkiReviewSession reviewSession;
    private boolean completed;
    private boolean failed;
    private boolean activeReviewSwitchesFocus;
    private FailureReason failureReason = FailureReason.NONE;
    private String failureMessage = "";

    public static void openLoadingReviewScreen(AnkiLinkRelic relic) {
        BaseMod.openCustomScreen(Enum.ANKI_REVIEW_SCREEN, relic);
    }

    @Override
    public void open(Object... params) {
        this.relic = extractRelic(params);
        this.completed = false;
        startExternalReview();
        reopen();
    }

    @Override
    public AbstractDungeon.CurrentScreen curScreen() {
        return Enum.ANKI_REVIEW_SCREEN;
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
        cancelReviewSession();
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
        if (reviewSession != null && reviewSession.isDone()) {
            if (reviewSession.isSuccessful()) {
                finishWithEase(reviewSession.getEase());
                return;
            }
            failed = true;
            failureReason = reviewSession.getFailureReason();
            failureMessage = reviewSession.getFailureMessage();
            reviewSession = null;
        }

        if (failed) {
            if (clicked(retryHb)) {
                startExternalReview();
            }
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        renderPanel(sb);
        renderText(sb);
        if (failed) {
            renderButton(sb, retryHb, retryButtonText(), Settings.GOLD_COLOR);
        }
        if (Settings.isDebug) {
            retryHb.render(sb);
        }
    }

    @Override
    public void openingSettings() {
        AbstractDungeon.previousScreen = curScreen();
    }

    private void startExternalReview() {
        cancelReviewSession();
        failed = false;
        failureReason = FailureReason.NONE;
        failureMessage = "";
        ExternalApplicationController applicationController = reviewApplicationController();
        activeReviewSwitchesFocus = applicationController.switchesFocus();
        reviewSession = new ExternalAnkiReviewSession(
                AnkiSpireLinkMod.ankiClient(),
                applicationController);
        reviewSession.start();
    }

    private static ExternalApplicationController reviewApplicationController() {
        return AnkiSpireLinkConfig.switchFocusToAnki()
                ? new DesktopApplicationController()
                : new NoOpApplicationController();
    }

    private static AnkiLinkRelic extractRelic(Object... params) {
        if (params == null) {
            return null;
        }
        for (Object param : params) {
            if (param instanceof AnkiLinkRelic) {
                return (AnkiLinkRelic) param;
            }
        }
        return null;
    }

    private void finishWithEase(AnkiEase ease) {
        if (completed) {
            return;
        }
        completed = true;
        cancelReviewSession();
        if (relic != null) {
            relic.completeReview(ease);
        }
        AbstractDungeon.closeCurrentScreen();
    }

    private void cancelReviewSession() {
        if (reviewSession != null) {
            reviewSession.cancel();
            reviewSession = null;
        }
    }

    private void renderPanel(SpriteBatch sb) {
        sb.setColor(new Color(0.0F, 0.0F, 0.0F, 0.52F));
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, 0.0F, 0.0F, Settings.WIDTH, Settings.HEIGHT);
        sb.setColor(new Color(0.10F, 0.11F, 0.13F, 0.97F));
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, PANEL_X, PANEL_Y, PANEL_W, PANEL_H);
        sb.setColor(new Color(0.85F, 0.78F, 0.54F, 1.0F));
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, PANEL_X, PANEL_Y + PANEL_H - 4.0F * Settings.yScale,
                PANEL_W, 4.0F * Settings.yScale);
    }

    private void renderText(SpriteBatch sb) {
        FontHelper.renderFontCentered(sb, FontHelper.cardTitleFont,
                text(0, "Anki Review"),
                Settings.WIDTH * 0.5F, PANEL_Y + PANEL_H - 56.0F * Settings.yScale, Settings.CREAM_COLOR);

        String body = failed ? failureStatus()
                : currentStatus();
        FontHelper.renderSmartText(sb, FontHelper.cardDescFont_N,
                body,
                PANEL_X + 76.0F * Settings.xScale,
                PANEL_Y + PANEL_H - 122.0F * Settings.yScale,
                PANEL_W - 152.0F * Settings.xScale,
                30.0F * Settings.yScale,
                failed ? Settings.RED_TEXT_COLOR : Settings.CREAM_COLOR);

        if (!failed) {
            FontHelper.renderFontCentered(sb, FontHelper.cardDescFont_N,
                    text(6, "The game is waiting for your answer in the real Anki app."),
                    Settings.WIDTH * 0.5F,
                    PANEL_Y + 86.0F * Settings.yScale,
                    Settings.GOLD_COLOR);
        }
    }

    private String currentStatus() {
        if (reviewSession != null) {
            return statusText(reviewSession.getStatus(), reviewSession.getStatusDetail());
        }
        String deckName = AnkiSpireLinkMod.ankiClient().getDeckName();
        if (deckName != null && !deckName.trim().isEmpty()) {
            return text(1, "Anki review started for deck: ") + deckName
                    + "\n" + text(2, "Finish the current review card in the real Anki app.");
        }
        return text(3, "Start reviewing your chosen deck in Anki, then answer one card there.");
    }

    private String failureStatus() {
        String message;
        if (failureReason == FailureReason.NO_ACTIVE_CARD) {
            message = text(4, "No active Anki review was detected.");
        } else if (failureMessage != null && !failureMessage.trim().isEmpty()) {
            message = text(13, "Anki review failed: ") + failureMessage;
        } else {
            message = text(4, "No active Anki review was detected.");
        }
        return message + "\n" + text(5, "This card will not resolve until you complete an Anki review.");
    }

    private String retryButtonText() {
        return activeReviewSwitchesFocus
                ? text(7, "Retry / Open Anki")
                : text(16, "Retry");
    }

    private static String statusText(ExternalAnkiReviewSession.Status status, String detail) {
        if (status == ExternalAnkiReviewSession.Status.OPENING_ANKI) {
            return text(8, "Opening Anki...");
        }
        if (status == ExternalAnkiReviewSession.Status.CONNECTING_TO_ANKI) {
            return text(15, "Waiting for AnkiConnect without switching app focus...");
        }
        if (status == ExternalAnkiReviewSession.Status.STARTING_DECK_REVIEW) {
            return text(9, "Starting Anki deck review: ") + safeDetail(detail);
        }
        if (status == ExternalAnkiReviewSession.Status.WAITING_FOR_MANUAL_REVIEW) {
            return text(10, "Waiting for you to start a review in Anki...");
        }
        if (status == ExternalAnkiReviewSession.Status.ANSWER_CURRENT_CARD) {
            return text(11, "Answer the current card in Anki: ") + safeDetail(detail);
        }
        if (status == ExternalAnkiReviewSession.Status.COMPLETE) {
            return text(12, "Anki review complete.");
        }
        return text(14, "Starting Anki review...");
    }

    private static String safeDetail(String detail) {
        return detail == null ? "" : detail;
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
        return index >= 0 && index < TEXT.length && TEXT[index] != null ? TEXT[index] : fallback;
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
        public static AbstractDungeon.CurrentScreen ANKI_REVIEW_SCREEN;
    }
}
