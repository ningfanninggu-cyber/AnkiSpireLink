package com.ankispirelink.anki;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class AnkiConnectClient {
    private static final int API_VERSION = 6;
    private static final Gson GSON = new Gson();

    private final AnkiConnectTransport transport;
    private volatile String deckName = "";

    public AnkiConnectClient(AnkiConnectTransport transport) {
        this.transport = transport;
    }

    public void setDeckName(String deckName) {
        this.deckName = deckName == null ? "" : deckName.trim();
    }

    public String getDeckName() {
        return deckName;
    }

    public boolean startDeckReview(String deckName) {
        String name = deckName == null ? "" : deckName.trim();
        if (name.isEmpty()) {
            return false;
        }
        JsonObject params = new JsonObject();
        params.addProperty("name", name);
        JsonElement result = invoke("guiDeckReview", params);
        Boolean started = getBoolean(result);
        return started != null && started;
    }

    public Optional<AnkiGuiCard> currentGuiCard() {
        JsonElement result = invoke("guiCurrentCard", null);
        if (result == null || !result.isJsonObject()) {
            return Optional.empty();
        }
        JsonObject card = result.getAsJsonObject();
        Long cardId = getLong(card, "cardId");
        if (cardId == null || cardId <= 0L) {
            return Optional.empty();
        }
        return Optional.of(new AnkiGuiCard(cardId, getString(card, "deckName", "")));
    }

    public long latestReviewId(long cardId) {
        long latest = 0L;
        for (ReviewLogEntry entry : reviewLogEntries(cardId)) {
            if (entry.reviewId > latest) {
                latest = entry.reviewId;
            }
        }
        return latest;
    }

    public Optional<AnkiEase> latestReviewEaseAfter(long cardId, long baselineReviewId) {
        return latestReviewEaseAfter(Collections.singletonList(cardId), baselineReviewId);
    }

    public Optional<AnkiEase> latestReviewEaseAfter(Iterable<Long> cardIds, long baselineReviewId) {
        return latestReviewEase(cardIds, baselineReviewId, false);
    }

    public Optional<AnkiEase> latestReviewEaseAtOrAfter(Iterable<Long> cardIds, long baselineReviewId) {
        return latestReviewEase(cardIds, baselineReviewId, true);
    }

    private Optional<AnkiEase> latestReviewEase(Iterable<Long> cardIds, long baselineReviewId,
                                                boolean includeBaseline) {
        ReviewLogEntry latest = null;
        for (ReviewLogEntry entry : reviewLogEntries(cardIds)) {
            if ((includeBaseline ? entry.reviewId >= baselineReviewId : entry.reviewId > baselineReviewId)
                    && (latest == null || entry.reviewId > latest.reviewId)) {
                latest = entry;
            }
        }
        if (latest == null) {
            return Optional.empty();
        }
        return Optional.of(AnkiEase.fromAnkiValue(latest.ease));
    }

    private List<ReviewLogEntry> reviewLogEntries(long cardId) {
        return reviewLogEntries(Collections.singletonList(cardId));
    }

    private List<ReviewLogEntry> reviewLogEntries(Iterable<Long> cardIds) {
        Set<Long> uniqueCardIds = new LinkedHashSet<>();
        if (cardIds != null) {
            for (Long cardId : cardIds) {
                if (cardId != null && cardId > 0L) {
                    uniqueCardIds.add(cardId);
                }
            }
        }
        if (uniqueCardIds.isEmpty()) {
            return Collections.emptyList();
        }

        JsonArray cards = new JsonArray();
        for (Long cardId : uniqueCardIds) {
            cards.add(cardId);
        }

        JsonObject params = new JsonObject();
        params.add("cards", cards);

        JsonElement result = invoke("getReviewsOfCards", params);
        if (result == null || !result.isJsonObject()) {
            return Collections.emptyList();
        }

        List<ReviewLogEntry> reviewLogEntries = new ArrayList<>();
        JsonObject allEntries = result.getAsJsonObject();
        for (Long cardId : uniqueCardIds) {
            JsonElement entries = allEntries.get(String.valueOf(cardId));
            if (entries == null || !entries.isJsonArray()) {
                continue;
            }
            for (JsonElement item : entries.getAsJsonArray()) {
                if (item != null && item.isJsonObject()) {
                    Optional<ReviewLogEntry> entry = reviewLogEntry(item.getAsJsonObject());
                    if (entry.isPresent()) {
                        reviewLogEntries.add(entry.get());
                    }
                }
            }
        }
        return reviewLogEntries;
    }

    private Optional<ReviewLogEntry> reviewLogEntry(JsonObject object) {
        Long reviewId = getLong(object, "id");
        Integer ease = getInt(object, "ease");
        if (reviewId == null || reviewId <= 0L || ease == null || !AnkiEase.isKnownAnkiValue(ease)) {
            return Optional.empty();
        }
        return Optional.of(new ReviewLogEntry(reviewId, ease));
    }

    private JsonElement invoke(String action, JsonObject params) {
        JsonObject request = new JsonObject();
        request.addProperty("action", action);
        request.addProperty("version", API_VERSION);
        request.add("params", params == null ? new JsonObject() : params);
        try {
            String responseJson = transport.post(GSON.toJson(request));
            JsonObject response = new JsonParser().parse(responseJson).getAsJsonObject();
            JsonElement error = response.get("error");
            if (error != null && !error.isJsonNull()) {
                return null;
            }
            return response.get("result");
        } catch (RuntimeException | IOException e) {
            return null;
        }
    }

    private static String getString(JsonObject object, String name, String fallback) {
        JsonElement element = object.get(name);
        if (element == null || element.isJsonNull()) {
            return fallback;
        }
        try {
            return element.getAsString();
        } catch (RuntimeException e) {
            return fallback;
        }
    }

    private static Long getLong(JsonObject object, String name) {
        JsonElement element = object.get(name);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        try {
            return element.getAsLong();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static Integer getInt(JsonObject object, String name) {
        JsonElement element = object.get(name);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        try {
            return element.getAsInt();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static Boolean getBoolean(JsonElement element) {
        if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) {
            return null;
        }
        try {
            return element.getAsBoolean();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static final class ReviewLogEntry {
        private final long reviewId;
        private final int ease;

        private ReviewLogEntry(long reviewId, int ease) {
            this.reviewId = reviewId;
            this.ease = ease;
        }
    }
}
