package com.brimworks.json5;

import java.math.BigInteger;
import java.math.BigDecimal;

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
     * Indicates a floating point value was found in the input source. Note that
     * input source of 0.0 is considered to be a double and 0 is a long. If a
     * floating point value does not "fit" into a double, the conversion uses
     * BigDecimal and {@link #visit(Number,int,long)} will be called instead.
     * 
     * In order for a floating point value to "fit" it must conform to these two
     * conditions:
     * <ul>
     * <li>The significand is <= 2 to the power of 52. All 15 digit numbers will
     * fit, some 16 digit numbers too.
     * <li>The base 2 exponent is <= 2 to the power of 10. Basically any number
     * greater than 1e308 can be represented as a double.
     * 
     */
    default void visit(Number val, int line, long offset) {
    }

    /**
     * Found an integer value (no decimal, no exponent) which does not fit within a
     * long. Defaults to calling {@link #visit(Number,int,long)
     * 
     * @param val    value found.
     * @param line   line it was found on.
     * @param offset offset within source-text.
     */
    default void visitNumber(BigInteger val, int line, long offset) {
        visit(val, line, offset);
    }

    /**
     * Found a floating point value (decimal or exponent was specified). Defaults to
     * calling {@link #visit(Number,int,long)
     * 
     * @param val    value found.
     * @param line   line it was found on.
     * @param offset offset within source-text.
     */
    default void visitNumber(BigDecimal val, int line, long offset) {
        visit(val, line, offset);
    }

    /**
     * Found an integer value (no decimal, no exponent) which fits within a long.
     * Defaults to calling {@link #visit(Number,int,long)
     * 
     * @param val    value found.
     * @param line   line it was found on.
     * @param offset offset within source-text.
     */
    default void visitNumber(long val, int line, long offset) {
        visit(Long.valueOf(val), line, offset);
    }

    /**
     * Found a floating point value (decimal or exponent was specified) which is non
     * fractional and is in the range 2^53 -1 to -2^53 - 1. Defaults to calling
     * {@link #visit(Number,int,long)
     * 
     * @param val    value found.
     * @param line   line it was found on.
     * @param offset offset within source-text.
     */
    default void visitNumber(double val, int line, long offset) {
        visit(Double.valueOf(val), line, offset);
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