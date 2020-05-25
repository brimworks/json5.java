package com.brimworks.json5;

import java.util.Deque;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.StandardOpenOption;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.math.BigDecimal;
import java.math.BigInteger;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Validates input conforms to the rules of the JSON5 grammer and emits this
 * structure to a visitor. The visitor can build a tree, transform input, or do
 * anything with the JSON5 input tokens.
 */
public class JSON5Parser {
    private static final JSON5Key EMPTY = new JSON5Key("");

    private static class LineOffset {
        private int line;
        private long offset;

        public LineOffset(int line, long offset) {
            this.line = line;
            this.offset = offset;
        }

        public int getLine() {
            return this.line;
        }

        public long getOffset() {
            return this.offset;
        }
    }

    private static enum State {
        INITIAL, STRING_VALUE, VALUE, OBJECT, OBJECT_KEY, ARRAY, APPEND, EOF;
    }

    // Per instance:
    private Deque<JSON5Key> path = new ArrayDeque<>();
    private Deque<LineOffset> begins = new ArrayDeque<>();
    private String lastString = null;
    private JSON5Visitor visitor = null;

    // Per parse unit:
    private State state;
    private String sourceName;
    private JSON5Location.Read readSource;

    private void visitValue(int line, long offset) {
        if (path.isEmpty()) {
            return;
        } else if (path.getLast().isArray()) {
            visitor.endArrayValue(line, offset);
        } else {
            visitor.endObjectPair(path.getLast().asKey(), line, offset);
        }
    }

