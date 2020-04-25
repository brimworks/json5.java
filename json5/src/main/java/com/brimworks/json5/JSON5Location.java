package com.brimworks.json5;

import java.util.List;
import java.util.Collections;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.nio.ByteBuffer;
import java.util.logging.Logger;
import java.util.logging.Level;
import static java.nio.charset.StandardCharsets.UTF_8;

public class JSON5Location {
    private static final int MAX_LINE_LEN = 5 * 1024;
    private static Logger log = Logger.getLogger("" + JSON5Location.class);

    @FunctionalInterface
    public interface Read {
        /**
         * Used to read the context line.
         * 
         * @param into is the array to populate.
         * @param skip the number of bytes of input to skip before reading.
         * @return the number of bytes placed into the array.
         * @throws IOException if the underlying source could not be read.
         */
        public int read(ByteBuffer into, long skip) throws IOException;
    }

    private long byteOffset;
    private int lineNumber;
    private String sourceName;
    private List<JSON5Key> path;
    private Read readSource;
    private String contextLine;
    private int contextLineOffset = -1;
    private StackTraceElement[] constructedAt;

    public JSON5Location(int lineNumber, long byteOffset, String sourceName, List<JSON5Key> path, Read readSource) {
        if (null == path)
            throw new NullPointerException("Expected path to be non null");
        if (byteOffset < 0)
            throw new IndexOutOfBoundsException("byteOffset must be greater than zero");
        this.byteOffset = byteOffset;
        this.lineNumber = lineNumber;
        this.sourceName = sourceName;
        this.path = path;
        this.readSource = readSource;
        this.constructedAt = Thread.currentThread().getStackTrace();
    }

    public long getByteOffset() {
        return byteOffset;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getSourceName() {
        return sourceName;
    }

    public List<JSON5Key> getPath() {
        return Collections.unmodifiableList(path);
    }

    /**
     * Obtain a context line of the source if this JSON5Location was constructed
     * with a ReadSource.
     * 
     * @return the context line
     */
    public synchronized String getContextLine() {
        if (null != contextLine || null == readSource)
            return contextLine;
        byte[] contextBytes = new byte[MAX_LINE_LEN];
        int contextBytesLen = 0;
        int contextOffset = MAX_LINE_LEN / 2;
        try {
            if (byteOffset < contextOffset) {
                contextOffset = (int) byteOffset;
                contextBytesLen = readSource.read(ByteBuffer.wrap(contextBytes), 0);
            } else {
                contextBytesLen = readSource.read(ByteBuffer.wrap(contextBytes), byteOffset - contextOffset);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        if (contextBytesLen < contextOffset) {
            String msg = "JSON5Location was passed in a Source that returned insufficient bytes, expected at least="
                    + contextOffset + ", but got length=" + contextBytesLen;
            Throwable ex = new Throwable(msg, null, false, true) {
            };
            ex.setStackTrace(constructedAt);
            log.log(Level.WARNING, msg, ex);
            readSource = null;
            return null;
        }
        int lineBegins = contextOffset;
        while (lineBegins > 0 && !isNewline(contextBytes, lineBegins - 1, contextBytesLen))
            lineBegins--;
        int lineEnds = contextOffset;
        while (lineEnds < contextBytesLen && !isNewline(contextBytes, lineEnds, contextBytesLen))
            lineEnds++;
        contextLine = new String(contextBytes, lineBegins, lineEnds - lineBegins, UTF_8);
        contextLineOffset = contextOffset - lineBegins;
        // Now adjust contextLineOffset for UTF-8 char sizes vs UTF-16:
        for (int idx = 0; idx < contextLineOffset; idx++) {
            int ch = contextLine.codePointAt(idx);
            if (ch < 0x80) {
                continue;
            } else if (ch < 0x800) {
                contextLineOffset--;
            } else if (ch < 0x10000) {
                contextLineOffset -= 2;
            } else {
                // must have represented a surrogate pair.
                idx++;
                contextLineOffset -= 2;
            }
        }
        return contextLine;
    }

    /**
     * Obtain the character offset within context line which has an error.
     * 
     * @return the character offset relative to context line or -1 if no context
     *         line.
     */
    public int getContextLineOffset() {
        if (null == contextLine)
            getContextLine();
        return contextLineOffset;
    }

    /**
     * Returns a string suitable for line precise error descriptions. Specifically:
     * 
     * <pre>
     * ${sourceName}:${lineNumber}: ${format_args}
     * ${lineDisplay}
     *      ^
     * location: ${jsonPath}
     * </pre>
     * 
     * @param format a {@link String#format(String, Object...)} string
     * @param args   is a list of arguments to pass to the format string.
     * @return a nicely formatted string suitable for line precise errors.
     */
    public String format(String format, Object... args) {
        StringBuilder sb = new StringBuilder();
        sb.append(getSourceName()).append(":").append(getLineNumber()).append(": ").append(String.format(format, args))
                .append("\n");
        String contextLine = getContextLine();
        if (null != contextLine) {
            sb.append(contextLine).append("\n").append(maskWithCaret(contextLine, getContextLineOffset()));
        }
        sb.append("location: /");
        boolean isFirst = true;
        for (JSON5Key key : getPath()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append("/");
            }
            sb.append(key.toString());
        }
        return sb.toString();
    }

    // Is this a UTF-8 newline?
    private static boolean isNewline(byte[] bytes, int position, int limit) {
        switch (bytes[position] & 0xFF) {
            case 0x0A:
            case 0x0D:
                return true;
            case 0xE2:
                if (limit - position < 3)
                    return false;
                // Handle LS & PS
                return bytes[position + 1] == 0x80 && (bytes[position + 2] & 0xFE) == 0xA8;
        }
        return false;
    }

    private static String maskWithCaret(String input, int charOffset) {
        StringBuilder sb = new StringBuilder(input.substring(0, charOffset));
        for (int idx = 0; idx < sb.length(); idx++) {
            char ch = sb.charAt(idx);
            if (!Character.isWhitespace(ch)) {
                int width = Wcwidth.of(ch);
                char[] fill = new char[width];
                Arrays.fill(fill, ' ');
                sb.replace(idx, idx + width, new String(fill));
            }
        }
        sb.append("^\n");
        return sb.toString();
    }
}