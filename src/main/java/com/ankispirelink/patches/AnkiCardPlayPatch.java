package com.ankispirelink.patches;

import com.ankispirelink.AnkiSpireLinkMod;
import com.ankispirelink.relics.AnkiLinkRelic;
import com.evacipated.cardcrawl.modthespire.lib.LineFinder;
import com.evacipated.cardcrawl.modthespire.lib.Matcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertLocator;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import javassist.CtBehavior;

public class AnkiCardPlayPatch {
    public static AbstractPlayer storedPlayer;
    public static AbstractMonster storedMonster;
    public static AbstractCard storedCard;
    public static int storedEnergyOnUse;
    private static volatile boolean replayingStoredCard;

    @SpirePatch(clz = AbstractPlayer.class, method = "useCard",
            paramtypez = {AbstractCard.class, AbstractMonster.class, int.class})
    public static class UseCardPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(AbstractPlayer __instance, AbstractCard card,
                                               AbstractMonster monster, int energyOnUse) {
            if (replayingStoredCard) {
                return SpireReturn.Continue();
            }
            AnkiLinkRelic relic = findRelic(__instance);
            if (relic == null || !AnkiSpireLinkMod.isEnabledForCurrentRun()) {
                return SpireReturn.Continue();
            }
            if (card == null || card.type == AbstractCard.CardType.CURSE
                    || card.type == AbstractCard.CardType.STATUS) {
                return SpireReturn.Continue();
            }
            if (relic.isReadyToPlayStoredCard()) {
                return SpireReturn.Continue();
            }
            storedPlayer = __instance;
            storedCard = card;
            storedMonster = monster;
            storedEnergyOnUse = energyOnUse;
            relic.sendReviewPrePlay(card);
            return SpireReturn.Return();
        }

        @SpireInsertPatch(locator = Locator.class)
        public static void Insert(AbstractPlayer __instance, AbstractCard card,
                                  AbstractMonster monster, int energyOnUse) {
            AnkiLinkRelic relic = findRelic(__instance);
            if (replayingStoredCard && relic != null && isStoredReplayCard(card)) {
                relic.applyStoredReviewMultiplier(card);
            }
        }

        @SpirePostfixPatch
        public static void Postfix(AbstractPlayer __instance, AbstractCard card,
                                   AbstractMonster monster, int energyOnUse) {
            if (!replayingStoredCard || !isStoredReplayCard(card)) {
                return;
            }
            AnkiLinkRelic relic = findRelic(__instance);
            if (relic != null) {
                relic.restoreCardStatsAfterUse(card);
            }
        }
    }

    public static boolean autoUseStoredCard() {
        AbstractPlayer player = storedPlayer;
        AbstractCard card = storedCard;
        if (player == null || card == null) {
            clearStoredCard();
            return false;
        }
        AnkiLinkRelic relic = findRelic(player);
        AbstractMonster monster = resolveStoredMonster(card, storedMonster);
        if (requiresTarget(card) && monster == null) {
            if (relic != null) {
                relic.applyStoredReviewRewardsOnly();
            }
            clearStoredCard();
            return true;
        }
        replayingStoredCard = true;
        try {
            player.useCard(card, monster, storedEnergyOnUse);
            return true;
        } finally {
            replayingStoredCard = false;
            if (relic != null) {
                relic.restoreCardStatsAfterUse(card);
                if (relic.isReadyToPlayStoredCard()) {
                    relic.applyStoredReviewRewardsOnly();
                }
            }
            clearStoredCard();
        }
    }

    public static void clearStoredCard() {
        storedPlayer = null;
        storedMonster = null;
        storedCard = null;
        storedEnergyOnUse = 0;
    }

    private static boolean isStoredReplayCard(AbstractCard card) {
        return card != null && card == storedCard;
    }

    private static AnkiLinkRelic findRelic(AbstractPlayer player) {
        if (player == null || player.relics == null) {
            return null;
        }
        for (AbstractRelic relic : player.relics) {
            if (relic instanceof AnkiLinkRelic) {
                return (AnkiLinkRelic) relic;
            }
        }
        return null;
    }

    private static AbstractMonster resolveStoredMonster(AbstractCard card, AbstractMonster monster) {
        if (!requiresTarget(card)) {
            return monster;
        }
        if (monster != null && !monster.isDeadOrEscaped() && !monster.isDying) {
            return monster;
        }
        if (AbstractDungeon.getCurrRoom() == null) {
            return null;
        }
        MonsterGroup monsters = AbstractDungeon.getCurrRoom().monsters;
        if (monsters == null || monsters.monsters == null) {
            return null;
        }
        for (AbstractMonster candidate : monsters.monsters) {
            if (candidate != null && !candidate.isDeadOrEscaped() && !candidate.isDying) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean requiresTarget(AbstractCard card) {
        return card != null
                && (card.target == AbstractCard.CardTarget.ENEMY
                || card.target == AbstractCard.CardTarget.SELF_AND_ENEMY);
    }

    public static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctBehavior) throws Exception {
            Matcher.MethodCallMatcher matcher = new Matcher.MethodCallMatcher(AbstractCard.class, "use");
            return LineFinder.findInOrder(ctBehavior, matcher);
        }
    }
}
