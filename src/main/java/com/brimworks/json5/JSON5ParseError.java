package com.brimworks.json5;

public class JSON5ParseError extends RuntimeException {
    private JSON5Location location;
    public JSON5ParseError(String msg, JSON5Location location) {
        super(location.format("%s", msg));
        this.location = location;
    }
    public JSON5Location getLocation() {
        return location;
    }
}