package com.brimworks.json5;

import java.util.Deque;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.StandardOpenOption;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

/**
 * Validates input conforms to the rules of the JSON5 grammer.
 */
public class JSON5Parser {
    private static final JSON5Key EMPTY = new JSON5Key("");

    private static enum State {
        INITIAL, STRING_VALUE, VALUE, OBJECT, OBJECT_KEY, ARRAY, APPEND, EOF;
    }

    // Per instance:
    private Deque<JSON5Key> path = new ArrayDeque<>();
    private Deque<JSON5Location> begins = new ArrayDeque<>();
    private String lastString = null;
    private JSON5Visitor visitor = null;

    // Per parse unit:
    private State state;
    private String sourceName;
    private JSON5Location.Read readSource;

    private JSON5Lexer lexer = new JSON5Lexer(new JSON5Visitor() {
        public void visitComment(String comment, int line, long offset) {
            if (null != visitor)
                visitor.visitComment(comment, line, offset);
        }

        public void visitSpace(String space, int line, long offset) {
            if (null != visitor)
                visitor.visitSpace(space, line, offset);
        }

        public void visitNull(int line, long offset) {
            transitionState(State.VALUE, line, offset);
            if (null != visitor)
                visitor.visitNull(line, offset);
        }

        public void visit(boolean val, int line, long offset) {
            transitionState(State.VALUE, line, offset);
            if (null != visitor)
                visitor.visit(val, line, offset);
        }

        public void visit(String val, int line, long offset) {
            transitionState(State.STRING_VALUE, line, offset);
            lastString = val;
            if (null != visitor)
                visitor.visit(val, line, offset);
        }

        public void visit(Number val, int line, long offset) {
            transitionState(State.VALUE, line, offset);
            if (null != visitor)
                visitor.visit(val, line, offset);
        }

        public void endObject(int line, long offset) {
            if (path.isEmpty() || !path.getLast().isObject()) {
                error("Unexpected '}'", line, offset);
            }
            state = State.VALUE;
            path.removeLast();
            begins.removeLast();
            if (null != visitor)
                visitor.endObject(line, offset);
        }

        public void startObject(int line, long offset) {
            transitionState(State.OBJECT, line, offset);
            begins.addLast(getLocation(line, offset));
            path.addLast(EMPTY);
            if (null != visitor)
                visitor.startObject(line, offset);
        }

        public void endArray(int line, long offset) {
            if (path.isEmpty() || !path.getLast().isArray()) {
                error("Unexpected ']'", line, offset);
            }
            state = State.VALUE;
            path.removeLast();
            begins.removeLast();
            if (null != visitor)
                visitor.endArray(line, offset);
        }

        public void startArray(int line, long offset) {
            transitionState(State.ARRAY, line, offset);
            begins.addLast(getLocation(line, offset));
            path.addLast(new JSON5Key(0));
            if (null != visitor)
                visitor.startArray(line, offset);
        }

        public void endObjectKey(int line, long offset) {
            if (path.isEmpty()) {
                error("Unexpected ':'", line, offset);
            }
            transitionState(State.OBJECT_KEY, line, offset);
            path.removeLast();
            path.addLast(new JSON5Key(lastString));
            if (null != visitor)
                visitor.endObjectKey(line, offset);
        }

        public void append(int line, long offset) {
            if (path.isEmpty()) {
                error("Unexpected ','", line, offset);
            }
            transitionState(State.APPEND, line, offset);
            JSON5Key key = path.removeLast();
            if (key.isArray()) {
                path.addLast(new JSON5Key(key.asIndex() + 1));
            } else {
                path.addLast(EMPTY);
            }
            if (null != visitor)
                visitor.append(line, offset);
        }

        public void unexpectedByte(byte ch, int line, long offset) {
            throw new JSON5ParseError(String.format("Unexpected character 0x%02X", ch & 0xFF),
                    getLocation(line, offset));
        }

        public void endOfStream(int line, long offset) {
            transitionState(State.EOF, line, offset);
            if (null != visitor)
                visitor.endOfStream(line, offset);
        }
    });

    public JSON5Parser() {
    }

    public JSON5Parser(JSON5Visitor visitor) {
        this.visitor = visitor;
    }

    public void setVisitor(JSON5Visitor visitor) {
        this.visitor = visitor;
    }

    public JSON5Location getLocation(int line, long offset) {
        return new JSON5Location(line, offset, sourceName, new ArrayList<>(path), readSource);
    }

    public void parse(Path path) throws IOException {
        if (null == path)
            throw new NullPointerException("Expected path to be non-null");
        FileChannel fc = FileChannel.open(path, StandardOpenOption.READ);
        parse(fc, path.toString(), (buff, skip) -> {
            fc.position(skip);
            return fc.read(buff);
        });
    }

