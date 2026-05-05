package com.ankispirelink.relics;

import com.ankispirelink.AnkiSpireLinkMod;
import com.ankispirelink.actions.IncreasePlayerMaxHpAction;
import com.ankispirelink.anki.AnkiEase;
import com.ankispirelink.game.AnkiRandomReward;
import com.ankispirelink.game.AnkiReviewReward;
import com.ankispirelink.game.CardStatMath;
import com.ankispirelink.game.RewardPolicy;
import com.ankispirelink.patches.AnkiCardPlayPatch;
import com.ankispirelink.screens.AnkiReviewScreen;
import basemod.abstracts.CustomRelic;
import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.actions.common.GainGoldAction;
import com.megacrit.cardcrawl.actions.common.HealAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.powers.DexterityPower;
import com.megacrit.cardcrawl.powers.LoseDexterityPower;
import com.megacrit.cardcrawl.powers.LoseStrengthPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.vfx.ThoughtBubble;

public class AnkiLinkRelic extends CustomRelic {
    public static final String ID = AnkiSpireLinkMod.JSON_KEY + "AnkiLinkRelic";

    private static final RelicStrings STRINGS = CardCrawlGame.languagePack.getRelicStrings(ID);
    private static final UIStrings REWARD_STRINGS =
            CardCrawlGame.languagePack.getUIString(AnkiSpireLinkMod.JSON_KEY + "RewardMessages");
    private static final UIStrings RANDOM_REWARD_STRINGS =
            CardCrawlGame.languagePack.getUIString(AnkiSpireLinkMod.JSON_KEY + "RandomRewardMessages");
    private static final String IMG = "AnkiSpireLinkResources/images/relics/anki_link.png";
    private static final String OUTLINE = "AnkiSpireLinkResources/images/relics/outline/anki_link.png";

    private AnkiReviewReward pendingReward = RewardPolicy.rewardFor(AnkiEase.AGAIN);
    private AnkiRandomReward pendingRandomReward = AnkiRandomReward.NONE;
    private int pendingMultiplier = 1;
    private boolean readyToPlayStoredCard = false;
    private CardStatSnapshot modifiedCardStats;

    public AnkiLinkRelic() {
        super(ID, texture(IMG), texture(OUTLINE), RelicTier.SPECIAL, LandingSound.MAGICAL);
        this.counter = -1;
    }

    @Override
    public String getUpdatedDescription() {
        if (STRINGS != null && STRINGS.DESCRIPTIONS != null
                && STRINGS.DESCRIPTIONS.length > 0 && STRINGS.DESCRIPTIONS[0] != null) {
            return STRINGS.DESCRIPTIONS[0];
        }
        return "Play cards after completing a real Anki review.";
    }

    @Override
    public AbstractRelic makeCopy() {
        return new AnkiLinkRelic();
    }

    public boolean isReadyToPlayStoredCard() {
        return readyToPlayStoredCard;
    }

    public void cancelPendingStoredCard() {
        restoreModifiedCardStats();
        clearPendingReview();
    }

    public void sendReviewPrePlay(AbstractCard card) {
        this.flash();
        if (AbstractDungeon.player != null && AbstractDungeon.actionManager != null) {
            AbstractDungeon.actionManager.addToTop(new RelicAboveCreatureAction(AbstractDungeon.player, this));
        }
        AnkiReviewScreen.openLoadingReviewScreen(this);
    }

    public void completeReview(AnkiEase ease) {
        this.pendingReward = RewardPolicy.rewardFor(ease);
        this.pendingRandomReward = rollRandomReward(ease);
        this.pendingMultiplier = this.pendingReward.getMultiplier();
        this.readyToPlayStoredCard = true;
        if (!AnkiCardPlayPatch.autoUseStoredCard()) {
            cancelPendingStoredCard();
        }
    }

    public void applyStoredReviewMultiplier(AbstractCard card) {
        if (!this.readyToPlayStoredCard) {
            return;
        }
        if (card != null && card.type != AbstractCard.CardType.CURSE && card.type != AbstractCard.CardType.STATUS) {
            if (this.pendingMultiplier > 1) {
                this.modifiedCardStats = CardStatSnapshot.capture(card);
                multiplyCard(card, this.pendingMultiplier);
            }
            applyRewardActions(this.pendingReward, this.pendingRandomReward);
            showRewardMessage(this.pendingReward, this.pendingRandomReward);
        }
        clearPendingReview();
    }

    public void applyStoredReviewRewardsOnly() {
        if (!this.readyToPlayStoredCard) {
            return;
        }
        applyRewardActions(this.pendingReward, this.pendingRandomReward);
        showRewardMessage(this.pendingReward, this.pendingRandomReward);
        clearPendingReview();
    }

