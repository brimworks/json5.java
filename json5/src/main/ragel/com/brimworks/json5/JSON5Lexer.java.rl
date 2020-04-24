package com.brimworks.json5;

import com.brimworks.json5.ragel.Ragel;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements the lexer using ragel, please see {@link JSON5Parser} which uses this.
 */
class JSON5Lexer extends Ragel {
    /**
     * A few callbacks are specific to the Lexer -> Parser and are thus not
     * part of the public JSON5Visitor interface.
     */
    interface Visitor extends JSON5Visitor {
        /**
         * Indicates an unexpected character found in the input.
         */
        default void unexpectedByte(byte ch, int line, long offset) {}
        /**
         * Indicates an exponent number was specified in the source-text
         * which is larger than {@link Integer.MAX_VALUE}.
         */
        default void exponentOverflow(int line, long offset) {}
    }

    private Visitor visitor;

    /**
     * Maintains the location of the start of the current token.
     */
    private int tsLine;
    private long tsOffset;
    /**
     * Keep track of if we are within a fractional part of a number.
     */
    private boolean inFraction;

    /**
     * Constructs a lexer which emits tokens of the JSON5 language.
     *
     * @param visitor to use when tokens are found.
     */
    JSON5Lexer(Visitor visitor) {
        this.visitor = visitor;
    }

    private void tokenStart() {
        tsLine = line;
        tsOffset = offset + p;
    }
    protected void exponentOverflow(int num) {
        // FIXME: Can a number have a newline within it? If so, the
        // line number will be incorrect. I don't think this is possible though.
        visitor.exponentOverflow(tsLine, offset + p);
    }

    %% machine json5;
    %% alphtype int;
    %% getkey (data.get(p) & 0xff);
    %% write data;
    @Override
    protected void ragelInit() {
        %% write init;
    }
    @Override
    protected void ragelExec() {
        %% write exec;
        if ( p == eof ) {
            visitor.endOfStream(line, offset + p);
        }
    }
}

