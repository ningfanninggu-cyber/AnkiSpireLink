package com.ankispirelink.anki;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DesktopApplicationControllerTest {

    @Test
    void focusGameActivatesOnlyAnAlreadyRunningGameProcessOnMac() {
        String previousOsName = System.getProperty("os.name");
        System.setProperty("os.name", "Mac OS X");
        RecordingRunner runner = new RecordingRunner();
        try {
            new DesktopApplicationController(runner).focusGame();
        } finally {
            restoreOsName(previousOsName);
        }

        assertEquals(1, runner.commands.size());
        List<String> command = runner.commands.get(0);
        assertEquals("/usr/bin/osascript", command.get(0));
        assertFalse(command.contains("/usr/bin/open"));
        assertFalse(command.toString().contains("steamapps"));
        assertTrue(command.toString().contains("Modded Slay the Spire"));
        assertTrue(command.toString().contains("set frontmost of process"));
    }

    @Test
    void focusAnkiStillOpensAnkiOnMac() {
        String previousOsName = System.getProperty("os.name");
        System.setProperty("os.name", "Mac OS X");
        RecordingRunner runner = new RecordingRunner();
        try {
            new DesktopApplicationController(runner).focusAnki();
        } finally {
            restoreOsName(previousOsName);
        }

        assertEquals(1, runner.commands.size());
        assertEquals(Arrays.asList("/usr/bin/open", "-a", "Anki"), runner.commands.get(0));
    }

    @Test
    void reportsFocusSwitchingUnavailableOffMac() {
        String previousOsName = System.getProperty("os.name");
        System.setProperty("os.name", "Linux");
        try {
            assertFalse(new DesktopApplicationController(new RecordingRunner()).switchesFocus());
        } finally {
            restoreOsName(previousOsName);
        }
    }

    @Test
    void reportsFocusSwitchingAvailableOnMac() {
        String previousOsName = System.getProperty("os.name");
        System.setProperty("os.name", "Mac OS X");
        try {
            assertTrue(new DesktopApplicationController(new RecordingRunner()).switchesFocus());
        } finally {
            restoreOsName(previousOsName);
        }
    }

    private static void restoreOsName(String previousOsName) {
        if (previousOsName == null) {
            System.clearProperty("os.name");
        } else {
            System.setProperty("os.name", previousOsName);
        }
    }

    private static class RecordingRunner implements DesktopApplicationController.CommandRunner {
        private final List<List<String>> commands = new ArrayList<>();

        @Override
        public boolean run(String... command) {
            commands.add(Arrays.asList(command));
            return true;
        }
    }
}
