package com.rood.whatschat.model;

public class Message {

    private String message;
    private String time;
    private String from;

    public Message() {
    }

    public Message(String message, String time, String from) {
        this.message = message;
        this.time = time;
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
