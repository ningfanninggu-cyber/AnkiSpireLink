package com.ankispirelink.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnkiRunChoiceStateTest {

    @Test
    void promptsOnceForNewRunWithoutAnkiRelic() {
        AnkiRunChoiceState state = new AnkiRunChoiceState();
        Object player = new Object();

        assertEquals(AnkiRunChoiceState.Decision.OPEN_CHOICE,
                state.onDungeonInitialize(player, false));
        assertEquals(AnkiRunChoiceState.Decision.DO_NOT_OPEN,
                state.onDungeonInitialize(player, false));
    }

    @Test
    void disablingSuppressesPromptForLaterActsInSameRun() {
        AnkiRunChoiceState state = new AnkiRunChoiceState();
        Object player = new Object();

        state.onDungeonInitialize(player, false);
        state.markDisabled();

        assertEquals(AnkiRunChoiceState.Decision.DO_NOT_OPEN,
                state.onDungeonInitialize(player, false));
        assertFalse(state.isEnabled());
    }

    @Test
    void newPlayerReferenceStartsANewChoice() {
        AnkiRunChoiceState state = new AnkiRunChoiceState();
        Object firstRunPlayer = new Object();
        Object secondRunPlayer = new Object();

        state.onDungeonInitialize(firstRunPlayer, false);
        state.markDisabled();

        assertEquals(AnkiRunChoiceState.Decision.OPEN_CHOICE,
                state.onDungeonInitialize(secondRunPlayer, false));
    }

    @Test
    void existingRelicMeansTheRunIsAlreadyEnabled() {
        AnkiRunChoiceState state = new AnkiRunChoiceState();

        assertEquals(AnkiRunChoiceState.Decision.DO_NOT_OPEN,
                state.onDungeonInitialize(new Object(), true));
        assertTrue(state.isEnabled());
    }

    @Test
    void enablingMarksRunActive() {
        AnkiRunChoiceState state = new AnkiRunChoiceState();
        state.onDungeonInitialize(new Object(), false);

        state.markEnabled();

        assertTrue(state.isEnabled());
    }

    @Test
    void disabledChoiceStaysDisabledEvenIfRelicIsStillPresentLater() {
        AnkiRunChoiceState state = new AnkiRunChoiceState();
        Object player = new Object();
        state.onDungeonInitialize(player, false);
        state.markDisabled();

        assertEquals(AnkiRunChoiceState.Decision.DO_NOT_OPEN,
                state.onDungeonInitialize(player, true));
        assertFalse(state.isEnabled());
    }

    @Test
    void loadedDisabledChoiceSuppressesPromptForContinuedRun() {
        AnkiRunChoiceState state = new AnkiRunChoiceState();
        state.loadValue("disabled");

        assertEquals(AnkiRunChoiceState.Decision.DO_NOT_OPEN,
                state.onDungeonInitialize(new Object(), false));
        assertFalse(state.isEnabled());
        assertEquals("disabled", state.saveValue());
    }

    @Test
    void loadedEnabledChoiceSuppressesPromptAndEnablesRun() {
        AnkiRunChoiceState state = new AnkiRunChoiceState();
        state.loadValue("enabled");

        assertEquals(AnkiRunChoiceState.Decision.DO_NOT_OPEN,
                state.onDungeonInitialize(new Object(), false));
        assertTrue(state.isEnabled());
        assertEquals("enabled", state.saveValue());
    }

    @Test
    void unknownLoadedChoicePromptsNormallyForContinuedRun() {
        AnkiRunChoiceState state = new AnkiRunChoiceState();
        state.loadValue("corrupt");

        assertEquals(AnkiRunChoiceState.Decision.OPEN_CHOICE,
                state.onDungeonInitialize(new Object(), false));
        assertFalse(state.isEnabled());
        assertEquals("unresolved", state.saveValue());
    }

    @Test
    void loadedChoiceIsConsumedBeforeNextFreshRun() {
        AnkiRunChoiceState state = new AnkiRunChoiceState();
        state.loadValue("disabled");

        assertEquals(AnkiRunChoiceState.Decision.DO_NOT_OPEN,
                state.onDungeonInitialize(new Object(), false));
        assertEquals(AnkiRunChoiceState.Decision.OPEN_CHOICE,
                state.onDungeonInitialize(new Object(), false));
    }

    @Test
    void loadingSavedChoiceDuringCurrentRunDoesNotMakeSamePlayerLookFresh() {
        AnkiRunChoiceState state = new AnkiRunChoiceState();
        Object player = new Object();
        state.onDungeonInitialize(player, false);
        state.markEnabled();

        state.loadValue("enabled");

        assertEquals(AnkiRunChoiceState.Decision.DO_NOT_OPEN,
                state.onDungeonInitialize(player, false));
        assertTrue(state.isEnabled());
    }
}