    public void restoreCardStatsAfterUse(AbstractCard card) {
        if (this.modifiedCardStats != null && this.modifiedCardStats.belongsTo(card)) {
            restoreModifiedCardStats();
        }
    }

    private static void multiplyCard(AbstractCard card, int multiplier) {
        if (multiplier <= 1) {
            return;
        }
        card.damage = CardStatMath.multiplyPositive(card.damage, multiplier);
        card.block = CardStatMath.multiplyPositive(card.block, multiplier);
        card.magicNumber = CardStatMath.multiplyPositive(card.magicNumber, multiplier);
        card.multiDamage = CardStatMath.multiplyPositiveValues(card.multiDamage, multiplier);
        card.isDamageModified = card.isDamageModified || multiplier > 1 && (card.damage > card.baseDamage || hasPositiveMultiDamage(card));
        card.isBlockModified = card.isBlockModified || multiplier > 1 && card.block > card.baseBlock;
        card.isMagicNumberModified = card.isMagicNumberModified || multiplier > 1 && card.magicNumber > card.baseMagicNumber;
    }

    private static boolean hasPositiveMultiDamage(AbstractCard card) {
        if (card.multiDamage == null) {
            return false;
        }
        for (int damage : card.multiDamage) {
            if (damage > 0) {
                return true;
            }
        }
        return false;
    }

