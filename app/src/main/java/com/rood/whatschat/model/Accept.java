package com.rood.whatschat.model;

public class Accept {

    private String request_type;

    public Accept() {
    }

    public Accept(String request_type) {
        this.request_type = request_type;
    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }
}
