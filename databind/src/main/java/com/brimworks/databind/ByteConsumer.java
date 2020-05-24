package com.brimworks.databind;

// Not sure why this is missing from java.util.function.*;

import java.util.Objects;

@FunctionalInterface
public interface ByteConsumer {
    void accept(byte value);
    default ByteConsumer andThen(ByteConsumer after) {
        Objects.requireNonNull(after);
        return (byte t) -> { accept(t); after.accept(t); };
    }
}