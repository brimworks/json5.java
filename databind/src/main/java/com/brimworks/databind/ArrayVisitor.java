package com.brimworks.databind;

/**
 * The return type of {@link TypeVisitor#visitArray()}.
 */
public interface ArrayVisitor {
    /**
     * Visit a new array element.
     * 
     * @return a TypeVisitor to use on for the next array element encountered.
     */
    TypeVisitor add();
}