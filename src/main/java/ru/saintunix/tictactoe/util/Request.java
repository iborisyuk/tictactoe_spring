package ru.saintunix.tictactoe.util;

public class Request {
    private boolean status;
    private String msg;
    private boolean endGame = false;
    private String[][] field;

    public static Request ok() {
        return new Request(true, "ok");
    }

    public static Request error(Exception e) {
        return new Request(false, e.getClass().getName());
    }

    public Request(boolean status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public Request(boolean status, int msg) {
        this.status = status;
        this.msg = msg + "";
    }

    public Request(boolean status, int msg, String[][] field) {
        this.status = status;
        this.msg = msg + "";
        this.field = field;
    }

    public Request(boolean status, int msg, String[][] field, boolean endGame) {
        this.status = status;
        this.msg = msg + "";
        this.field = field;
        this.endGame = endGame;
    }

    public boolean isStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public String[][] getField() {
        return field;
    }

    public boolean isEndGame() {
        return endGame;
    }
}
