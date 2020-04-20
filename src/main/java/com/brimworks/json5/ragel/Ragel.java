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
 * public class JSON5 extends Ragel<Token> {
 *     %% getkey (data.get(p) & 0xff);
 *     %% write data;
 *     @Override
 *     protected void init() {
 *         %% write init;
 *     }
 *     @Override
 *     protected void exec() {
 *         %% write exec;
 *     }
 * }
 * </pre>
 */
abstract public class Ragel {
    private static final double LOG_10_TO_2_RATIO = Math.log(10) / Math.log(2);

    /**
     * Standard "interface to host" variables. Be sure to properly define getkey so that
     * data can be properly dereferenced.
     */
    protected ByteBuffer data;
    protected int cs, p, pe, ts, te, act, eof;

    /**
     * Generic mark, typically used for marking a subtoken.
     */
    protected int mark = 0;

    /**
     * Offset is relative to the entire input we have parsed.
     */
    protected long offset = 0;

    /**
     * Subclasses should increment this when a newline is found.
     */
    protected int line = 0;

    /**
     * Used for accumulating a string token value.
     */
    private CharBuffer stringBuffer = CharBuffer.allocate(8*1024);

    /**
     * Used for accumulating the value of a number.
     */
    private long numberValue = 0;
    private BigInteger numberValueBig = null;

    /**
     * Used for accumulating the sign value of a number (always 1 or -1)
     */
    private int numberSign = 1;

    /**
     * Used for accumulating the scale of a number.
     */
    private int numberScale = 0;

    private CharsetDecoder utf8Decoder = UTF_8.newDecoder()
        .onUnmappableCharacter(CodingErrorAction.REPLACE)
        .onMalformedInput(CodingErrorAction.REPLACE);

    /**
     * An internal number significand is accumulated, this method
     * multiplies that number by the specified base and adds to
     * the number. If scaleDown is true, the scale of the number
     * is also decreased by one (do this after finding the "." in
     * a floating point number).
     * @param number is a number from {@code 0} to {@code base-1}.
     * @param base is the base being used by the number.
     * @param scaleDown should be set if appending a fractional part.
     */
    protected void appendNumber(int number, int base, boolean scaleDown) {
        if ( null != numberValueBig ) {
            numberValueBig = numberValueBig
                .multiply(BigInteger.valueOf(base))
                .add(BigInteger.valueOf(number));
        } else {
            long newValue = numberValue * base + number;
            if ( newValue < 0 ) {
                numberValueBig = BigInteger.valueOf(numberValue);
                appendNumber(number, base, scaleDown);
                return;
            }
            numberValue = newValue;
        }
        if ( scaleDown ) numberScale--;
    }

    /**
     * When a negative sign is found, call this method.
     */
    protected void negateNumber() {
        numberSign *= -1;
    }

    /**
     * Obtain and reset the value in the number.
     * @return the current value in the number
     */
    protected Number resetNumber() {
        Number result = getNumber();
        numberValue = 0;
        numberValueBig = null;
        numberSign = 1;
        numberScale = 0;
        return result;
    }

    private Number getNumber() {
            if ( null != numberValueBig ) {
            // Handle big numbers.
            BigInteger result = numberSign < 0 ? numberValueBig.negate() : numberValueBig;
            if ( numberScale < 0 ) {
                return new BigDecimal(result, numberScale);
            } else if ( numberScale > 0 ) {
                return result.multiply(BigInteger.valueOf(10).pow(numberScale));
            }
            return result;
        }
        if ( numberScale > 0 ) {
            // Handle positive scale (may still be integer)
            BigInteger result = BigInteger.valueOf(numberValue).multiply(BigInteger.valueOf(10).pow(numberScale));
            try {
                numberValue = numberValueBig.longValueExact();
            } catch ( ArithmeticException ex ) {
                // Scaled it, handle as big number.
                numberValueBig = result;
                numberScale = 0;
                return resetNumber();
            }
        } else if ( numberScale < 0 ) {
            // NOTE: Due to the base conversion, numbers
            // can not be perfectly represented.
            double base2Scale = numberScale*LOG_10_TO_2_RATIO;
            // Must represent as a floating point value:
            if ( numberValue < (1<<23) && base2Scale < (1<<8) ) {
                // IEEE 754 single precision:
                return (float)(numberSign*numberValue*Math.pow(10, numberScale));
            } else if ( numberValue < (1<<52) && base2Scale < (1<<11) ) {
                // IEEE 754 double precision:
                return numberSign*numberValue*Math.pow(10, numberScale);
            } else {
                BigDecimal result = new BigDecimal(BigInteger.valueOf(numberValue), numberScale);
                if ( numberScale < 0 ) result = result.negate();
                return result;
            }
        }
        // Simple integer
        numberValue *= numberScale;
        if ( numberValue < Byte.MAX_VALUE ) {
            return (byte)numberValue;
        } else if ( numberValue < Short.MAX_VALUE ) {
            return (short)numberValue;
        } else if ( numberValue < Integer.MAX_VALUE ) {
            return (int)numberValue;
        }
        return numberValue;
    }

    /**
     * Append UTF8 encoded elements from data to the stringBuffer.
     */
    protected void appendStringBufferUTF8(int begin, int end) {
        ByteBuffer slice = data.slice();
        slice.position(begin);
        slice.limit(end);
        while ( CoderResult.OVERFLOW == utf8Decoder.decode(slice, stringBuffer, true) ) {
            CharBuffer fresh = CharBuffer.allocate(stringBuffer.capacity()*2);
            fresh.put(stringBuffer);
            stringBuffer = fresh;
        }
    }

    /**
     * Obtain and reset the value in the string buffer.
     * @return the current value in the string buffer.
     */
    protected String resetStringBuffer() {
        stringBuffer.flip();
        String result = stringBuffer.toString();
        stringBuffer.clear();
        return result;
    }
    /**
     * @return true if progress is being, otherwise ByteBuffer capacity is not sufficient
     *     for the token... which probably indicates malicious input or unexpectedly large
     *     input.
     */
    public boolean lex(ByteBuffer data, boolean eof) {
        this.data = data;
        // Obtain the bounds:
        ts = data.position();
        pe = data.limit();
        if ( eof ) this.eof = pe;
        enter();
        ragelExec();
        boolean progress = ts > data.position();
        // Update the position:
        data.position(ts);
        exit();
        return progress;
    }
    public void lex(ReadableByteChannel in) throws IOException {
        reset();
        ByteBuffer buff = ByteBuffer.allocate(8*1024);
        while ( true ) {
            if ( in.read(buff) <= 0 ) break;
            buff.flip();
            if ( !lex(buff, false) ) {
                ByteBuffer fresh = ByteBuffer.allocate(buff.capacity()*2);
                fresh.put(buff);
                buff = fresh;
            } else {
                buff.compact();
            }
        }
        lex(buff, true);
    }
    public void reset() {
        ragelInit();
        line = 0;
        offset = 0;
        mark = 0;
        resetStringBuffer();
        resetNumber();
    }
    protected void enter() {
        p += ts;
        te += ts;
        mark += ts;
        offset -= ts;
    }
    protected void exit() {
        p -= ts;
        te -= ts;
        mark -= ts;
        offset += ts;
    }
    /**
     * Subclasses should implement via {@code write init}
     */
    abstract protected void ragelInit();
    /**
     * Subclasses should implement via {@code write exec}
     */
    abstract protected void ragelExec();
}