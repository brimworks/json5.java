package com.brimworks.json5;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.channels.FileChannel;
import java.io.IOException;
import java.io.UncheckedIOException;
import static java.nio.file.StandardOpenOption.READ;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Collectors;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

public class JSON5ParserTest {
    private static String SOURCE = "JSON5ParserTest.java";

    public static List<String> json5Tests() throws IOException {
        // return
        // Collections.singletonList("src/test/resources/json5-tests/comments/inline-comment-following-top-level-value.json5");
        return Files.walk(Paths.get("src/test/resources/json5-tests")).filter(
                path -> !path.toFile().isDirectory() && path.getFileName().toString().matches(".*[.](json5?|js|txt)$"))
                .map(Path::toString).collect(Collectors.toList());
    }

    private JSON5Parser parser = new JSON5Parser();

    @Tag("unit")
    @ParameterizedTest
    @MethodSource
    public void json5Tests(String path) throws IOException {
        boolean expectSuccess = path.matches(".*[.]json5?$");
        boolean ok = false;
        try {
            parser.parse(Paths.get(path));
            ok = true;
        } catch (Exception ex) {
            if (expectSuccess)
                fail("Unexpected error in '" + path + "'", ex);
        }
        if (expectSuccess == ok) {
            return;
        }
        fail("Expected " + (expectSuccess ? "success" : "failure") + ", but got success when parsing " + path);
    }

    private void assertParsed(String txt, Object expect) {
        Object[] got = new Object[1];
        parser.setVisitor(new JSON5Visitor() {
            @Override
            public void visit(Number val, int line, long offset) {
                got[0] = val;
            }

            @Override
            public void visit(String val, int line, long offset) {
                got[0] = val;
            }
        });
        got[0] = null;
        parser.parse(txt, SOURCE);
        assertEquals(expect, got[0]);
    }

    @Tag("unit")
    @Test
    public void parseIntegers() throws IOException {
        assertParsed("123", Byte.valueOf((byte) 123));
        assertParsed("12345", Short.valueOf((short) 12345));
        assertParsed("1234567890", Integer.valueOf(1234567890));

        // MIN/MAX (although note that true MAX is promoted to long)
        assertParsed("127", Byte.valueOf((byte) 127));
        assertParsed("-127", Byte.valueOf((byte) -127));

        assertParsed("32767", Short.valueOf((short) 32767));
        assertParsed("-32767", Short.valueOf((short) -32767));

        assertParsed("2147483647", Integer.valueOf(2147483647));
        assertParsed("-2147483647", Integer.valueOf(-2147483647));

        assertParsed("9223372036854775807", Long.valueOf(Long.MAX_VALUE));
        assertParsed("-9223372036854775807", Long.valueOf(-Long.MAX_VALUE));

        assertParsed("92233720368547758070", new BigInteger("92233720368547758070"));
        assertParsed("-92233720368547758070", new BigInteger("-92233720368547758070"));
    }

    @Tag("unit")
    @Test
    public void parseFloats() throws IOException {
        assertParsed("10.0", Float.valueOf(10.0f));
        assertParsed("3.402823E38", Float.valueOf((float)3.402823E38));
        assertParsed(""+(-3.402823E38), Float.valueOf((float)-3.402823E38));

        assertParsed("3.402824E38", Float.valueOf((float)3.402824E38));
        assertParsed(""+(-3.402824E38), Float.valueOf((float)-3.402824E38));

        assertParsed("1.797693134862315E308", Double.valueOf(1.797693134862315E308));
        assertParsed("-1.797693134862315E308", Double.valueOf(-1.797693134862315E308));

        // +1 should flop over to BigDecimal:
        assertParsed("1.7976931348623158E308", new BigDecimal(BigInteger.valueOf(17976931348623158L), -(308-16)));

    }
}