    private static void applyRewardActions(AnkiReviewReward reward, AnkiRandomReward randomReward) {
        if (reward == null || AbstractDungeon.player == null || AbstractDungeon.actionManager == null) {
            return;
        }
        if (reward.getMaxHp() > 0) {
            queueMaxHpIncrease(reward.getMaxHp());
        }
        if (reward.getEnergy() > 0) {
            AbstractDungeon.actionManager.addToBottom(new GainEnergyAction(reward.getEnergy()));
        }
        if (reward.getBlock() > 0) {
            AbstractDungeon.actionManager.addToBottom(new GainBlockAction(AbstractDungeon.player, reward.getBlock()));
        }
        if (reward.getTemporaryStrength() > 0) {
            AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(
                    AbstractDungeon.player,
                    AbstractDungeon.player,
                    new StrengthPower(AbstractDungeon.player, reward.getTemporaryStrength()),
                    reward.getTemporaryStrength()));
            AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(
                    AbstractDungeon.player,
                    AbstractDungeon.player,
                    new LoseStrengthPower(AbstractDungeon.player, reward.getTemporaryStrength()),
                    reward.getTemporaryStrength()));
        }
        if (reward.getTemporaryDexterity() > 0) {
            AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(
                    AbstractDungeon.player,
                    AbstractDungeon.player,
                    new DexterityPower(AbstractDungeon.player, reward.getTemporaryDexterity()),
                    reward.getTemporaryDexterity()));
            AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(
                    AbstractDungeon.player,
                    AbstractDungeon.player,
                    new LoseDexterityPower(AbstractDungeon.player, reward.getTemporaryDexterity()),
                    reward.getTemporaryDexterity()));
        }
        if (reward.getDraw() > 0) {
            AbstractDungeon.actionManager.addToBottom(new DrawCardAction(AbstractDungeon.player, reward.getDraw()));
        }
        applyRandomReward(randomReward);
    }

    private static void applyRandomReward(AnkiRandomReward randomReward) {
        if (randomReward == null || !randomReward.isPresent() || AbstractDungeon.player == null) {
            return;
        }
        if (randomReward.getMaxHp() > 0) {
            queueMaxHpIncrease(randomReward.getMaxHp());
        }
        if (randomReward.getHeal() > 0 && AbstractDungeon.actionManager != null) {
            AbstractDungeon.actionManager.addToBottom(new HealAction(
                    AbstractDungeon.player,
                    AbstractDungeon.player,
                    randomReward.getHeal()));
        }
        if (randomReward.getGold() > 0 && AbstractDungeon.actionManager != null) {
            AbstractDungeon.actionManager.addToBottom(new GainGoldAction(randomReward.getGold()));
        }
        if (randomReward.getType() == AnkiRandomReward.Type.RELIC) {
            grantRandomRelic(randomReward.getRelicTier());
        }
    }

    private static void queueMaxHpIncrease(int amount) {
        if (amount <= 0 || AbstractDungeon.player == null) {
            return;
        }
        if (AbstractDungeon.actionManager != null) {
            AbstractDungeon.actionManager.addToBottom(new IncreasePlayerMaxHpAction(AbstractDungeon.player, amount));
        } else {
            AbstractDungeon.player.increaseMaxHp(amount, true);
        }
    }

    private static void grantRandomRelic(AbstractRelic.RelicTier tier) {
        if (tier == null || AbstractDungeon.getCurrRoom() == null || AbstractDungeon.player == null) {
            return;
        }
        try {
            AbstractRelic relic = AbstractDungeon.returnRandomScreenlessRelic(tier);
            if (relic != null && !AbstractDungeon.player.hasRelic(relic.relicId)) {
                AbstractDungeon.getCurrRoom().spawnRelicAndObtain(
                        AbstractDungeon.player.drawX,
                        AbstractDungeon.player.drawY,
                        relic);
            }
        } catch (RuntimeException ignored) {
            // Relic pools can be exhausted or altered by other mods; skip the bonus rather than breaking combat.
        }
    }

    private static void showRewardMessage(AnkiReviewReward reward, AnkiRandomReward randomReward) {
        if (reward == null || AbstractDungeon.player == null || AbstractDungeon.effectList == null) {
            return;
        }
        AbstractDungeon.effectList.add(new ThoughtBubble(
                AbstractDungeon.player.dialogX,
                AbstractDungeon.player.dialogY,
                3.0F,
                rewardMessage(reward, randomReward),
                true));
    }

    private static String rewardMessage(AnkiReviewReward reward, AnkiRandomReward randomReward) {
        String message;
        if (REWARD_STRINGS != null && REWARD_STRINGS.TEXT != null
                && reward.getMessageIndex() >= 0
                && reward.getMessageIndex() < REWARD_STRINGS.TEXT.length
                && REWARD_STRINGS.TEXT[reward.getMessageIndex()] != null) {
            message = REWARD_STRINGS.TEXT[reward.getMessageIndex()];
        } else {
            message = reward.getFallbackMessage();
        }
        if (randomReward != null && randomReward.isPresent()) {
            message += " / " + randomRewardMessage(randomReward);
        }
        return message;
    }

    private static String randomRewardMessage(AnkiRandomReward randomReward) {
        String template = randomReward.getFallbackMessage();
        if (RANDOM_REWARD_STRINGS != null && RANDOM_REWARD_STRINGS.TEXT != null
                && randomReward.getMessageIndex() >= 0
                && randomReward.getMessageIndex() < RANDOM_REWARD_STRINGS.TEXT.length
                && RANDOM_REWARD_STRINGS.TEXT[randomReward.getMessageIndex()] != null) {
            template = RANDOM_REWARD_STRINGS.TEXT[randomReward.getMessageIndex()];
        }
        return template
                .replace("{maxHp}", String.valueOf(randomReward.getMaxHp()))
                .replace("{heal}", String.valueOf(randomReward.getHeal()))
                .replace("{gold}", String.valueOf(randomReward.getGold()));
    }

    private static AnkiRandomReward rollRandomReward(AnkiEase ease) {
        return RewardPolicy.randomBonusFor(ease, randomInt(99), randomInt(99), randomInt(99));
    }

    private static int randomInt(int maxInclusive) {
        if (AbstractDungeon.miscRng != null) {
            return AbstractDungeon.miscRng.random(maxInclusive);
        }
        return maxInclusive;
    }

    private static Texture texture(String path) {
        return ImageMaster.loadImage(path);
    }

    private void clearPendingReview() {
        this.readyToPlayStoredCard = false;
        this.pendingReward = RewardPolicy.rewardFor(AnkiEase.AGAIN);
        this.pendingRandomReward = AnkiRandomReward.NONE;
        this.pendingMultiplier = 1;
        this.counter = -1;
    }

    private void restoreModifiedCardStats() {
        if (this.modifiedCardStats != null) {
            this.modifiedCardStats.restore();
            this.modifiedCardStats = null;
        }
    }

    private static final class CardStatSnapshot {
        private final AbstractCard card;
        private final int damage;
        private final int block;
        private final int magicNumber;
        private final int[] multiDamage;
        private final boolean damageModified;
        private final boolean blockModified;
        private final boolean magicNumberModified;

        private CardStatSnapshot(AbstractCard card) {
            this.card = card;
            this.damage = card.damage;
            this.block = card.block;
            this.magicNumber = card.magicNumber;
            this.multiDamage = card.multiDamage == null ? null : card.multiDamage.clone();
            this.damageModified = card.isDamageModified;
            this.blockModified = card.isBlockModified;
            this.magicNumberModified = card.isMagicNumberModified;
        }

        private static CardStatSnapshot capture(AbstractCard card) {
            return new CardStatSnapshot(card);
        }

        private boolean belongsTo(AbstractCard candidate) {
            return candidate == this.card;
        }

        private void restore() {
            this.card.damage = this.damage;
            this.card.block = this.block;
            this.card.magicNumber = this.magicNumber;
            this.card.multiDamage = this.multiDamage == null ? null : this.multiDamage.clone();
            this.card.isDamageModified = this.damageModified;
            this.card.isBlockModified = this.blockModified;
            this.card.isMagicNumberModified = this.magicNumberModified;
        }
    }
}
