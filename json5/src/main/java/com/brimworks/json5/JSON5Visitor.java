package com.brimworks.json5;

/**
 * Called by JSON5Ragel when tokens are found within the source.
 */
interface JSON5Visitor {
    /**
     * Indicates a null value was found.
     */
    default void visitNull(int line, long offset) {
    }

    /**
     * Indicates a boolean value was found.
     */
    default void visit(boolean val, int line, long offset) {
    }

    /**
     * Indicates a string value was found.
     */
    default void visit(String val, int line, long offset) {
    }

    /**
     * Indicates a numeric value was found, this number is always in "smallest"
     * form, so the number "125" fits within a Byte and therefore will be returned
     * as such.
     */
    default void visit(Number val, int line, long offset) {
    }

    /**
     * Indicates a "{" was found.
     */
    default void startObject(int line, long offset) {
    }

    /**
     * Called when the end of an object "key": VALUE pair has been observed, current
     * location will indicate the location of VALUE.
     */
    default void endObjectPair(int line, long offset) {
    }

    /**
     * Indicates a "}" was found.
     */
    default void endObject(int line, long offset) {
    }

    /**
     * Indicates a "[" was found.
     */
    default void startArray(int line, long offset) {
    }

    /**
     * Called when a new VALUE has been observed which should be appended to an
     * array.
     */
    default void endArrayValue(int line, long offset) {
    }

    /**
     * Indicates a "]" was found.
     */
    default void endArray(int line, long offset) {
    }

    /**
     * Indicates we found a comment.
     */
    default void visitComment(String comment, int line, long offset) {
    }

    /**
     * Indicates we found space.
     */
    default void visitSpace(String space, int line, long offset) {
    }

    /**
     * Indicates a ":" was found.
     */
    default void visitColon(int line, long offset) {
    }

    /**
     * Indicates a "," was found. Generally only useful if trying to preserve input.
     */
    default void visitComma(int line, long offset) {
    }

    /**
     * Indicates the end of stream has been reached.
     */
    default void endOfStream(int line, long offset) {
    }
}