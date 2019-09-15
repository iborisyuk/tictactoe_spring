package ru.saintunix.tictactoe.server;

import ru.saintunix.tictactoe.exceptions.BusyFieldExceptions;
import ru.saintunix.tictactoe.exceptions.GameAlreadyEndedExceptions;
import ru.saintunix.tictactoe.exceptions.PlayersNotFoundExceptions;
import ru.saintunix.tictactoe.exceptions.TooManyPlayersExceptions;

import java.util.Collection;
import java.util.HashMap;

public class Game {
    private HashMap<String, Integer> players = new HashMap<>();
    private int countStep = 0;
    private String winner = null;
    private String[][] field = {{"", "", ""}, {"", "", ""}, {"", "", ""}};

    public void addPlayer(int playerId) throws TooManyPlayersExceptions {
        if (countPlayer() == 2)
            throw new TooManyPlayersExceptions();

        if (players.containsKey("X"))
            players.put("O", playerId);
        else
            players.put("X", playerId);
    }

    public void removePlayer(int playerId) throws PlayersNotFoundExceptions {
        if (players.size() == 0)
            throw new PlayersNotFoundExceptions();

        for (String player : players.keySet()) {
            if (players.get(player) == playerId) {
                players.remove(player, playerId);
                break;
            }
        }
    }

    public int getPlayerByKey(String key) {
        return players.getOrDefault(key, -1);
    }

    public void step(int playerId, int[] step) throws PlayersNotFoundExceptions, GameAlreadyEndedExceptions, BusyFieldExceptions {
        if (playerId == nextStepPlayer()) {
            if (!field[step[0]][step[1]].equals("")) {
                throw new BusyFieldExceptions();
            }

            if (countStep++ % 2 == 0) {
                field[step[0]][step[1]] = "X";
            } else {
                field[step[0]][step[1]] = "O";
            }
        } else {
            throw new PlayersNotFoundExceptions();
        }

        winner = checkEndOfTheGame();

        if (checkWinner()) {
            throw new GameAlreadyEndedExceptions();
        }
    }

    public boolean checkWinner() {
        return winner != null;
    }

    public int getWinner() {
        return players.get(winner);
    }

    /**
     * Return empty String if exist empty fields.
     *
     * @return win player X|O or empty string.
     */
    private String checkEndOfTheGame() throws GameAlreadyEndedExceptions {
        for (int i = 0; i < field.length; i++) {
            if (!field[i][0].equals("") && field[i][0].equals(field[i][1]) && field[i][1].equals(field[i][2])) {
                return field[i][0];
            }

            if (!field[0][i].equals("") && field[0][i].equals(field[1][i]) && field[1][i].equals(field[2][i])) {
                return field[0][i];
            }
        }

        if (!field[0][0].equals("") && field[0][0].equals(field[1][1]) && field[1][1].equals(field[2][2])) {
            return field[0][0];
        }

        if (!field[0][2].equals("") && field[0][2].equals(field[1][1]) && field[1][1].equals(field[2][1])) {
            return field[0][2];
        }

        int status = 0;
        for (String[] f : field) {
            if (!f[0].equals("") && !f[1].equals("") && !f[2].equals(""))
                status++;
        }

        if (status == 3)
            throw new GameAlreadyEndedExceptions();

        return null;
    }

    public String[][] getField() {
        return field;
    }

    /**
     * Return -1 if nextPlayer not found
     *
     * @return idPlayer or -1
     * @throws PlayersNotFoundExceptions
     */
    public int nextStepPlayer() throws PlayersNotFoundExceptions {
        if (players.size() == 0)
            throw new PlayersNotFoundExceptions();

        if (countStep % 2 == 0)
            return players.getOrDefault("X", -1);
        else
            return players.getOrDefault("O", -1);
    }

    public int countPlayer() {
        return players.size();
    }

    public Collection<Integer> getPlayersId() {
        return players.values();
    }

    @Override
    public String toString() {
        return String.format("[%s][%s][%s]\n[%s][%s][%s]\n[%s][%s][%s]",
                field[0][0], field[0][1], field[0][2],
                field[1][0], field[1][1], field[1][2],
                field[2][0], field[2][1], field[2][2]);
    }
}
