package com.brimworks.json5;

import com.brimworks.json5.ragel.Ragel;
import java.util.ArrayList;
import java.util.List;

class JSON5Lexer extends Ragel {
    private JSON5Visitor visitor;

    JSON5Lexer(JSON5Visitor visitor) {
        this.visitor = visitor;
    }
    /**
     * Maintains the location of the start of the current token.
     */
    private int tsLine;
    private long tsOffset;

    private void tokenStart() {
        tsLine = line;
        tsOffset = offset + p;
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

# Various unicode characters encoded as UTF-8:
LS = 0xE2 0x80 0xA8;
PS = 0xE2 0x80 0xA9;
ZWNJ = 0xE2 0x80 0x8C;
ZWJ = 0xE2 0x80 0x8D;
BOM = 0xEF 0xBB 0xBF;
TAB = 0x09;
VT = 0x0B;
FF = 0x0C;
SP = 0x20;
LF = 0x0A;
CR = 0x0D;
NBSP = 0xC2 0xA0;

# ECMA 26.2 parts:
WhiteSpace = TAB | VT | FF | SP | NBSP | BOM; # FIXME: \u{Zs}
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
    "0" [xX] HexDigit+;

DecimalIntegerLiteral =
    "0" |
    NonZeroDigit DecimalDigit*;

SignedInteger =
    DecimalDigit+ |
    "+" DecimalDigit+ |
    "-" DecimalDigit+;

ExponentPart =
    ExponentIndicator SignedInteger;

DecimalLiteral =
    DecimalIntegerLiteral "." DecimalDigit* ExponentPart? |
    "." DecimalDigit+ ExponentPart? |
    DecimalIntegerLiteral ExponentPart?;

NumericLiteral =
    DecimalLiteral |
    HexIntegerLiteral;

UnicodeLetter =
    # FIXME: \u{Lu} | \u{Ll} | \u{Lt} | \u{Lm} | \u{Lo} | \u{Nl}
    [a-zA-Z];

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
    # FIXME: \u{Mn} | \u{Mc}
    0xCC ( 0x80 .. 0xFF ) |
    0xCD ( 0 .. 0xAF );

UnicodeDigit =
    # FIXME: \u{Nd}
    [0-9]
        >{ mark=p; }
        %{ appendStringBufferUTF8(mark, p); };

LineContinuation =
    "\\" LineTerminatorSequence;

UnicodeConnectorPunctuation =
    # FIXME: \u{Pc}
    "_";

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
    "Infinity" |
    "NaN";

JSON5Number =
    JSON5NumericLiteral |
    "+" JSON5NumericLiteral |
    "-" JSON5NumericLiteral;

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
    JSON5Number        > { tokenStart(); }
        { visitor.visit(resetNumber(), tsLine, tsOffset); };
    any                > { tokenStart(); }
        { visitor.unexpectedByte(data.get(p), tsLine, tsOffset); };
*|;

}%%
