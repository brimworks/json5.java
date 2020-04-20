package com.brimworks.json5;

import java.util.Deque;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

public class JSON5Parser {
    private JSON5Lexer lexer = new JSON5Lexer(new JSON5Visitor() {
        // TODO: Implement...
    });
    private Deque<JSON5Key> path = new ArrayDeque<>();
    private Deque<JSON5Location> begins = new ArrayDeque<>();
    private Deque<Object> stack = new ArrayDeque<>();
    public void parse(ReadableByteChannel in, String sourceName, JSON5Location.Read readSource) throws IOException {
        path.clear();
        begins.clear();
        stack.clear();
        lexer.reset();
        lexer.lex(in);
        if ( stack.size() < 1 ) {
            throw new JSON5ParseError("Empty input", lexer.getLocation(sourceName, new ArrayList<>(path), readSource));
        } else if ( stack.size() > 1 ) {
            String terminator = path.getLast().isArray() ? "]" : "}";
            throw new JSON5ParseError("Missing "+terminator+" at end of input", begins.getLast());
        }
    }
}