package com.brimworks.databind;

// Not sure why this is missing from java.util.function.*;

import java.util.Objects;

@FunctionalInterface
public interface CharConsumer {
    void accept(char value);
    default CharConsumer andThen(CharConsumer after) {
        Objects.requireNonNull(after);
        return (char t) -> { accept(t); after.accept(t); };
    }
}