// Assumes input in UTF-8 byte array encoding.
%%{

include Unicode_UTF8 "Unicode_UTF8.rl";

# ECMA 26.2 parts:
WhiteSpace = TAB | VT | FF | SP | NBSP | BOM | U_Zs;
LineTerminatorSequence =
    ( LF |
      CR | # FIXME: lookahead not LF
      LS |
      PS |
      CR LF ) %{ line++; };
HexDigit = [0-9a-fA-F];
DecimalDigit = [0-9];
NonZeroDigit = [1-9];
ExponentIndicator = [eE];

HexIntegerLiteral =
    "0" [xX] HexDigit+ @{ appendNumber(decodeAsciiHex(p), 16, inFraction); };

DecimalIntegerLiteral =
    "0" |
    ( NonZeroDigit DecimalDigit* )
        @{ appendNumber(data.get(p)-'0', 10, inFraction); };

# FIXME: Scale number with exponent.
SignedInteger =
    ( "+" | "-" %{ negateExponent(); } )? DecimalDigit+
        @{ appendExponent(data.get(p)-'0'); };

ExponentPart =
    ExponentIndicator SignedInteger;

DecimalLiteral =
    DecimalIntegerLiteral "." %{ inFraction=true; }
        DecimalDigit* @{ appendNumber(data.get(p)-'0', 10, inFraction); }
        ExponentPart? |
    "." %{ inFraction=true; }
        DecimalDigit+ @{ appendNumber(data.get(p)-'0', 10, inFraction); }
        ExponentPart? |
    DecimalIntegerLiteral ExponentPart?;

NumericLiteral =
    DecimalLiteral |
    HexIntegerLiteral;

UnicodeLetter =
    U_Lu | U_Ll | U_Lt | U_Lm | U_Lo | U_Nl; 

SingleEscapeCharacter =
    ( "'" |
      '"' |
      "\\" )
        %{ appendStringBufferCodePt(p); } |
    'b' %{ appendStringBufferCodePt(0x08); } |
    'f' %{ appendStringBufferCodePt(0x0C); } |
    'n' %{ appendStringBufferCodePt(0x0A); } |
    'r' %{ appendStringBufferCodePt(0x0D); } |
    't' %{ appendStringBufferCodePt(0x09); } |
    'v' %{ appendStringBufferCodePt(0x0B); };

EscapeCharacter =
    SingleEscapeCharacter |
    DecimalDigit |
    "x" |
    "u";

# FIXME: This doesn't seem to work as expected:
NonEscapeCharacter =
    any -- ( EscapeCharacter | LineTerminatorSequence );

CharacterEscapeSequence =
    SingleEscapeCharacter;
    # |
    #NonEscapeCharacter
    #    %{ appendStringBufferCodePt(p); };

UnicodeEscapeSequence =
    "u" ( HexDigit HexDigit HexDigit HexDigit )
        >{ mark=p; }
        %{ appendStringBufferCodePt(decodeAsciiHex(mark, p)); };

HexEscapeSequence =
    "x" HexDigit HexDigit
        >{ mark=p; }
        %{ appendStringBufferCodePt(decodeAsciiHex(mark, p)); };

UnicodeCombiningMark =
    U_Mn |
    U_Mc;

UnicodeDigit =
    U_Nd
        >{ mark=p; }
        %{ appendStringBufferUTF8(mark, p); };

LineContinuation =
    "\\" LineTerminatorSequence;

UnicodeConnectorPunctuation =
    U_Pc;

MultiLineComment =
    "/*" any* :>> "*/";

SingleLineComment =
    "//" (any -- LineTerminatorSequence)* LineTerminatorSequence
    # Bummer, wish we could just "match" eof...
    $eof{
        appendStringBufferUTF8(ts, p--);
        visitor.visitComment(resetStringBuffer(), tsLine, tsOffset);
        fbreak;
    };

Comment =
    MultiLineComment |
    SingleLineComment;

IdentifierStart =
    ( UnicodeLetter |
     "$" |
     "_" )
        >{ mark=p; }
        %{ appendStringBufferUTF8(mark, p); } |
    "\\" UnicodeEscapeSequence;

IdentifierPart =
    IdentifierStart |
    ( UnicodeCombiningMark |
      UnicodeDigit |
      UnicodeConnectorPunctuation |
      ZWNJ |
      ZWJ )
        >{ mark=p; }
        %{ appendStringBufferUTF8(mark, p); };

IdentifierName =
    IdentifierStart IdentifierPart*;

EscapeSequence =
    CharacterEscapeSequence |
    "0" # FIXME: lookahead not DecimalDigit
        %{ appendStringBufferCodePt(0); } |
    HexEscapeSequence |
    UnicodeEscapeSequence;

# JSON5 parts:
JSON5DoubleStringCharacter =
    ([^"\\] -- LineTerminatorSequence)+
        >{ mark=p; }
        %{ appendStringBufferUTF8(mark, p); }
    (   "\\" ( LineTerminatorSequence | EscapeSequence )
        ( [^"\\] -- LineTerminatorSequence ) *
            >{ mark=p; }
            %{ appendStringBufferUTF8(mark, p); } )*;

JSON5SingleStringCharacter =
    ([^'\\] -- LineTerminatorSequence)+
        >{ mark=p; }
        %{ appendStringBufferUTF8(mark, p); }
    (   "\\" ( LineTerminatorSequence | EscapeSequence )
        ([^'\\] -- LineTerminatorSequence)*
            >{ mark=p; }
            %{ appendStringBufferUTF8(mark, p); } )*;

JSON5String =
    '"' JSON5DoubleStringCharacter* '"' |
    "'" JSON5SingleStringCharacter* "'";

JSON5NumericLiteral =
    NumericLiteral |
    "Infinity" %{ setNumber(Double.POSITIVE_INFINITY); } |
    "NaN" %{ setNumber(Double.NaN); };

JSON5Number =
    JSON5NumericLiteral |
    "+" JSON5NumericLiteral |
    "-" @{ negateNumber(); } JSON5NumericLiteral;

# FIXME: Don't we want to exclude reserved words so it is valid EMCAScript?
JSON5Identifier =
    IdentifierName;

# Accumulate at most one line before emitting a token:
Space =
    ( WhiteSpace* LineTerminatorSequence ) | WhiteSpace+;

# JSON5InputElement
main := |*
    Space              > { tokenStart(); }
        {   appendStringBufferUTF8(ts, te);
            visitor.visitSpace(resetStringBuffer(), tsLine, tsOffset); };
    Comment            > { tokenStart(); }
        {   appendStringBufferUTF8(ts, te);
            visitor.visitComment(resetStringBuffer(), tsLine, tsOffset); };
    "{"                > { tokenStart(); }
        { visitor.startObject(tsLine, tsOffset); };
    "}"                > { tokenStart(); }
        { visitor.endObject(tsLine, tsOffset); };
    "["                > { tokenStart(); }
        { visitor.startArray(tsLine, tsOffset); };
    "]"                > { tokenStart(); }
        { visitor.endArray(tsLine, tsOffset); };
    ","                > { tokenStart(); }
        { visitor.append(tsLine, tsOffset); };
    ":"                > { tokenStart(); }
        { visitor.endObjectKey(tsLine, tsOffset); };
    "null"             > { tokenStart(); }
        { visitor.visitNull(tsLine, tsOffset); };
    "true"             > { tokenStart(); }
        { visitor.visit(true, tsLine, tsOffset); };
    "false"            > { tokenStart(); }
        { visitor.visit(false, tsLine, tsOffset); };
    JSON5Identifier    > { tokenStart(); }
        { visitor.visit(resetStringBuffer(), tsLine, tsOffset); };
    JSON5String        > { tokenStart(); }
        { visitor.visit(resetStringBuffer(), tsLine, tsOffset); };
    JSON5Number        > { tokenStart(); inFraction=false; }
        { visitor.visit(resetNumber(), tsLine, tsOffset); };
    any                > { tokenStart(); }
        { visitor.unexpectedByte(data.get(p), tsLine, tsOffset); };
*|;

}%%