    private JSON5Lexer lexer = new JSON5Lexer(new JSON5Lexer.Visitor() {
        @Override
        public void visitComment(String comment, int line, long offset) {
            if (null != visitor)
                visitor.visitComment(comment, line, offset);
        }

        @Override
        public void visitSpace(String space, int line, long offset) {
            if (null != visitor)
                visitor.visitSpace(space, line, offset);
        }

        @Override
        public void visitNull(int line, long offset) {
            transitionState(State.VALUE, line, offset);
            if (null != visitor) {
                if (!path.isEmpty() && path.getLast().isArray()) {
                    visitor.visitIndex(path.getLast().asIndex(), line, offset);
                }
                visitor.visitNull(line, offset);
                visitValue(line, offset);
            }
        }

        @Override
        public void visit(boolean val, int line, long offset) {
            transitionState(State.VALUE, line, offset);
            if (null != visitor) {
                if (!path.isEmpty() && path.getLast().isArray()) {
                    visitor.visitIndex(path.getLast().asIndex(), line, offset);
                }
                visitor.visit(val, line, offset);
                visitValue(line, offset);
            }
        }

        @Override
        public void visit(String val, int line, long offset) {
            boolean isObjectKey = state == State.OBJECT || (state == State.APPEND && path.getLast().isObject());
            transitionState(State.STRING_VALUE, line, offset);
            lastString = val;
            if (null != visitor) {
                if (!isObjectKey) {
                    if (!path.isEmpty() && path.getLast().isArray()) {
                        visitor.visitIndex(path.getLast().asIndex(), line, offset);
                    }
                    visitor.visit(val, line, offset);
                    visitValue(line, offset);
                } else {
                    visitor.visitKey(val, line, offset);
                }
            }
        }

        @Override
        public void visitNumber(BigInteger val, int line, long offset) {
            transitionState(State.VALUE, line, offset);
            if (null != visitor) {
                if (!path.isEmpty() && path.getLast().isArray()) {
                    visitor.visitIndex(path.getLast().asIndex(), line, offset);
                }
                visitor.visitNumber(val, line, offset);
                visitValue(line, offset);
            }
        }

        @Override
        public void visitNumber(BigDecimal val, int line, long offset) {
            transitionState(State.VALUE, line, offset);
            if (null != visitor) {
                if (!path.isEmpty() && path.getLast().isArray()) {
                    visitor.visitIndex(path.getLast().asIndex(), line, offset);
                }
                visitor.visitNumber(val, line, offset);
                visitValue(line, offset);
            }
        }

        @Override
        public void visitNumber(long val, int line, long offset) {
            transitionState(State.VALUE, line, offset);
            if (null != visitor) {
                if (!path.isEmpty() && path.getLast().isArray()) {
                    visitor.visitIndex(path.getLast().asIndex(), line, offset);
                }
                visitor.visitNumber(val, line, offset);
                visitValue(line, offset);
            }
        }

        @Override
        public void visitNumber(double val, int line, long offset) {
            transitionState(State.VALUE, line, offset);
            if (null != visitor) {
                if (!path.isEmpty() && path.getLast().isArray()) {
                    visitor.visitIndex(path.getLast().asIndex(), line, offset);
                }
                visitor.visitNumber(val, line, offset);
                visitValue(line, offset);
            }
        }

        @Override
        public void endObject(int line, long offset) {
            if (path.isEmpty()) {
                error("Unexpected '}'", line, offset);
            } else if (path.getLast().isArray()) {
                error("Expected ']' to match with '[' on line " + begins.getLast().getLine(), line, offset);
            }
            state = State.VALUE;
            path.removeLast();
            LineOffset beginning = begins.removeLast();
            if (null != visitor) {
                visitor.endObject(line, offset);
                visitValue(beginning.getLine(), beginning.getOffset());
            }
        }

        @Override
        public void startObject(int line, long offset) {
            transitionState(State.OBJECT, line, offset);
            JSON5Key key = path.peekLast();
            begins.addLast(new LineOffset(line, offset));
            path.addLast(EMPTY);
            if (null != visitor) {
                if (null != key && key.isArray()) {
                    visitor.visitIndex(key.asIndex(), line, offset);
                }
                visitor.startObject(line, offset);
            }
        }

        @Override
        public void endArray(int line, long offset) {
            if (path.isEmpty()) {
                error("Unexpected ']'", line, offset);
            } else if (!path.getLast().isArray()) {
                error("Expected '}' to match with '{' on line " + begins.getLast().getLine(), line, offset);
            }
            state = State.VALUE;
            path.removeLast();
            LineOffset beginning = begins.removeLast();
            if (null != visitor) {
                visitor.endArray(line, offset);
                visitValue(beginning.getLine(), beginning.getOffset());
            }
        }

        @Override
        public void startArray(int line, long offset) {
            transitionState(State.ARRAY, line, offset);
            JSON5Key key = path.peekLast();
            begins.addLast(new LineOffset(line, offset));
            path.addLast(new JSON5Key(0));
            if (null != visitor) {
                if (null != key && key.isArray()) {
                    visitor.visitIndex(key.asIndex(), line, offset);
                }
                visitor.startArray(line, offset);
            }
        }

        @Override
        public void visitColon(int line, long offset) {
            if (path.isEmpty()) {
                error("Unexpected ':'", line, offset);
            }
            transitionState(State.OBJECT_KEY, line, offset);
            path.removeLast();
            path.addLast(new JSON5Key(lastString));
            if (null != visitor)
                visitor.visitColon(line, offset);
        }

        @Override
        public void visitComma(int line, long offset) {
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
                visitor.visitComma(line, offset);
        }

        @Override
        public void unexpectedByte(byte ch, int line, long offset) {
            throw new JSON5ParseError(String.format("Unexpected character 0x%02X", ch & 0xFF),
                    getLocation(line, offset));
        }

        @Override
        public void exponentOverflow(int line, long offset) {
            throw new JSON5ParseError(String.format("Exponent exceeds %d", Integer.MAX_VALUE),
                    getLocation(line, offset));
        }

        @Override
        public void endOfStream(int line, long offset) {
            transitionState(State.EOF, line, offset);
            if (!path.isEmpty()) {
                JSON5Key key = path.getLast();
                if (key.isArray()) {
                    error("Expected ']' before end of file to match with '[' on line " + begins.getLast().getLine(),
                            line, offset);
                } else {
                    error("Expected '}' before end of file to match with '{' on line " + begins.getLast().getLine(),
                            line, offset);
                }
            }
            if (null != visitor)
                visitor.endOfStream(line, offset);
        }
    });

