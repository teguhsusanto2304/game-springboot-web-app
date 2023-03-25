package com.teguh.tictactoegame.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class Player {
    @NotBlank(message = "{Player.Name.NotEmpty}")
   //@NotBlank(message = "Player name cannot be empty")
    private String playername;

    public String getPlayername() {
        return playername;
    }

    public void setPlayername(String playername) {
        this.playername = playername;
    }
}
