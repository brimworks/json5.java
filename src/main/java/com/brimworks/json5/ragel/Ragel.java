package com.brimworks.json5.ragel;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.io.IOException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.math.BigDecimal;
import java.math.BigInteger;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Extend this class when generating your ragel tokenizer to make buffer
 * management easier and event firing easier. To implement your tokenizer define
 * your class as such:
 * 
 * <pre>
 * public class Lexer extends Ragel {
 *     %% getkey (data.get(p) & 0xff);
 *     %% alphtype int;
 *     %% write data;
 *     &#64;Override
 *     protected void init() {
 *         %% write init;
 *     }
 *     &#64;Override
 *     protected void exec() {
 *         %% write exec;
 *     }
 * }
 * </pre>
 * 
 * Consumers of the lexer can use {@link #lex(ReadableByteChannel)} to read from
 * a channel, or might want to manually feed {@code ByteBuffer}s by calling
 * {@link #reset()} followed by {@link #lex(ByteBuffer, boolean)} one or more
 * times.
 * 
 * Implementors will have all the standard ragel variables already setup and
 * ready along with some handy methods to build a numeric or string token.
 */
abstract public class Ragel {
    private static final double LOG_10_TO_2_RATIO = Math.log(10) / Math.log(2);

    /**
     * Standard "interface to host" variables. Be sure to properly define getkey so
     * that data can be properly dereferenced.
     */
    protected ByteBuffer data;
    protected int cs, p, pe, ts, te, act, eof;

    /**
     * Generic mark, typically used for marking the start of a subtoken.
     */
    protected int mark;

    /**
     * Offset is relative to the entire input we have parsed.
     */
    protected long offset;

    /**
     * Subclasses should increment this when a newline is found.
     */
    protected int line;

    /**
     * Used for accumulating a string token value.
     */
    private CharBuffer stringBuffer;

    /**
     * Used for accumulating the significand value of a number.
     */
    private long numberValue = 0;
    private BigInteger numberValueBig = null;
    private double numberValueSpecial;

    /**
     * Used for accumulating the sign value of a number (always 1 or -1)
     */
    private int numberSign = 1;

    /**
     * Used for accumulating the scale of a number.
     */
    private int numberScale = 0;
    private int numberExponent = 0;
    private int numberExponentSign = 1;

    private CharsetDecoder utf8Decoder = UTF_8.newDecoder().onUnmappableCharacter(CodingErrorAction.REPLACE)
            .onMalformedInput(CodingErrorAction.REPLACE);

    /**
     * An internal number significand is accumulated, this method multiplies that
     * number by the specified base and adds to the number. If scaleDown is true,
     * the scale of the number is also decreased by one (do this after finding the
     * "." in a floating point number).
     * 
     * @param number       is a number from {@code 0} to {@code base-1}.
     * @param base         is the base being used by the number.
     * @param isFractional set to true when appending a fractional part.
     */
    protected void appendNumber(int number, int base, boolean isFractional) {
        if (null != numberValueBig) {
            numberValueBig = numberValueBig.multiply(BigInteger.valueOf(base)).add(BigInteger.valueOf(number));
        } else {
            long newValue = numberValue * base + number;
            if (newValue < 0) {
                numberValueBig = BigInteger.valueOf(numberValue);
                appendNumber(number, base, isFractional);
                return;
            }
            numberValue = newValue;
        }
        if (isFractional)
            numberScale--;
    }

    /**
     * An internal number decimal exponent is scaled.
     * 
     * @param number
     */
    protected void appendExponent(int number) {
        int newExponent = numberExponent * 10 + number;
        if (newExponent < 0) {
            exponentOverflow(number);
        } else {
            numberExponent = newExponent;
        }
    }

    protected void negateExponent() {
        numberExponentSign *= -1;
    }

    /**
     * When a negative sign is found, call this method.
     */
    protected void negateNumber() {
        numberSign *= -1;
    }

    /**
     * Used to explicitly set number to one of those "special" values like Nan and
     * Infinity.
     * 
     * @param special a non-zero double value.
     */
    protected void setNumber(double special) {
        if (0 == special)
            throw new AssertionError("setNumber must be called with non-zero value");
        numberValueSpecial = special;
    }

    /**
     * Obtain and reset the value in the number.
     * 
     * @return the current value in the number
     */
    protected Number resetNumber() {
        Number result = getNumber();
        numberValue = 0;
        numberValueBig = null;
        numberValueSpecial = 0;
        numberSign = 1;
        numberScale = 0;
        numberExponent = 0;
        numberExponentSign = 1;
        return result;
    }

    private Number getNumber() {
        if (0 != numberValueSpecial)
            return numberValueSpecial;
        // Calculate the final scale:
        int scale = numberExponentSign * (numberExponent + numberScale);
        if (null != numberValueBig) {
            // Handle big numbers.
            BigInteger result = numberSign < 0 ? numberValueBig.negate() : numberValueBig;
            if (scale < 0) {
                return new BigDecimal(result, scale);
            } else if (scale > 0) {
                return result.multiply(BigInteger.valueOf(10).pow(scale));
            }
            return result;
        }
        long value = numberValue;
        if (scale < 0) {
            // NOTE: Due to the base conversion, numbers
            // can not be perfectly represented.
            double base2Scale = scale * LOG_10_TO_2_RATIO;
            // Must represent as a floating point value:
            if (value < (1 << 23) && base2Scale < (1 << 8)) {
                // IEEE 754 single precision:
                return (float) (numberSign * value * Math.pow(10, scale));
            } else if (value < (1 << 52) && base2Scale < (1 << 11)) {
                // IEEE 754 double precision:
                return numberSign * value * Math.pow(10, scale);
            } else {
                BigDecimal result = new BigDecimal(BigInteger.valueOf(value), scale);
                if (numberSign < 0)
                    result = result.negate();
                return result;
            }
        } else if (scale > 0) {
            // Handle positive scale (may still be integer)
            BigInteger result = BigInteger.valueOf(value).multiply(BigInteger.valueOf(10).pow(scale));
            try {
                value = result.longValueExact();
            } catch (ArithmeticException ex) {
                // Can't represent as a long:
                if (numberSign < 0) result = result.negate();
                return result;
            }
        }
        // Simple long:
        if (value <= Byte.MAX_VALUE) {
            return (byte)(value * numberSign);
        } else if (value <= Short.MAX_VALUE) {
            return (short)(value * numberSign);
        } else if (value <= Integer.MAX_VALUE) {
            return (int)(value * numberSign);
        }
        return value * numberSign;
    }

    /**
     * Decodes a sequence of ASCII hex number
     * 
     * @param begin offset within data inclusive
     * @param end   offset within data exclusive
     * @return number
     */
    protected int decodeAsciiHex(int begin, int end) {
        int result = 0;
        for (; begin < end; begin++) {
            byte ch = data.get(begin);
            if (ch <= '9') {
                ch -= (byte) '0';
            } else if (ch <= 'F') {
                ch -= (byte) 'A';
            } else if (ch <= 'f') {
                ch -= (byte) 'a';
            }
            result = result * 16 + ch;
        }
        return result;
    }

    /**
     * Decode a single ASCII hex number
     * 
     * @param pos offset within data containing the number.
     * @return number
     */
    protected int decodeAsciiHex(int pos) {
        int ch = data.get(pos) & 0xFF;
        if (ch <= '9') {
            return ch - '0';
        } else if (ch <= 'F') {
            return ch - 'A';
        } else if (ch <= 'f') {
            return ch - 'a';
        }
        throw new AssertionError(String.format("Unexpected ch=0x%02X, was not a hex character", ch));
    }

    /**
     * Append UTF8 encoded elements from data into the internal string buffer.
     * 
     * @param begin start offset within data inclusive.
     * @param end   end offset within data exclusive.
     */
    protected void appendStringBufferUTF8(int begin, int end) {
        if (null == stringBuffer)
            stringBuffer = CharBuffer.allocate(8 * 1024);
        ByteBuffer slice = data.slice();
        slice.position(begin);
        slice.limit(end);
        while (CoderResult.OVERFLOW == utf8Decoder.decode(slice, stringBuffer, true)) {
            CharBuffer fresh = CharBuffer.allocate(stringBuffer.capacity() * 2);
            fresh.put(stringBuffer);
            stringBuffer = fresh;
        }
    }

    /**
     * Append a code point to the internal string buffer.
     * 
     * @param codePoint
     */
    protected void appendStringBufferCodePt(int codePoint) {
        if (null == stringBuffer)
            stringBuffer = CharBuffer.allocate(8 * 1024);
        int width = codePoint < 0x10000 ? 1 : 2;
        // Enough space?
        if (stringBuffer.position() >= stringBuffer.limit() - width) {
            CharBuffer fresh = CharBuffer.allocate(stringBuffer.capacity() * 2);
            fresh.put(stringBuffer);
            stringBuffer = fresh;
        }
        if (1 == width) {
            stringBuffer.put((char) codePoint);
        } else {
            stringBuffer.put(Character.highSurrogate(codePoint));
            stringBuffer.put(Character.lowSurrogate(codePoint));
        }
    }

    /**
     * Obtain and reset the value in the string buffer.
     * 
     * @return the current value in the string buffer.
     */
    protected String resetStringBuffer() {
        stringBuffer.flip();
        String result = stringBuffer.toString();
        stringBuffer.clear();
        return result;
    }

    /**
     * Subclasses should implement via {@code write init}
     */
    abstract protected void ragelInit();

    /**
     * Subclasses should implement via {@code write exec}
     */
    abstract protected void ragelExec();

    /**
     * Subclasses should implement by handling an exponent overflow condition. This
     * occurs if {@link #appendExponent(int)} is called and the int was not large
     * enough to hold the exponent (input specified an exponent greater than
     * {@link Integer.MAX_VALUE}). Note that the internal number state remains
     * unchanged, so if this is overridden with an empty body implementation, one
     * could recover from the error by ignoring the subsequent digits of the
     * expontent.
     * 
     * @param number is the value which was passed to appendExponent.
     */
    abstract protected void exponentOverflow(int number);

    /**
     * Reset (or initialize) internal lexical analysis state.
     */
    public void reset() {
        ragelInit();
        line = 1;
        offset = 0;
        mark = -1;
        if (null != stringBuffer)
            stringBuffer.clear();
        numberValue = 0;
        numberValueBig = null;
        numberSign = 1;
        numberScale = 0;
    }

    /**
     * Perform lexical analysis on an input buffer, be sure to call {@link #reset()}
     * before making iterative calls to this method.
     * 
     * @param data is a buffer which must be large enough to hold the largest token,
     *             if the buffer is not large enough, false is returned.
     * @param eof  should be set to true to indicate the end of stream has ben
     *             found.
     * @return true if progress is being, otherwise ByteBuffer capacity is not
     *         sufficient for the token... which probably indicates malicious input
     *         or unexpectedly large input.
     */
    public boolean lex(ByteBuffer data, boolean eof) {
        this.data = data;
        // Obtain the bounds:
        pe = data.limit();
        enter(data.position());
        if (eof)
            this.eof = pe;
        ragelExec();
        int pos = ts >= 0 ? ts : p;
        boolean progress = pos > data.position();
        // Update the position:
        data.position(pos);
        exit(pos);
        return progress;
    }

    /**
     * Perform lexical analysis on the entire {@code ReadableByteChannel} by
     * resetting the internal state, and reading until the stream end.
     * 
     * @param in the channel to read from
     * @throws IOException if {@code in} encounters an error reading.
     */
    public void lex(ReadableByteChannel in) throws IOException {
        reset();
        ByteBuffer buff = ByteBuffer.allocate(8 * 1024);
        boolean eof = false;
        while (!eof) {
            eof = in.read(buff) <= 0;
            buff.flip();
            if (!lex(buff, eof) && !eof) {
                ByteBuffer fresh = ByteBuffer.allocate(buff.capacity() * 2);
                fresh.put(buff);
                buff = fresh;
            } else {
                buff.compact();
            }
        }
    }

    private void enter(int pos) {
        if (ts >= 0)
            ts += pos;
        p += pos;
        if (te >= 0)
            te += pos;
        if (mark >= 0)
            mark += ts;
        offset -= pos;
    }

    private void exit(int pos) {
        if (ts >= 0)
            ts -= pos;
        p -= pos;
        if (te >= 0)
            te -= pos;
        if (mark >= 0)
            mark -= pos;
        offset += pos;
    }
}