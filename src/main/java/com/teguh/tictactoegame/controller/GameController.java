package com.teguh.tictactoegame.controller;

import com.teguh.tictactoegame.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
@SessionAttributes("game")
public class GameController {
    //PLAYER_STATE is a session scope attribute key to identify player state
    public final static String PLAYER_STATE="PLAYER_STATE";

    //FIRST_PLAYER_NAME is a application scope attribute key to identify who is first player name
    public final static String FIRST_PLAYER_NAME="FIRST_PLAYER_NAME";

    //USERNAME is a session scope attribute key to store the logged in username
    public final static String PLAYERNAME="PLAYERNAME";

    //OPPONENT_DIMENSION is a application scope attribute key to keep dimension for First Player to be shared to Second Player on HUMAN opponent
    public final static String OPPONENT_DIMENSION="OPPONENT_DIMENSION";

    @Autowired
    private ServletContext servletContext;

    /**
     * RequestMapping to handle request if user choose to play with COMPUTER
     */
    @RequestMapping(value = "/computer", method = RequestMethod.GET)
    public String computer(@ModelAttribute("game" ) Game game , Model model  ) {
        return "play";
    }

    /**
     * RequestMapping to handle request for refreshing page on realtime HUMAN opponent move
     */
    @RequestMapping(value = "/play", method = RequestMethod.GET)
    public String play( ) {
        System.out.println("/play GET ");
        return "play";
    }

    /**
     * RequestMapping to handle request if user choose to play with HUMAN
     */
    @RequestMapping(value = "/human", method = RequestMethod.POST)
    public String human(@ModelAttribute("game" ) Game game, @ModelAttribute("user" ) Player player, HttpSession httpSession , Model model  ) {
        System.out.println("/human POST player: "+player);
        initPlayerStateSession(game,httpSession);
        return "play";
    }

    /**
     * Method to init player state on the new game
     */
    private   void initPlayerStateSession( Game game, HttpSession httpSession ) {
        if(game.isYourTurn() ) {
            httpSession.setAttribute(PLAYER_STATE, PlayerState.IN_PROGRESS);
        } else {
            httpSession.setAttribute(PLAYER_STATE, PlayerState.WAIT_FOR_OPPONENT);
        }
    }


    /**
     * Method to init player turn on new game
     */
    private   void initPlayerGoFirstSession( Game game, HttpSession httpSession ) {
        Object firstPlayerObj = servletContext.getAttribute(FIRST_PLAYER_NAME);
        if(firstPlayerObj!=null) {
            String playerName = (String)firstPlayerObj;
            if(playerName.equals((String)httpSession.getAttribute(PLAYERNAME))) {
                game.setYourTurn(true);
            } else {
                game.setYourTurn(false);
            }

        }
    }


    @RequestMapping(value = "/playMove", method = RequestMethod.GET)
    public String markTileGet(
            @ModelAttribute("player" ) Player player,
            @ModelAttribute("game" ) Game game,
            @RequestParam("tile_id") String tileId,
            @RequestParam(value = "self", required = false, defaultValue = "false") boolean self,
            @RequestParam(value = "new_game", required = false, defaultValue = "false") boolean newGame,
            @RequestParam(value = "player_go_first", required = false, defaultValue = "false") boolean playerGoFirst ,
            HttpSession httpSession
    ) {
        String playername = (String) httpSession.getAttribute(PLAYERNAME);
        GameType gameType = game.getGameSelection().getGameType();

        //this goFirst variable used only by HUMAN Opponent to check the Player has the chance to run the first move on game, if goFirst true then it is the First Player
        boolean goFirst = isFirstPlayer(servletContext,playername);

        if ( newGame ) {
            // player clicked the NEW GAME
            if(gameType.equals(GameType.COMPUTER)) {
                game.startNew(game.getGameSelection().getDimension());

                // set playerGoFirst from checkbox option on play.html
                game.setPlayerGoFirst( playerGoFirst );
                if ( !playerGoFirst ) {
                    // give computer an advantage by always placing X in the middle as its first move
                    game.markTile( "1-1" );
                }
            } else {
                Object dimensionObj = servletContext.getAttribute(OPPONENT_DIMENSION);
                // get the shared dimension from servletContext for HUMAN type opponent
                if(dimensionObj!=null) {
                    // if shared dimension exist, means First Player already create game, and Second Player can start new game with First Player dimension, not by Second Player dimension
                    game.startNew((Integer)dimensionObj);
                } else {
                    // if shared dimension not exist, means First Player just create a new game with His dimension
                    game.startNew(game.getGameSelection().getDimension());
                }

                // reset the state of player
                initPlayerGoFirstSession(game,httpSession);
                initPlayerStateSession(game,httpSession);

            }

        } else {
            // player clicked the Tic Tac Toe square
            if(gameType.equals(GameType.COMPUTER)) {

                //if game type is COMPUTER opponent, then call markTile() to mark the square and set the value
                game.markTile( tileId ); // Player Turn

                //computer will execute its move by calling markTileRandom() to mark the computer square and set the value
                game.markTileRandom(); // Computer Turn

                //set player turn always true, because in COMPUTER opponent no need to wait opponent turn, as computer has extremely fast brain process then human (xixixixix...)
                game.setYourTurn(true);
            } else {
                //set your next turn to wait opponent move, because Human still need time for thinking process on his brain (xixixixi...)
                game.setYourTurn(!game.isYourTurn());

                //if game type is HUMAN opponent, then call markTileHuman() to mark the square and set the value, with goFirst variable from session to evaluate Win
                PlayerState pState = game.markTileHuman(tileId, goFirst);
                if(pState!=null) {
                    //System.out.println("markTile() getUsername: "+username+", goFirst: "+goFirst+", PLAYER_STATE: "+pState.toString());

                    //if it not your turn, then better you can see 'Waiting for opponent move' message
                    if(!game.isYourTurn()&&pState.isInProgress()) {
                        pState = PlayerState.WAIT_FOR_OPPONENT;
                    }
                    httpSession.setAttribute(PLAYER_STATE, pState);

                }
            }
        }

        return "play";
    }

