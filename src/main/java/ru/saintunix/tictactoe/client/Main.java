package ru.saintunix.tictactoe.client;

import java.util.Random;

public class Main {
    private static final Random rand = new Random();

    private static final String host = "http://localhost:8080/game/";
    private static final int playerId = rand.nextInt(9999);

    public static void main(String[] args) {
        new PlayToGame(host, playerId).start();
    }
}