    /**
     * Simply create a parser.
     */
    public JSON5Parser() {
    }

    /**
     * Create a parser and set the visitor.
     * 
     * @param visitor set the visitor to this
     */
    public JSON5Parser(JSON5Visitor visitor) {
        this.visitor = visitor;
    }

    /**
     * Switch to a different visitor implementation.
     * 
     * @param visitor set the visitor
     * @return this
     */
    public JSON5Parser setVisitor(JSON5Visitor visitor) {
        this.visitor = visitor;
        return this;
    }

    /**
     * Obtain the current location within the input, useful when implementing a
     * visitor.
     * 
     * @param line   the current line (passed into the visitor methods)
     * @param offset the current byte offset from the beginning of the UTF-8 encoded
     *               file (passed into the visitor methods)
     * @return a new {@code JSON5Location} which will display source-text messages.
     */
    public JSON5Location getLocation(int line, long offset) {
        return new JSON5Location(line, offset, sourceName, new ArrayList<>(path), readSource);
    }

    /**
     * Parse a JSON5 document at the specified path.
     * 
     * @param path location of JSON5 document.
     * @throws JSON5ParseError if source-text does not conform to JSON5.
     * @throws IOException     if there was an error reading the file at Path.
     */
    public void parse(Path path) throws IOException, JSON5ParseError {
        if (null == path)
            throw new NullPointerException("Unexpected null path to parse");
        FileChannel fc = FileChannel.open(path, StandardOpenOption.READ);
        parse(fc, path.toString(), (buff, skip) -> {
            fc.position(skip);
            return fc.read(buff);
        });
    }

    /**
     * Parse a JSON5 document from a string.
     * 
     * @param str        string to parse
     * @param sourceName name of source location used in errors
     * @throws JSON5ParseError if a parse error is encountered.
     */
    public void parse(String str, String sourceName) throws JSON5ParseError {
        parse(ByteBuffer.wrap(str.getBytes(UTF_8)), sourceName);
    }

    /**
     * Parse a JSON5 document from a byte buffer, if you have a {@code byte[]},
     * simply use {@link ByteBuffer#wrap(byte[])} to wrap it into a
     * {@code ByteBuffer}.
     * 
     * @param utf8       utf8 encoded byte buffer
     * @param sourceName name of source location used in errors
     * @throws JSON5ParseError if a parse error is encountered.
     */
    public void parse(ByteBuffer utf8, String sourceName) throws JSON5ParseError {
        this.sourceName = sourceName;
        this.readSource = (into, skip) -> {
            ByteBuffer slice = utf8.slice();
            slice.position(slice.position() + (int) skip);
            int len = Math.min(slice.remaining(), into.remaining());
            into.put(slice);
            return len;
        };
        this.state = State.INITIAL;
        this.path.clear();
        this.begins.clear();
        lexer.reset();
        lexer.lex(utf8, true);
    }

    /**
     * Parse a JSON5 document from a {@code ReadableByteChannel}. Note that you can
     * use {@link java.nio.channels.Channels#newChannel(java.io.InputStream)} to
     * convert an input stream into a channel.
     * 
     * @param in         required ReadableByteChannel
     * @param sourceName optional name of source text location (for better errors).
     * @param readSource optional function for obtaining source-text error message.
     * @throws IOException     if {@code in}
     * @throws JSON5ParseError if source-text does not conform to JSON5.
     */
    public void parse(ReadableByteChannel in, String sourceName, JSON5Location.Read readSource)
            throws IOException, JSON5ParseError {
        if (null == in)
            throw new NullPointerException("Expected ReadableByteChannel to be non-null");
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