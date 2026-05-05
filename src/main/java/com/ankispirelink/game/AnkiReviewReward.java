package com.ankispirelink.game;

public final class AnkiReviewReward {
    private final int multiplier;
    private final int block;
    private final int maxHp;
    private final int energy;
    private final int draw;
    private final int temporaryStrength;
    private final int temporaryDexterity;
    private final int messageIndex;
    private final String fallbackMessage;

    AnkiReviewReward(int multiplier, int block, int maxHp, int energy, int draw,
                     int temporaryStrength, int temporaryDexterity,
                     int messageIndex, String fallbackMessage) {
        this.multiplier = multiplier;
        this.block = block;
        this.maxHp = maxHp;
        this.energy = energy;
        this.draw = draw;
        this.temporaryStrength = temporaryStrength;
        this.temporaryDexterity = temporaryDexterity;
        this.messageIndex = messageIndex;
        this.fallbackMessage = fallbackMessage;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public int getBlock() {
        return block;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getEnergy() {
        return energy;
    }

    public int getDraw() {
        return draw;
    }

    public int getTemporaryStrength() {
        return temporaryStrength;
    }

    public int getTemporaryDexterity() {
        return temporaryDexterity;
    }

    public int getMessageIndex() {
        return messageIndex;
    }

    public String getFallbackMessage() {
        return fallbackMessage;
    }
}
