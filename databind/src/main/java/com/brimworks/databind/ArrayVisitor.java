package com.brimworks.databind;

import java.util.function.Consumer;

/**
 * The return type of {@link TypeVisitor#visitArray()}.
 */
public interface ArrayVisitor {
    /**
     * Visit a new array element.
     * 
     * @param consumer called to recursively visit this array element.
     * @return this
     */
    ArrayVisitor add(Consumer<TypeVisitor> consumer);
}