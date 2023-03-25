package com.teguh.tictactoegame.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class GameSelection {
    @NotNull(message = "{Board.Dimension.NotEmpty}")
    @Min(value = 3,  message= "{Board.Dimension.Size.Minimun}")
    private Integer dimension;

    public void setDimension(Integer dimension) {
        this.dimension=dimension;
    }

    public Integer getDimension() {
        return this.dimension;
    }

    @NotNull(message = "{Board.GameType.NotEmpty}")
    private GameType gameType;

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType=gameType;
    }
}
