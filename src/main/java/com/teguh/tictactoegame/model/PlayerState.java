package com.teguh.tictactoegame.model;

public enum PlayerState {
    IN_PROGRESS,
    WAIT_FOR_OPPONENT,
    WIN,
    LOSS,
    DRAW;

    public boolean isWaitForOpponent() {
        return this == WAIT_FOR_OPPONENT;
    }

    public boolean isInProgress() {
        return this == IN_PROGRESS;
    }

    public boolean isWin() {
        return this == WIN;
    }

    public boolean isLoss() {
        return this == LOSS;
    }

    public boolean isDraw() {
        return this == DRAW;
    }

    public boolean isGameOver() {
        return this == WIN || this == LOSS || this == DRAW;
    }
}
