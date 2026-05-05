package com.ankispirelink.anki;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnkiConnectClientTest {

    @Test
    void startDeckReviewUsesAnkiGuiReviewForNamedDeck() throws IOException {
        RecordingTransport transport = new RecordingTransport();
        transport.responses.add("{\"result\":true,\"error\":null}");
        AnkiConnectClient client = new AnkiConnectClient(transport);

        boolean started = client.startDeckReview("Daily English");

        assertTrue(started);
        assertTrue(transport.requests.get(0).contains("\"action\":\"guiDeckReview\""));
        assertTrue(transport.requests.get(0).contains("\"name\":\"Daily English\""));
    }

    @Test
    void currentGuiCardParsesAnkiReviewCardWithoutUsingInGameQuestionText() throws IOException {
        RecordingTransport transport = new RecordingTransport();
        transport.responses.add("{\"result\":{" +
                "\"cardId\":42," +
                "\"deckName\":\"Daily English\"," +
                "\"question\":\"front\"," +
                "\"answer\":\"back\"" +
                "},\"error\":null}");
        AnkiConnectClient client = new AnkiConnectClient(transport);

        Optional<AnkiGuiCard> card = client.currentGuiCard();

        assertTrue(card.isPresent());
        assertEquals(42L, card.get().getCardId());
        assertEquals("Daily English", card.get().getDeckName());
        assertTrue(transport.requests.get(0).contains("\"action\":\"guiCurrentCard\""));
    }

    @Test
    void currentGuiCardReturnsEmptyWhenAnkiIsNotReviewing() throws IOException {
        RecordingTransport transport = new RecordingTransport();
        transport.responses.add("{\"result\":null,\"error\":null}");
        AnkiConnectClient client = new AnkiConnectClient(transport);

        Optional<AnkiGuiCard> card = client.currentGuiCard();

        assertFalse(card.isPresent());
    }

    @Test
    void currentGuiCardReturnsEmptyForInvalidCardIds() throws IOException {
        RecordingTransport transport = new RecordingTransport();
        transport.responses.add("{\"result\":{\"cardId\":0,\"deckName\":\"Daily English\"},\"error\":null}");
        AnkiConnectClient client = new AnkiConnectClient(transport);

        Optional<AnkiGuiCard> card = client.currentGuiCard();

        assertFalse(card.isPresent());
    }

    @Test
    void latestReviewEaseAfterReadsNewAnkiReviewLogEntry() throws IOException {
        RecordingTransport transport = new RecordingTransport();
        transport.responses.add("{\"result\":{\"42\":[" +
                "{\"id\":100,\"ease\":3}," +
                "{\"id\":110,\"ease\":4}" +
                "]},\"error\":null}");
        AnkiConnectClient client = new AnkiConnectClient(transport);

        Optional<AnkiEase> ease = client.latestReviewEaseAfter(42L, 100L);

        assertTrue(ease.isPresent());
        assertEquals(AnkiEase.EASY, ease.get());
        assertTrue(transport.requests.get(0).contains("\"action\":\"getReviewsOfCards\""));
        assertTrue(transport.requests.get(0).contains("\"cards\":[42]"));
    }

    @Test
    void latestReviewEaseAfterCanBatchMultipleCardsAndReturnLatestEntry() throws IOException {
        RecordingTransport transport = new RecordingTransport();
        transport.responses.add("{\"result\":{" +
                "\"42\":[{\"id\":110,\"ease\":2}]," +
                "\"43\":[{\"id\":130,\"ease\":4}]" +
                "},\"error\":null}");
        AnkiConnectClient client = new AnkiConnectClient(transport);

        Optional<AnkiEase> ease = client.latestReviewEaseAfter(Arrays.asList(42L, 43L), 100L);

        assertTrue(ease.isPresent());
        assertEquals(AnkiEase.EASY, ease.get());
        assertTrue(transport.requests.get(0).contains("\"cards\":[42,43]"));
    }

    @Test
    void latestReviewEaseAtOrAfterAcceptsSameMillisecondReviewWithoutAcceptingOlderEntries() throws IOException {
        RecordingTransport transport = new RecordingTransport();
        transport.responses.add("{\"result\":{\"42\":[" +
                "{\"id\":999,\"ease\":2}," +
                "{\"id\":1000,\"ease\":3}" +
                "]},\"error\":null}");
        AnkiConnectClient client = new AnkiConnectClient(transport);

        Optional<AnkiEase> ease = client.latestReviewEaseAtOrAfter(Arrays.asList(42L), 1000L);

        assertTrue(ease.isPresent());
        assertEquals(AnkiEase.GOOD, ease.get());
    }

    @Test
    void latestReviewEaseIgnoresMalformedReviewLogEntries() throws IOException {
        RecordingTransport transport = new RecordingTransport();
        transport.responses.add("{\"result\":{\"42\":[" +
                "{\"id\":\"bad\",\"ease\":4}," +
                "{\"id\":120,\"ease\":\"bad\"}," +
                "{\"id\":125,\"ease\":99}," +
                "{\"id\":130,\"ease\":4}" +
                "]},\"error\":null}");
        AnkiConnectClient client = new AnkiConnectClient(transport);

        Optional<AnkiEase> ease = client.latestReviewEaseAtOrAfter(Arrays.asList(42L), 100L);

        assertTrue(ease.isPresent());
        assertEquals(AnkiEase.EASY, ease.get());
    }

    @Test
    void latestReviewIdReturnsHighestKnownReviewLogId() throws IOException {
        RecordingTransport transport = new RecordingTransport();
        transport.responses.add("{\"result\":{\"42\":[" +
                "{\"id\":100,\"ease\":3}," +
                "{\"id\":110,\"ease\":4}" +
                "]},\"error\":null}");
        AnkiConnectClient client = new AnkiConnectClient(transport);

        long reviewId = client.latestReviewId(42L);

        assertEquals(110L, reviewId);
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
}
