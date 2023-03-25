package com.teguh.tictactoegame.model;

import java.util.List;

public class Game {
    private Board board;
    private PlayerState playerState;
    private boolean playerGoFirst;
    private boolean nextMoveX;
    private String existingOpponent;
    private GameSelection gameSelection;
    private boolean yourTurn;
    public Game() {
    }


    public void startNew(int dimension) {
        System.out.print("startNew");
        playerGoFirst = true;
        nextMoveX = true;
        playerState = PlayerState.IN_PROGRESS;
        board = new Board(dimension);
        System.out.print(" startNew dimension:"+dimension+" board: "+board );
    }


    public Board getBoard() {
        return board;
    }


    public void markTile( String tileId ) {
        System.out.print("markTile tileId"+tileId+", board: "+board);
        setTileValue( board.get( tileId ) );
    }


    public PlayerState markTileHuman( String tileId,	boolean goFirst) {

        System.out.print("markTileHuman tileId"+tileId);
        return setTileValueHuman( board.get( tileId ) ,goFirst);
    }

    public void markTileRandom() {
        setTileValue( board.getRandomAvailable() );
    }


    private void setTileValue( BoxBoard box ) {

        if ( isGameOver() || !box.isEmpty() ) {
            return;
        }

        box.setValue( nextMoveX ? BoxBoard.Value.X : BoxBoard.Value.O );

        nextMoveX = !nextMoveX;

        BoxBoard.Value winValue = evaluateWinValue();

        if ( winValue != null ) {
            BoxBoard.Value playerValue = playerGoFirst ? BoxBoard.Value.X : BoxBoard.Value.O;
            playerState = winValue == playerValue ? PlayerState.WIN : PlayerState.LOSS;
        } else {
            playerState = board.isFull() ? PlayerState.DRAW : PlayerState.IN_PROGRESS;
        }
    }


    private 	PlayerState   setTileValueHuman( BoxBoard box,boolean goFirst) {

        if ( isGameOver() || !box.isEmpty() ) {
            return null;
        }

        box.setValue( nextMoveX ? BoxBoard.Value.X : BoxBoard.Value.O );

        nextMoveX = !nextMoveX;
        PlayerState pState;
        BoxBoard.Value winValue = evaluateWinValue();
        if ( winValue != null ) {
            BoxBoard.Value playerValue = goFirst ? BoxBoard.Value.X : BoxBoard.Value.O;
            //after we know the player value is X or O, then we compare with winValue, if it the same, then classify the player to WIN, otherwise LOSS
            pState = winValue == playerValue ? PlayerState.WIN : PlayerState.LOSS;
        } else {
            //winValue is not null means the game can be still in progress with available empty board square or finished with DRAW condtion if board square already full
            pState = board.isFull() ? PlayerState.DRAW : PlayerState.IN_PROGRESS;

        }

        return pState;
    }


    private BoxBoard.Value evaluateWinValue() {

        List<List<BoxBoard>> allLines = board.getAllEligibleWinCandidateLines();

        for ( List<BoxBoard> line : allLines ) {

            BoxBoard last = line.get(board.getDimension()-1 );

            if ( last.isEmpty() ) {
                continue;
            }

            if ( line.stream().allMatch( t -> t.getValue() == last.getValue() ) ) {
                return last.getValue();
            }
        }

        return null;
    }

    public void setPlayerGoFirst( boolean flag ) {
        this.playerGoFirst = flag;
    }

    public boolean isPlayerGoFirst() {
        return playerGoFirst;
    }

    public void setYourTurn( boolean yourTurn ) {
        this.yourTurn = yourTurn;
    }

    public boolean isYourTurn() {
        return yourTurn;
    }

    public boolean isGameOver() {
        return playerState.isGameOver();
    }


    public PlayerState getPlayerState() {
        return playerState;
    }

    public GameSelection getGameSelection() {
        return gameSelection;
    }

    public void setGameSelection(GameSelection gameSelection) {
        this.gameSelection= gameSelection;
    }

    public String getExistingOpponent() {
        return existingOpponent;
    }

    public void setExistingOpponent(String existingOpponent) {
        this.existingOpponent= existingOpponent;
    }
}
