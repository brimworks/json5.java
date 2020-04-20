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
    public JSON5Location getLocation(String sourceName, List<JSON5Key> path, JSON5Location.Read readSource) {
        return new JSON5Location(offset + p, line, sourceName, path, readSource);
    }

    %% machine json5;
    %% getkey (data.get(p) & 0xff);
    %% write data;
    @Override
    protected void ragelInit() {
        %% write init;
    }
    @Override
    protected void ragelExec() {
        %% write exec;
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
      CR LF ) @{ line++; };
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
    "'" |
    '"' |
    "\\" |
    [bfnrtv];

EscapeCharacter =
    SingleEscapeCharacter |
    DecimalDigit |
    "x" |
    "u";

NonEscapeCharacter =
    any - ( EscapeCharacter | LineTerminatorSequence );

CharacterEscapeSequence =
    SingleEscapeCharacter
    NonEscapeCharacter;

UnicodeEscapeSequence =
    "u" HexDigit HexDigit HexDigit HexDigit;

HexEscapeSequence =
    "x" HexDigit HexDigit;

UnicodeCombiningMark =
    # FIXME: \u{Mn} | \u{Mc}
    0xCC ( 0x80 .. 0xFF ) |
    0xCD ( 0x00 .. 0xAF );

UnicodeDigit =
    # FIXME: \u{Nd}
    [0-9];

LineContinuation =
    "\\" LineTerminatorSequence;

UnicodeConnectorPunctuation =
    # FIXME: \u{Pc}
    "_";

MultiLineComment =
    "/*" ( any* - ( any* "*/" any* ) ) "*/";

SingleLineComment =
    "//" ( any* - LineTerminatorSequence );

Comment =
    MultiLineComment |
    SingleLineComment;

IdentifierStart =
    UnicodeLetter |
    "$" |
    "_" |
    "\\" UnicodeEscapeSequence;

IdentifierPart =
    IdentifierStart |
    UnicodeCombiningMark |
    UnicodeDigit |
    UnicodeConnectorPunctuation |
    ZWNJ |
    ZWJ;

IdentifierName =
    IdentifierStart IdentifierPart*;

EscapeSequence =
    CharacterEscapeSequence |
    "0" | # FIXME: lookahead not DecimalDigit
    HexEscapeSequence |
    UnicodeEscapeSequence;

# JSON5 parts:
JSON5DoubleStringCharacter =
    ( any - ( '"' | "\\" | LineTerminatorSequence ) ) |
    "\\" EscapeSequence |
    LineContinuation |
    LS |
    PS;

JSON5SingleStringCharacter =
    ( any - ( "'" | "\\" | LineTerminatorSequence ) ) |
    "\\" EscapeSequence |
    LineContinuation |
    LS |
    PS;

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

JSON5Punctuator =
    "{" @{ visitor.startObject(tsLine, tsOffset); } |
    "}" @{ visitor.endObject(tsLine, tsOffset); } |
    "[" @{ visitor.startArray(tsLine, tsOffset); } |
    "]" @{ visitor.endArray(tsLine, tsOffset); } |
    "," @{ visitor.append(tsLine, tsOffset); } |
    ":" @{ visitor.endObjectKey(tsLine, tsOffset); };

JSON5Token =
    JSON5Identifier @{ visitor.visit(resetStringBuffer(), tsLine, tsOffset); } |
    JSON5Punctuator |
    JSON5String @{ visitor.visit(resetStringBuffer(), tsLine, tsOffset); } |
    JSON5Number @{ visitor.visit(resetNumber(), tsLine, tsOffset); };

NullLiteral =
    "null" @{ visitor.visitNull(tsLine, tsOffset); };

BooleanLiteral =
    "true" @{ visitor.visit(true, tsLine, tsOffset); } |
    "false" @{ visitor.visit(false, tsLine, tsOffset); };

# JSON5InputElement
main := |*
    WhiteSpace;
    LineTerminatorSequence;
    Comment;
    NullLiteral >{ tokenStart(); };
    BooleanLiteral > { tokenStart(); };
    JSON5Token > { tokenStart(); };
    any { visitor.unexpectedByte(data.get(p), tsLine, tsOffset); };
*|;

}%%
