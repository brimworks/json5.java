package com.brimworks.json5;

public class JSON5ParseError extends RuntimeException {
    private JSON5Location location;
    private String msg;
    public JSON5ParseError(String msg, JSON5Location location) {
        this.location = location;
        this.msg = msg;
    }
    @Override
    public String getMessage() {
        // Defer the cost of fetching the contextLine of the location:
        return location.format("%s", msg);
    }
    public JSON5Location getLocation() {
        return location;
    }
}