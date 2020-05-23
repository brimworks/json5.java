package com.brimworks.databind;

/**
 * Thrown to indicate a specified key was not valid.
 */
public class UnknownKeyError extends UnsupportedTypeError {
    public UnknownKeyError(String msg) {
        super(msg);
    }
    public UnknownKeyError(String msg, Throwable cause) {
        super(msg, cause);
    }
}