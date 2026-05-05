package com.ankispirelink.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;

public final class IncreasePlayerMaxHpAction extends AbstractGameAction {
    private final int amount;

    public IncreasePlayerMaxHpAction(AbstractPlayer player, int amount) {
        this.target = player;
        this.amount = amount;
        this.actionType = ActionType.HEAL;
    }

    @Override
    public void update() {
        if (target instanceof AbstractPlayer && amount > 0) {
            ((AbstractPlayer) target).increaseMaxHp(amount, true);
        }
        isDone = true;
    }
}
