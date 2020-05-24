package com.brimworks.databind;

// Not sure why this is missing from java.util.function.*;

import java.util.Objects;

@FunctionalInterface
public interface ShortConsumer {
    void accept(short value);
    default ShortConsumer andThen(ShortConsumer after) {
        Objects.requireNonNull(after);
        return (short t) -> { accept(t); after.accept(t); };
    }
}