package com.ankispirelink.anki;

public final class AnkiGuiCard {
    private final long cardId;
    private final String deckName;

    public AnkiGuiCard(long cardId, String deckName) {
        this.cardId = cardId;
        this.deckName = deckName == null ? "" : deckName;
    }

    public long getCardId() {
        return cardId;
    }

    public String getDeckName() {
        return deckName;
    }
}
