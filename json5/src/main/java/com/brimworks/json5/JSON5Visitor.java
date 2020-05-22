package com.brimworks.json5;

import java.math.BigInteger;
import java.math.BigDecimal;

/**
 * Called by JSON5Ragel when tokens are found within the source.
 */
public interface JSON5Visitor {
    /**
     * Indicates a null value was found.
     * 
     * @param line   source-input line of token
     * @param offset source-input byte offset from beginning of stream
     */
    default void visitNull(int line, long offset) {
    }

    /**
     * Indicates a boolean value was found.
     * 
     * @param val    value found
     * @param line   source-input line of token
     * @param offset source-input byte offset from beginning of stream
     */
    default void visit(boolean val, int line, long offset) {
    }

    /**
     * Indicates a string value was found.
     * 
     * @param val    value found
     * @param line   source-input line of token
     * @param offset source-input byte offset from beginning of stream
     */
    default void visit(String val, int line, long offset) {
    }

    /**
     * Narrowing interface, not actually called directly by the parser, but provides
     * convenience if you do not want to override the individual
     * {@code visitNumber()} methods.
     * 
     * @param val    value found.
     * @param line   source-input line of token
     * @param offset source-input byte offset from beginning of stream
     */
    default void visit(Number val, int line, long offset) {
    }

    /**
     * Found an integer value (no decimal, no exponent) which does not fit within a
     * long. Defaults to calling {@link #visit(Number,int,long)}
     * 
     * @param val    value found.
     * @param line   source-input line of token
     * @param offset source-input byte offset from beginning of stream
     */
    default void visitNumber(BigInteger val, int line, long offset) {
        visit(val, line, offset);
    }

    /**
     * Found a floating point value (decimal or exponent was specified) which could
     * not fit in a double without potentially loosing precision. Defaults to
     * calling {@link #visit(Number,int,long)}
     * 
     * @param val    value found.
     * @param line   source-input line of token
     * @param offset source-input byte offset from beginning of stream
     */
    default void visitNumber(BigDecimal val, int line, long offset) {
        visit(val, line, offset);
    }

    /**
     * Found an integer value (no decimal, no exponent) which fits within a long.
     * Defaults to calling {@link #visit(Number,int,long)}
     * 
     * @param val    value found.
     * @param line   source-input line of token
     * @param offset source-input byte offset from beginning of stream
     */
    default void visitNumber(long val, int line, long offset) {
        visit(Long.valueOf(val), line, offset);
    }

    /**
     * Found a floating point value (decimal or exponent was specified) which is non
     * fractional and is in the range 2^53 -1 to -2^53 - 1. Defaults to calling
     * {@link #visit(Number,int,long)}
     * 
     * @param val    value found.
     * @param line   source-input line of token
     * @param offset source-input byte offset from beginning of stream
     */
    default void visitNumber(double val, int line, long offset) {
        visit(Double.valueOf(val), line, offset);
    }

    /**
     * Indicates a "{" was found.
     * 
     * @param line   source-input line of token
     * @param offset source-input byte offset from beginning of stream
     */
    default void startObject(int line, long offset) {
    }

    /**
     * Called when an object "key" has been observed, current location will indicate
     * the location of "key". Note that `visitString()` is NOT called.
     * 
     * @param key    the key of the object
     * @param line   source-input line of token
     * @param offset source-input byte offset from beginning of stream
     */
    default void visitKey(String key, int line, long offset) {
    }

    /**
     * Called when an object "key": VALUE has been observed, current location will indicate
     * the location of VALUE.
     * 
     * @param key    the key of the object
     * @param line   source-input line of token
     * @param offset source-input byte offset from beginning of stream
     */
    default void endObjectPair(String key, int line, long offset) {
    }

    /**
     * Indicates a "}" was found.
     * 
     * @param line   source-input line of token
     * @param offset source-input byte offset from beginning of stream
     */
    default void endObject(int line, long offset) {
    }

    /**
     * Indicates a "[" was found.
     * 
     * @param line   source-input line of token
     * @param offset source-input byte offset from beginning of stream
     */
    default void startArray(int line, long offset) {
    }

    /**
     * Called when a new VALUE has been observed which should be appended to an
     * array.
     * 
     * @param line   source-input line of token
     * @param offset source-input byte offset from beginning of stream
     */
    default void endArrayValue(int line, long offset) {
    }

    /**
     * Indicates a "]" was found.
     * 
     * @param line   source-input line of token
     * @param offset source-input byte offset from beginning of stream
     */
    default void endArray(int line, long offset) {
    }

    /**
     * Indicates we found a comment.
     * 
     * @param comment value found
     * @param line    source-input line of token
     * @param offset  source-input byte offset from beginning of stream
     */
    default void visitComment(String comment, int line, long offset) {
    }

    /**
     * Indicates we found space.
     * 
     * @param space  value found
     * @param line   source-input line of token
     * @param offset source-input byte offset from beginning of stream
     */
    default void visitSpace(String space, int line, long offset) {
    }

    /**
     * Indicates a ":" was found.
     * 
     * @param line   source-input line of token
     * @param offset source-input byte offset from beginning of stream
     */
    default void visitColon(int line, long offset) {
    }

    /**
     * Indicates a "," was found. Generally only useful if trying to preserve input.
     * 
     * @param line   source-input line of token
     * @param offset source-input byte offset from beginning of stream
     */
    default void visitComma(int line, long offset) {
    }

    /**
     * Indicates the end of stream has been reached.
     * 
     * @param line   source-input line of token
     * @param offset source-input byte offset from beginning of stream
     */
    default void endOfStream(int line, long offset) {
    }
}