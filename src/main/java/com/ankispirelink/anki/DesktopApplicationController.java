package com.ankispirelink.anki;

import java.io.IOException;

public final class DesktopApplicationController implements ExternalApplicationController {
    private static final String[] GAME_APP_NAMES = {
            "Modded Slay the Spire",
            "Slay the Spire",
            "SlayTheSpire"
    };

    private final CommandRunner commandRunner;

    public DesktopApplicationController() {
        this(new ProcessCommandRunner());
    }

    DesktopApplicationController(CommandRunner commandRunner) {
        this.commandRunner = commandRunner == null ? new ProcessCommandRunner() : commandRunner;
    }

    @Override
    public boolean switchesFocus() {
        return isMac();
    }

    @Override
    public void focusAnki() {
        openApplication("Anki");
    }

    @Override
    public void focusGame() {
        focusRunningMacProcessOnly(GAME_APP_NAMES);
    }

    private boolean openApplication(String applicationName) {
        if (!isMac()) {
            return false;
        }
        return commandRunner.run("/usr/bin/open", "-a", applicationName);
    }

    private boolean focusRunningMacProcessOnly(String... processNames) {
        if (!isMac()) {
            return false;
        }
        return commandRunner.run("/usr/bin/osascript", "-e", runningProcessFocusScript(processNames));
    }

    private static String runningProcessFocusScript(String... processNames) {
        StringBuilder script = new StringBuilder();
        script.append("tell application \"System Events\"\n");
        for (String processName : processNames) {
            script.append("if exists process \"")
                    .append(escapeAppleScript(processName))
                    .append("\" then\n")
                    .append("set frontmost of process \"")
                    .append(escapeAppleScript(processName))
                    .append("\" to true\n")
                    .append("return \"true\"\n")
                    .append("end if\n");
        }
        script.append("end tell\nreturn \"false\"");
        return script.toString();
    }

    private static boolean isMac() {
        String osName = System.getProperty("os.name", "").toLowerCase();
        return osName.contains("mac");
    }

    private static String escapeAppleScript(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    interface CommandRunner {
        boolean run(String... command);
    }

    private static final class ProcessCommandRunner implements CommandRunner {
        @Override
        public boolean run(String... command) {
            try {
                Process process = new ProcessBuilder(command).start();
                return process.waitFor() == 0;
            } catch (IOException ignored) {
                return false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }
}
