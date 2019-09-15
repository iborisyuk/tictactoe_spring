package ru.saintunix.tictactoe.server;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.saintunix.tictactoe.exceptions.BusyFieldExceptions;
import ru.saintunix.tictactoe.exceptions.GameAlreadyEndedExceptions;
import ru.saintunix.tictactoe.exceptions.PlayersNotFoundExceptions;
import ru.saintunix.tictactoe.exceptions.TooManyPlayersExceptions;
import ru.saintunix.tictactoe.util.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

@RestController
@RequestMapping("game/")
public class GameController {
    private final AnnotationConfigApplicationContext cxt = new AnnotationConfigApplicationContext(GameConfiguration.class);

    private final HashMap<Integer, Game> games = new HashMap<>();
    private final Random rand = new Random();

    @GetMapping("/")
    public Request status() {
        return Request.ok();
    }

    @GetMapping("new")
    public Request info(@RequestParam(name = "player") int playerId) {
        int gameId = rand.nextInt(9999);

        Game game = cxt.getBean(Game.class);

        try {
            game.addPlayer(playerId);
        } catch (TooManyPlayersExceptions e) {
            return Request.error(e);
        }

        games.put(gameId, game);

        return new Request(true, gameId);
    }

    @GetMapping("list")
    public Request list() {
        ArrayList<Integer> freeGame = new ArrayList<>();

        for (Integer id : games.keySet()) {
            if (!games.get(id).checkWinner() || games.get(id).countPlayer() == 1)
                freeGame.add(id);
        }
        return new Request(true, freeGame.toString());
    }

    @GetMapping("step")
    public Request step(@RequestParam(name = "game") int gameId,
                        @RequestParam(name = "player") int playerId,
                        @RequestParam(name = "x") int x,
                        @RequestParam(name = "y") int y
    ) {
        try {
            games.get(gameId).step(playerId, new int[]{x, y});
        } catch (PlayersNotFoundExceptions | BusyFieldExceptions e) {
            return Request.error(e);
        } catch (GameAlreadyEndedExceptions e) {
            return Request.ok();
        }

        return Request.ok();
    }

    @GetMapping("status")
    public Request status(@RequestParam(name = "game") int gameId, @RequestParam(name = "player") int playerId) {
        if (!games.containsKey(gameId))
            return new Request(false, "Incorrect game ID");

        Game game = games.get(gameId);
        String[][] field = game.getField();

        if (!game.getPlayersId().contains(playerId))
            return new Request(false, "Player id not found this game");

        if (game.checkWinner())
            return new Request(true, game.getWinner(), field, true);


        int nextStepPlayer;
        try {
            nextStepPlayer = game.nextStepPlayer();
        } catch (PlayersNotFoundExceptions e) {
            return Request.error(e);
        }

        return new Request(true, nextStepPlayer, field);
    }

    @GetMapping("player/add")
    public Request playerAdd(@RequestParam(name = "game") int gameId, @RequestParam(name = "player") int playerId) {
        Game game = games.get(gameId);

        try {
            game.addPlayer(playerId);
        } catch (TooManyPlayersExceptions e) {
            return Request.error(e);
        }

        return new Request(true, "ok");
    }

    @GetMapping("player/remove")
    public Request playerRemove(@RequestParam(name = "game") int gameId, @RequestParam(name = "player") int playerId) {
        Game game = games.get(gameId);

        try {
            game.removePlayer(playerId);
        } catch (PlayersNotFoundExceptions e) {
            return Request.error(e);
        }

        return Request.ok();
    }

    @GetMapping("player/list")
    public Request playerList(@RequestParam(name = "game") int gameId) {
        Game game = games.get(gameId);
        return new Request(true, game.getPlayersId().toString());
    }
}