    /**
     * Method to initiliaze game object
     */
    @ModelAttribute("game")
    public Game populateGame( ) {
        return new Game();
    }

    /**
     * Method to check whether He is First Player or not
     */
    private boolean isFirstPlayer(ServletContext servletContext, String username ) {
        boolean firstPlayer = false;
        Object firstPlayerObj = servletContext.getAttribute(FIRST_PLAYER_NAME);
        if(firstPlayerObj!=null) {
            String playerName = (String) firstPlayerObj;
            if(playerName.equals(username)) {
                firstPlayer=true;
            } else {
                firstPlayer=false;
            }
        } else {
            firstPlayer=false;
        }
        return firstPlayer;
    }

    /**
     * MessageMapping to send the realtime message on HUMAN opponent game, since Play With Human should have realtime experience, then we use Websocket to send player movement
     */
    @MessageMapping("/move")
    @SendTo("/topic/move")
    public ActionMessage sendMove(@ModelAttribute("game" ) Game game, ActionMessage actionMessage, SimpMessageHeaderAccessor headerAccessor
    ) {
        MessageType messageType = actionMessage.getMessageType();
        if(messageType.equals(MessageType.JOIN)) {
            Object firstPlayerObj = servletContext.getAttribute(FIRST_PLAYER_NAME);
            if(firstPlayerObj==null) { // if null then user would be the first player
                servletContext.setAttribute(FIRST_PLAYER_NAME,actionMessage.getName());
            }
        }

        return actionMessage;
    }



    /**
     * PostMapping to provide Game type selection
     */
    @PostMapping("/option")
    public String option(@ModelAttribute("game" ) Game game, @Valid GameSelection gameSelection, BindingResult result, Model model, HttpSession httpSession) {
        model.addAttribute("gameSelection", gameSelection);
        if (result.hasErrors()) {
            return "index";
        }
        GameType selectedType = gameSelection.getGameType();
        game.setGameSelection(gameSelection);
        if(selectedType!=null) {
            if(selectedType.equals(GameType.COMPUTER)) {
                String username = (String) httpSession.getAttribute(PLAYERNAME);
                if(username!=null) {
                    httpSession.removeAttribute(PLAYERNAME);
                }
                game.startNew(gameSelection.getDimension());
                return "redirect:/computer";
            }
            if(selectedType.equals(GameType.HUMAN)) {
                model.addAttribute("player", new Player());
                Object dimensionObj = servletContext.getAttribute(OPPONENT_DIMENSION);
                if(dimensionObj!=null) {
                    Integer dimension = (Integer)dimensionObj;
                    game.startNew(dimension);
                } else {
                    servletContext.setAttribute(OPPONENT_DIMENSION,game.getGameSelection().getDimension());
                    game.startNew(gameSelection.getDimension());
                }
                return "register";
            }
        }
        httpSession.removeAttribute(PLAYER_STATE);

        return "index";
    }

    /**
     * PostMapping to continue playing game after registering username, it checked if there is existing game then force player to play with existing rather than create new game
     */
    @PostMapping("/select_game")
    public String selectGame(@Valid @ModelAttribute("player" ) Player player, BindingResult result,  @ModelAttribute("game" ) Game game,  Model model,HttpSession httpSession) {
        if (result.hasErrors()) {
            return "register";
        }

        httpSession.setAttribute(PLAYERNAME, player.getPlayername());

        Object firstPlayerObj = servletContext.getAttribute(FIRST_PLAYER_NAME);
        if(firstPlayerObj!=null) {
            game.setExistingOpponent((String)firstPlayerObj);
            game.setYourTurn(false);
            return "select_game";
        }    else {
            game.setYourTurn(true);
        }

        return "play";
    }

    /**
     * GetMapping for localhost:8080
     */
    @GetMapping("/")
    public String home(  GameSelection gameSelection, Model model, HttpSession httpSession) {
        model.addAttribute("gameSelection",gameSelection);
        cleanUpSession(httpSession);
        return "index";
    }


    /**
     * Method for cleaning up the session
     */
    private void cleanUpSession(HttpSession httpSession) {
        httpSession.removeAttribute(PLAYERNAME);
        httpSession.removeAttribute(PLAYER_STATE);
        servletContext.removeAttribute(FIRST_PLAYER_NAME);
        servletContext.removeAttribute(OPPONENT_DIMENSION);
    }

    /**
     * GetMapping for logout
     */
    @GetMapping("/logout")
    public String logout(HttpSession httpSession) {
        cleanUpSession(httpSession);
        return "redirect:/";
    }
}
