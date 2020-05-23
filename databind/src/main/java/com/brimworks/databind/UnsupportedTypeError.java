package com.brimworks.databind;

import java.lang.reflect.Type;

/**
 * Thrown to indicate a specified type was not supported.
 */
public class UnsupportedTypeError extends RuntimeException {
    public UnsupportedTypeError(String msg) {
        super(msg);
    }
    public UnsupportedTypeError(String msg, Throwable cause) {
        super(msg, cause);
    }
}