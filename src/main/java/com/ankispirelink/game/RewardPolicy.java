package com.ankispirelink.game;

import com.ankispirelink.anki.AnkiEase;
import com.megacrit.cardcrawl.relics.AbstractRelic;

public final class RewardPolicy {
    private RewardPolicy() {
    }

    public static AnkiReviewReward rewardFor(AnkiEase ease) {
        if (ease == AnkiEase.EASY) {
            return new AnkiReviewReward(4, 0, 0, 1, 1, 1, 1, 3,
                    "Easy: x4, gain 1 Energy, draw 1, temporary Strength and Dexterity +1.");
        }
        if (ease == AnkiEase.GOOD) {
            return new AnkiReviewReward(2, 0, 0, 0, 1, 1, 0, 2,
                    "Good: x2, draw 1, temporary Strength +1.");
        }
        if (ease == AnkiEase.HARD) {
            return new AnkiReviewReward(1, 3, 0, 1, 0, 0, 0, 1,
                    "Hard: gain 1 Energy and 3 Block.");
        }
        return new AnkiReviewReward(1, 0, 1, 0, 0, 0, 0, 0,
                "Again: max HP +1.");
    }

    public static AnkiRandomReward randomBonusFor(AnkiEase ease, int percentRoll,
                                                  int amountRoll, int relicTierRoll) {
        BonusOdds odds = oddsFor(ease);
        int normalizedPercent = normalize(percentRoll, 100);
        int cursor = odds.relicChance;
        if (normalizedPercent < cursor) {
            return AnkiRandomReward.relic(relicTierFor(relicTierRoll));
        }

        cursor += odds.maxHpChance;
        if (normalizedPercent < cursor) {
            return AnkiRandomReward.maxHp(3 + normalize(amountRoll, 3));
        }

        cursor += odds.healChance;
        if (normalizedPercent < cursor) {
            return AnkiRandomReward.heal(odds.healMin + normalize(amountRoll, odds.healRange()));
        }

        cursor += odds.goldChance;
        if (normalizedPercent < cursor) {
            return AnkiRandomReward.gold(odds.goldMin + normalize(amountRoll, odds.goldRange()));
        }

        return AnkiRandomReward.NONE;
    }

    public static int multiplierFor(AnkiEase ease) {
        return rewardFor(ease).getMultiplier();
    }

    public static String labelFor(AnkiEase ease) {
        return rewardFor(ease).getFallbackMessage();
    }

    private static BonusOdds oddsFor(AnkiEase ease) {
        if (ease == AnkiEase.EASY) {
            return new BonusOdds(15, 10, 10, 10, 5, 8, 12, 22);
        }
        if (ease == AnkiEase.GOOD) {
            return new BonusOdds(8, 8, 12, 15, 4, 6, 10, 18);
        }
        if (ease == AnkiEase.HARD) {
            return new BonusOdds(3, 6, 10, 14, 3, 5, 8, 14);
        }
        return new BonusOdds(1, 4, 8, 12, 2, 3, 6, 10);
    }

    private static AbstractRelic.RelicTier relicTierFor(int relicTierRoll) {
        int roll = normalize(relicTierRoll, 100);
        if (roll < 70) {
            return AbstractRelic.RelicTier.COMMON;
        }
        if (roll < 95) {
            return AbstractRelic.RelicTier.UNCOMMON;
        }
        return AbstractRelic.RelicTier.RARE;
    }

    private static int normalize(int value, int bound) {
        if (bound <= 1) {
            return 0;
        }
        int normalized = value % bound;
        return normalized < 0 ? normalized + bound : normalized;
    }

    private static final class BonusOdds {
        private final int relicChance;
        private final int maxHpChance;
        private final int healChance;
        private final int goldChance;
        private final int healMin;
        private final int healMax;
        private final int goldMin;
        private final int goldMax;

        private BonusOdds(int relicChance, int maxHpChance, int healChance, int goldChance,
                          int healMin, int healMax, int goldMin, int goldMax) {
            this.relicChance = relicChance;
            this.maxHpChance = maxHpChance;
            this.healChance = healChance;
            this.goldChance = goldChance;
            this.healMin = healMin;
            this.healMax = healMax;
            this.goldMin = goldMin;
            this.goldMax = goldMax;
        }

        private int healRange() {
            return healMax - healMin + 1;
        }

        private int goldRange() {
            return goldMax - goldMin + 1;
        }
    }
}
