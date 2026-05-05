package com.ankispirelink.game;

import com.megacrit.cardcrawl.relics.AbstractRelic;

public final class AnkiRandomReward {
    public enum Type {
        NONE,
        MAX_HP,
        HEAL,
        GOLD,
        RELIC
    }

    public static final AnkiRandomReward NONE = new AnkiRandomReward(
            Type.NONE, 0, 0, 0, null, -1, "");

    private final Type type;
    private final int maxHp;
    private final int heal;
    private final int gold;
    private final AbstractRelic.RelicTier relicTier;
    private final int messageIndex;
    private final String fallbackMessage;

    private AnkiRandomReward(Type type, int maxHp, int heal, int gold,
                             AbstractRelic.RelicTier relicTier, int messageIndex,
                             String fallbackMessage) {
        this.type = type;
        this.maxHp = maxHp;
        this.heal = heal;
        this.gold = gold;
        this.relicTier = relicTier;
        this.messageIndex = messageIndex;
        this.fallbackMessage = fallbackMessage;
    }

    public static AnkiRandomReward maxHp(int amount) {
        return new AnkiRandomReward(Type.MAX_HP, amount, 0, 0, null, 0,
                "Bonus: max HP +" + amount + ".");
    }

    public static AnkiRandomReward heal(int amount) {
        return new AnkiRandomReward(Type.HEAL, 0, amount, 0, null, 1,
                "Bonus: heal " + amount + " HP.");
    }

    public static AnkiRandomReward gold(int amount) {
        return new AnkiRandomReward(Type.GOLD, 0, 0, amount, null, 2,
                "Bonus: gain " + amount + " gold.");
    }

    public static AnkiRandomReward relic(AbstractRelic.RelicTier tier) {
        return new AnkiRandomReward(Type.RELIC, 0, 0, 0, tier, 3,
                "Bonus: gain a random relic.");
    }

    public Type getType() {
        return type;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getHeal() {
        return heal;
    }

    public int getGold() {
        return gold;
    }

    public AbstractRelic.RelicTier getRelicTier() {
        return relicTier;
    }

    public int getMessageIndex() {
        return messageIndex;
    }

    public String getFallbackMessage() {
        return fallbackMessage;
    }

    public boolean isPresent() {
        return type != Type.NONE;
    }
}
