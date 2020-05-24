package com.brimworks.databind;

// Not sure why this is missing from java.util.function.*;

import java.util.Objects;

@FunctionalInterface
public interface FloatConsumer {
    void accept(float value);
    default FloatConsumer andThen(FloatConsumer after) {
        Objects.requireNonNull(after);
        return (float t) -> { accept(t); after.accept(t); };
    }
}