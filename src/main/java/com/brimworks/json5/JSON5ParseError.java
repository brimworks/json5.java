package com.brimworks.json5;

/**
 * Thrown by {@link JSON5Parser} to indicate character precise errors within the
 * source-text.
 */
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

    /**
     * Return the {@link JSON5Location} within the source-text where the error
     * occurred.
     * 
     * @return the location
     */
    public JSON5Location getLocation() {
        return location;
    }
}