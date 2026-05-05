package com.ankispirelink.game;

import com.ankispirelink.anki.AnkiEase;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RewardPolicyTest {

    @Test
    void mapsAnkiEaseToCardMultiplier() {
        assertEquals(1, RewardPolicy.multiplierFor(AnkiEase.AGAIN));
        assertEquals(1, RewardPolicy.multiplierFor(AnkiEase.HARD));
        assertEquals(2, RewardPolicy.multiplierFor(AnkiEase.GOOD));
        assertEquals(4, RewardPolicy.multiplierFor(AnkiEase.EASY));
    }

    @Test
    void labelsRewardsForTheQuizScreen() {
        assertEquals("Again: max HP +1.", RewardPolicy.labelFor(AnkiEase.AGAIN));
        assertEquals("Hard: gain 1 Energy and 3 Block.", RewardPolicy.labelFor(AnkiEase.HARD));
        assertEquals("Good: x2, draw 1, temporary Strength +1.", RewardPolicy.labelFor(AnkiEase.GOOD));
        assertEquals("Easy: x4, gain 1 Energy, draw 1, temporary Strength and Dexterity +1.",
                RewardPolicy.labelFor(AnkiEase.EASY));
    }

    @Test
    void mapsAnkiEaseToSpecialRewards() {
        AnkiReviewReward again = RewardPolicy.rewardFor(AnkiEase.AGAIN);
        assertEquals(1, again.getMultiplier());
        assertEquals(0, again.getBlock());
        assertEquals(1, again.getMaxHp());

        AnkiReviewReward hard = RewardPolicy.rewardFor(AnkiEase.HARD);
        assertEquals(1, hard.getMultiplier());
        assertEquals(1, hard.getEnergy());
        assertEquals(3, hard.getBlock());

        AnkiReviewReward good = RewardPolicy.rewardFor(AnkiEase.GOOD);
        assertEquals(2, good.getMultiplier());
        assertEquals(1, good.getDraw());
        assertEquals(1, good.getTemporaryStrength());

        AnkiReviewReward easy = RewardPolicy.rewardFor(AnkiEase.EASY);
        assertEquals(4, easy.getMultiplier());
        assertEquals(1, easy.getEnergy());
        assertEquals(1, easy.getDraw());
        assertEquals(1, easy.getTemporaryStrength());
        assertEquals(1, easy.getTemporaryDexterity());
    }

    @Test
    void rollsRandomBonusWithEasyRelicChanceCappedAtFifteenPercent() {
        assertEquals(AnkiRandomReward.Type.RELIC,
                RewardPolicy.randomBonusFor(AnkiEase.EASY, 14, 0, 0).getType());
        assertEquals(AnkiRandomReward.Type.MAX_HP,
                RewardPolicy.randomBonusFor(AnkiEase.EASY, 15, 2, 0).getType());

        AnkiRandomReward outsideRelicChance = RewardPolicy.randomBonusFor(AnkiEase.EASY, 15, 0, 0);
        assertFalse(outsideRelicChance.getType() == AnkiRandomReward.Type.RELIC);
    }

    @Test
    void rollsRandomMaxHpBetweenThreeAndFive() {
        assertEquals(3, RewardPolicy.randomBonusFor(AnkiEase.GOOD, 8, 0, 0).getMaxHp());
        assertEquals(4, RewardPolicy.randomBonusFor(AnkiEase.GOOD, 8, 1, 0).getMaxHp());
        assertEquals(5, RewardPolicy.randomBonusFor(AnkiEase.GOOD, 8, 2, 0).getMaxHp());
    }

    @Test
    void rollsRelicTierFromCommonUncommonRareWeights() {
        assertEquals(AbstractRelic.RelicTier.COMMON,
                RewardPolicy.randomBonusFor(AnkiEase.EASY, 0, 0, 69).getRelicTier());
        assertEquals(AbstractRelic.RelicTier.UNCOMMON,
                RewardPolicy.randomBonusFor(AnkiEase.EASY, 0, 0, 70).getRelicTier());
        assertEquals(AbstractRelic.RelicTier.RARE,
                RewardPolicy.randomBonusFor(AnkiEase.EASY, 0, 0, 95).getRelicTier());
    }
}