    public void parse(ReadableByteChannel in, String sourceName, JSON5Location.Read readSource) throws IOException {
        this.sourceName = sourceName;
        this.readSource = readSource;
        this.state = State.INITIAL;
        this.path.clear();
        this.begins.clear();
        lexer.reset();
        lexer.lex(in);
    }

    private void error(String msg, int line, long offset) {
        throw new JSON5ParseError(msg, getLocation(line, offset));
    }

    private void transitionState(State newState, int line, long offset) {
        switch (state) {
            case INITIAL:
                switch (newState) {
                    case INITIAL:
                        throw new AssertionError("Unexpected transition to INITIAL");
                    case STRING_VALUE:
                        break;
                    case VALUE:
                        break;
                    case OBJECT:
                        break;
                    case OBJECT_KEY:
                        error("Unexpected ':'", line, offset);
                    case ARRAY:
                        break;
                    case APPEND:
                        error("Unexpected ','", line, offset);
                    case EOF:
                        error("Empty content", line, offset);
                        break;
                }
                break;

            case STRING_VALUE:
                switch (newState) {
                    case INITIAL:
                        throw new AssertionError("Unexpected transition to INITIAL");
                    case STRING_VALUE:
                        error("Expected end of stream", line, offset);
                    case VALUE:
                        error("Expected end of stream", line, offset);
                    case OBJECT:
                        error("Expected end of stream", line, offset);
                    case OBJECT_KEY:
                        break;
                    case ARRAY:
                        error("Expected end of stream", line, offset);
                    case APPEND:
                        break;
                    case EOF:
                        break;
                }
                break;

            case VALUE:
                switch (newState) {
                    case INITIAL:
                        throw new AssertionError("Unexpected transition to INITIAL");
                    case STRING_VALUE:
                        error("Expected end of stream", line, offset);
                    case VALUE:
                        error("Expected end of stream", line, offset);
                    case OBJECT:
                        error("Expected end of stream", line, offset);
                    case OBJECT_KEY:
                        error("Unexpected ':'", line, offset);
                    case ARRAY:
                        error("Expected end of stream", line, offset);
                    case APPEND:
                        break;
                    case EOF:
                        break;
                }
                break;
            case OBJECT:
                switch (newState) {
                    case INITIAL:
                        throw new AssertionError("Unexpected transition to INITIAL");
                    case STRING_VALUE:
                        break;
                    case VALUE:
                        error("Object keys must be a string", line, offset);
                    case OBJECT:
                        error("Object keys must be a string", line, offset);
                    case OBJECT_KEY:
                        error("Unexpected ':'", line, offset);
                    case ARRAY:
                        error("Object keys must be a string", line, offset);
                    case APPEND:
                        error("Unexpected ','", line, offset);
                    case EOF:
                        error("Missing '}'", line, offset);
                        break;
                }
                break;
            case OBJECT_KEY:
                switch (newState) {
                    case INITIAL:
                        throw new AssertionError("Unexpected transition to INITIAL");
                    case STRING_VALUE:
                        break;
                    case VALUE:
                        break;
                    case OBJECT:
                        break;
                    case OBJECT_KEY:
                        error("Unexpected ':'", line, offset);
                    case ARRAY:
                        break;
                    case APPEND:
                        error("Unexpected ','", line, offset);
                    case EOF:
                        error("Missing '}'", line, offset);
                }
                break;

            case ARRAY:
                switch (newState) {
                    case INITIAL:
                        throw new AssertionError("Unexpected transition to INITIAL");
                    case STRING_VALUE:
                        break;
                    case VALUE:
                        break;
                    case OBJECT:
                        break;
                    case OBJECT_KEY:
                        error("Unexpected ':'", line, offset);
                    case ARRAY:
                        break;
                    case APPEND:
                        error("Unexpected ','", line, offset);
                    case EOF:
                        error("Missing ']'", line, offset);
                }
                break;

            case APPEND:
                switch (newState) {
                    case INITIAL:
                        throw new AssertionError("Unexpected transition to INITIAL");
                    case STRING_VALUE:
                        break;
                    case VALUE:
                        break;
                    case OBJECT:
                        break;
                    case OBJECT_KEY:
                        error("Unexpected ':'", line, offset);
                    case ARRAY:
                        break;
                    case APPEND:
                        error("Unexpected ','", line, offset);
                    case EOF:
                        error("Missing ']'", line, offset);
                }
                break;

            case EOF:
                throw new AssertionError("Unexpected transition from EOF");
        }
        state = newState;
    }
}