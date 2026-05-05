package com.ankispirelink.anki;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalAnkiReviewSessionTest {

    @Test
    void focusesAnkiStartsConfiguredDeckAndCompletesFromAnkiReviewLogEase() throws Exception {
        RecordingTransport transport = new RecordingTransport();
        transport.responses.add("{\"result\":true,\"error\":null}");
        transport.responses.add("{\"result\":{\"cardId\":42,\"deckName\":\"Daily English\"},\"error\":null}");
        transport.responses.add("{\"result\":{\"42\":[{\"id\":100,\"ease\":3},{\"id\":110,\"ease\":4}]},\"error\":null}");
        AnkiConnectClient client = new AnkiConnectClient(transport);
        client.setDeckName("Daily English");
        RecordingApplicationController applications = new RecordingApplicationController();
        ExternalAnkiReviewSession session =
                new ExternalAnkiReviewSession(client, applications, 1L, 500L, () -> 105L);

        session.start();
        waitUntilDone(session);
        waitForFocusEvent(applications, "game");

        assertTrue(session.isDone());
        assertTrue(session.isSuccessful());
        assertEquals(AnkiEase.EASY, session.getEase());
        assertEquals("anki", applications.focusEvents.get(0));
        assertEquals("game", applications.focusEvents.get(1));
        assertTrue(transport.requests.get(0).contains("\"action\":\"guiDeckReview\""));
    }

    @Test
    void waitsForUserToOpenReviewManuallyWhenDeckNameIsBlank() throws Exception {
        RecordingTransport transport = new RecordingTransport();
        transport.responses.add("{\"result\":{\"cardId\":42,\"deckName\":\"Manual Deck\"},\"error\":null}");
        transport.responses.add("{\"result\":{\"42\":[{\"id\":120,\"ease\":2}]},\"error\":null}");
        AnkiConnectClient client = new AnkiConnectClient(transport);
        RecordingApplicationController applications = new RecordingApplicationController();
        ExternalAnkiReviewSession session =
                new ExternalAnkiReviewSession(client, applications, 1L, 500L, () -> 115L);

        session.start();
        waitUntilDone(session);

        assertTrue(session.isSuccessful());
        assertEquals(AnkiEase.HARD, session.getEase());
        assertFalse(transport.requests.get(0).contains("\"action\":\"guiDeckReview\""));
        assertTrue(transport.requests.get(0).contains("\"action\":\"guiCurrentCard\""));
    }

    @Test
    void noFocusControllerPollsAnkiWithoutSwitchingApplications() throws Exception {
        RecordingTransport transport = new RecordingTransport();
        transport.responses.add("{\"result\":{\"cardId\":42,\"deckName\":\"Manual Deck\"},\"error\":null}");
        transport.responses.add("{\"result\":{\"42\":[{\"id\":120,\"ease\":2}]},\"error\":null}");
        AnkiConnectClient client = new AnkiConnectClient(transport);
        RecordingApplicationController applications = new RecordingApplicationController() {
            @Override
            public boolean switchesFocus() {
                return false;
            }

            @Override
            public void focusAnki() {
                throw new AssertionError("focusAnki should not be called when focus switching is disabled.");
            }

            @Override
            public void focusGame() {
                throw new AssertionError("focusGame should not be called when focus switching is disabled.");
            }
        };
        ExternalAnkiReviewSession session =
                new ExternalAnkiReviewSession(client, applications, 1L, 500L, () -> 115L);

        session.start();
        waitUntilDone(session);

        assertTrue(session.isSuccessful());
        assertEquals(AnkiEase.HARD, session.getEase());
        assertTrue(applications.focusEvents.isEmpty());
        assertTrue(transport.requests.get(0).contains("\"action\":\"guiCurrentCard\""));
    }

    @Test
    void followsAnkiWhenCurrentCardChangesDuringReview() throws Exception {
        RecordingTransport transport = new RecordingTransport();
        transport.responses.add("{\"result\":true,\"error\":null}");
        transport.responses.add("{\"result\":{\"cardId\":42,\"deckName\":\"Daily English\"},\"error\":null}");
        transport.responses.add("{\"result\":{\"42\":[]},\"error\":null}");
        transport.responses.add("{\"result\":{\"cardId\":43,\"deckName\":\"Daily English\"},\"error\":null}");
        transport.responses.add("{\"result\":{\"42\":[],\"43\":[{\"id\":130,\"ease\":4}]},\"error\":null}");
        AnkiConnectClient client = new AnkiConnectClient(transport);
        client.setDeckName("Daily English");
        RecordingApplicationController applications = new RecordingApplicationController();
        ExternalAnkiReviewSession session =
                new ExternalAnkiReviewSession(client, applications, 1L, 500L, () -> 125L);

        session.start();
        waitUntilDone(session);

        assertTrue(session.isSuccessful());
        assertEquals(AnkiEase.EASY, session.getEase());
        assertTrue(transport.requests.get(4).contains("\"cards\":[42,43]"));
    }

    @Test
    void keepsWaitingAfterCardIsVisibleEvenPastDetectionTimeout() throws Exception {
        RecordingTransport transport = new RecordingTransport();
        transport.responses.add("{\"result\":{\"cardId\":42,\"deckName\":\"Math\"},\"error\":null}");
        transport.responses.add("{\"result\":{\"42\":[]},\"error\":null}");
        transport.responses.add("{\"result\":{\"cardId\":42,\"deckName\":\"Math\"},\"error\":null}");
        transport.responses.add("{\"result\":{\"42\":[{\"id\":60000,\"ease\":3}]},\"error\":null}");
        AnkiConnectClient client = new AnkiConnectClient(transport);
        RecordingApplicationController applications = new RecordingApplicationController();
        final long[] times = {0L, 0L, 0L, 60000L};
        final int[] index = {0};
        ExternalAnkiReviewSession session =
                new ExternalAnkiReviewSession(client, applications, 1L, 5L, () -> {
                    int current = Math.min(index[0], times.length - 1);
                    index[0]++;
                    return times[current];
                });

        session.start();
        waitUntilDone(session);

        assertTrue(session.isSuccessful());
        assertEquals(AnkiEase.GOOD, session.getEase());
    }

    @Test
    void acceptsReviewLogEntryAtSessionStartWithoutUsingOlderAnswers() throws Exception {
        RecordingTransport transport = new RecordingTransport();
        transport.responses.add("{\"result\":{\"cardId\":42,\"deckName\":\"Math\"},\"error\":null}");
        transport.responses.add("{\"result\":{\"42\":[{\"id\":999,\"ease\":2},{\"id\":1000,\"ease\":3}]},\"error\":null}");
        AnkiConnectClient client = new AnkiConnectClient(transport);
        RecordingApplicationController applications = new RecordingApplicationController();
        ExternalAnkiReviewSession session =
                new ExternalAnkiReviewSession(client, applications, 1L, 500L, () -> 1000L);

        session.start();
        waitUntilDone(session);

        assertTrue(session.isSuccessful());
        assertEquals(AnkiEase.GOOD, session.getEase());
    }

    @Test
    void invalidCurrentCardDoesNotDisableInitialDetectionTimeout() throws Exception {
        RecordingTransport transport = new RecordingTransport();
        transport.responses.add("{\"result\":{\"cardId\":0,\"deckName\":\"\"},\"error\":null}");
        transport.responses.add("{\"result\":{\"cardId\":0,\"deckName\":\"\"},\"error\":null}");
        AnkiConnectClient client = new AnkiConnectClient(transport);
        RecordingApplicationController applications = new RecordingApplicationController();
        final long[] times = {0L, 0L, 100L, 100L};
        final int[] index = {0};
        ExternalAnkiReviewSession session =
                new ExternalAnkiReviewSession(client, applications, 1L, 5L, () -> {
                    int current = Math.min(index[0], times.length - 1);
                    index[0]++;
                    return times[current];
                });

        session.start();
        waitUntilDone(session);

        assertTrue(session.isDone());
        assertFalse(session.isSuccessful());
        assertEquals(ExternalAnkiReviewSession.FailureReason.NO_ACTIVE_CARD,
                session.getFailureReason());
    }

    @Test
    void cancelDuringFocusStopsBeforeStartingDeckReview() throws Exception {
        RecordingTransport transport = new RecordingTransport();
        AnkiConnectClient client = new AnkiConnectClient(transport);
        client.setDeckName("Daily English");
        final ExternalAnkiReviewSession[] sessionRef = new ExternalAnkiReviewSession[1];
        RecordingApplicationController applications = new RecordingApplicationController() {
            @Override
            public void focusAnki() {
                super.focusAnki();
                sessionRef[0].cancel();
            }
        };
        sessionRef[0] = new ExternalAnkiReviewSession(client, applications, 1L, 500L, () -> 125L);

        sessionRef[0].start();
        waitUntilDone(sessionRef[0]);

        assertTrue(sessionRef[0].isDone());
        assertFalse(sessionRef[0].isSuccessful());
        assertEquals(1, applications.focusEvents.size());
        assertEquals("anki", applications.focusEvents.get(0));
        assertTrue(transport.requests.isEmpty());
    }

    @Test
    void cancelAfterSuccessfulReviewDoesNotInterruptFocusBackToGame() throws Exception {
        RecordingTransport transport = new RecordingTransport();
        transport.responses.add("{\"result\":{\"cardId\":42,\"deckName\":\"Math\"},\"error\":null}");
        transport.responses.add("{\"result\":{\"42\":[{\"id\":100,\"ease\":3}]},\"error\":null}");
        AnkiConnectClient client = new AnkiConnectClient(transport);
        SlowFocusApplicationController applications = new SlowFocusApplicationController();
        ExternalAnkiReviewSession session =
                new ExternalAnkiReviewSession(client, applications, 1L, 500L, () -> 90L);

        session.start();
        waitUntilDone(session);
        session.cancel();
        applications.waitForGameFocus();

        assertTrue(session.isSuccessful());
        assertFalse(applications.focusGameInterrupted);
        assertTrue(applications.focusEvents.contains("game"));
    }

    private static void waitUntilDone(ExternalAnkiReviewSession session) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 2000L;
        while (!session.isDone() && System.currentTimeMillis() < deadline) {
            Thread.sleep(10L);
        }
    }

    private static void waitForFocusEvent(RecordingApplicationController applications, String event)
            throws InterruptedException {
        long deadline = System.currentTimeMillis() + 2000L;
        while (!applications.focusEvents.contains(event) && System.currentTimeMillis() < deadline) {
            Thread.sleep(10L);
        }
    }

    private static class RecordingTransport implements AnkiConnectTransport {
        private final List<String> requests = new ArrayList<>();
        private final List<String> responses = new ArrayList<>();

        @Override
        public String post(String requestJson) {
            requests.add(requestJson);
            if (responses.isEmpty()) {
                return "{\"result\":null,\"error\":\"missing test response\"}";
            }
            return responses.remove(0);
        }
    }

    private static class RecordingApplicationController implements ExternalApplicationController {
        protected final List<String> focusEvents = new ArrayList<>();

        @Override
        public void focusAnki() {
            focusEvents.add("anki");
        }

        @Override
        public void focusGame() {
            focusEvents.add("game");
        }
    }

    private static class SlowFocusApplicationController extends RecordingApplicationController {
        private volatile boolean focusGameInterrupted;

        @Override
        public void focusGame() {
            try {
                Thread.sleep(80L);
            } catch (InterruptedException e) {
                focusGameInterrupted = true;
                Thread.currentThread().interrupt();
            }
            super.focusGame();
        }

        private void waitForGameFocus() throws InterruptedException {
            waitForFocusEvent(this, "game");
        }
    }
}
