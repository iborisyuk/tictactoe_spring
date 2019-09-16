package ru.saintunix.tictactoe.client;

import com.google.gson.Gson;
import ru.saintunix.tictactoe.util.Request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.NoSuchElementException;

public class PlayToGame {
    private final String host;
    private final String id;
    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private final Gson gosn = new Gson();
    private final HttpClient http = HttpClient.newBuilder().build();

    private String gameId = null;

    public PlayToGame(String host, int playerId) {
        this.host = host;
        this.id = Integer.toString(playerId);
    }

    public void start() {
        while (true) {
            menu();
            int menuId = readIntStdin();

            switch (menuId) {
                case 1: {
                    newGame();
                    break;
                }
                case 2: {
                    listGame();
                    break;
                }
                case 3: {
                    System.exit(0);
                }
            }
        }
    }

    private void newGame() {
        try {
            gameId = sendRequest("new?player=" + id).getMsg();
        } catch (Throwable e) {
            System.err.println("Failed create new game!");
            return;
        }
        play();

    }

    private void listGame() {
        Request resp;
        try {
            resp = sendRequest("list");
        } catch (Throwable e) {
            System.err.println("Failed list new game!");
            return;
        }
        String[] games = gosn.fromJson(resp.getMsg(), String[].class);

        if (games.length == 0) {
            System.out.println("Not active game!");
            return;
        }

        int count = 0;
        for (String game : games) {
            System.out.printf("%d) %s\n", count++, game);
        }

        int connectToGame;
        while (true) {
            connectToGame = readIntStdin();

            if (connectToGame > count) {
                System.err.println("Incorrect number game!");
                continue;
            }

            break;
        }

        try {
            sendRequest(String.format("player/add?player=%s&game=%s", id, games[connectToGame]));
        } catch (Throwable e) {
            System.err.println("Failed add player to game!");
            return;
        }
        gameId = games[connectToGame];

        play();
    }

    private void play() {
        boolean status = true; // Displayed a msg about the opponent progress

        while (true) {
            Request resp;
            try {
                resp = sendRequest("status?player=" + id + "&game=" + gameId);
            } catch (Throwable e) {
                System.err.println("Failed getting game status!");
                return;
            }

            String nextPlayer = resp.getMsg();
            String[][] field = resp.getField();

            if (resp.isEndGame()) {
                printField(field);
                if (nextPlayer.equals(id))
                    System.out.println("You win!");
                else if (!nextPlayer.equals(""))
                    System.out.println("You louse!");
                else
                    System.out.println("Friendship won!");
                break;
            }

            if (nextPlayer.equals(id)) {
                printField(field);
                System.out.println("You turn (Example '1 1')");
                String stringStep = readLineStdin();
                String[] step = stringStep.split(" ");

                try {
                    sendRequest(String.format("step?player=%s&game=%s&x=%s&y=%s", id, gameId, step[0], step[1]));
                } catch (Throwable e) {
                    System.err.println("Incorrect step! Try again!");
                    continue;
                }
                status = true;
            } else {
                if (status) {
                    System.out.println("Expect, now go your opponent.");
                    status = false;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void printField(String[][] field) {
        System.out.printf("[%s][%s][%s]\n[%s][%s][%s]\n[%s][%s][%s]\n",
                field[0][0], field[0][1], field[0][2],
                field[1][0], field[1][1], field[1][2],
                field[2][0], field[2][1], field[2][2]
        );
    }

    private Request sendRequest(String req) throws Throwable {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(host + req))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> resp = null;

        try {
            resp = http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }

        if (resp.statusCode() != 200) {
            System.err.println(resp.body());
            System.exit(1);
        }

        Request response = gosn.fromJson(resp.body(), Request.class);
        if (!response.isStatus()) {
            String className = response.getMsg();
            try {
                Class<?> err = Class.forName(className);
                if (className.contains("Exceptions")) {
                    throw (Throwable) err.getConstructor().newInstance();
                }
            } catch (ClassNotFoundException e) {
                System.err.println("Failed create error: " + className);
                System.exit(1);
            } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
                System.exit(1);
            }

            System.err.println(response.getMsg());
            System.exit(1);
        }

        return gosn.fromJson(resp.body(), Request.class);
    }

    private void menu() {
        System.out.println("1) New game\n2) List game\n3) Exit game\n");
    }

    private int readIntStdin() {
        int data;
        while (true) {
            System.out.print("~$ ");
            try {
                String dataString = reader.readLine();
                data = Integer.parseInt(dataString);
            } catch (NoSuchElementException | IOException | NumberFormatException e) {
                System.out.println("Invalid data. Re-enter.");
                continue;
            }

            break;
        }

        return data;
    }

    private String readLineStdin() {
        String data;

        while (true) {
            System.out.print("~$ ");

            try {
                data = reader.readLine();
            } catch (NoSuchElementException | IllegalStateException | IOException e) {
                System.out.println("Invalid data. Re-enter.");
                continue;
            }

            break;
        }

        return data;
    }
}
