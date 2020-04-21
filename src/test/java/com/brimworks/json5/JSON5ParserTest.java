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
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

public class JSON5ParserTest {
    private static String SOURCE = "JSON5ParserTest.java";
    public static List<String> json5Tests() throws IOException {
        // return Collections.singletonList("src/test/resources/json5-tests/comments/inline-comment-following-top-level-value.json5");
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
    @Tag("unit")
    @Test
    public void parseNumbers() throws IOException {
        Number[] got = new Number[1];
        parser.setVisitor(new JSON5Visitor() {
            @Override
            public void visit(Number val, int line, long offset) {
                got[0] = val;
            }
        });
        got[0] = null;
        parser.parse("123", SOURCE);
        assertEquals(Byte.valueOf((byte)123), got[0]);
        parser.parse("12345", SOURCE);
        assertEquals(Short.valueOf((short)12345), got[0]);
        parser.parse("1234567890", SOURCE);
        assertEquals(Integer.valueOf(1234567890), got[0]);

        // MIN/MAX (although note that true MAX is promoted to long)
        parser.parse("127", SOURCE);
        assertEquals(Byte.valueOf((byte)127), got[0]);
        parser.parse("-127", SOURCE);
        assertEquals(Byte.valueOf((byte)-127), got[0]);

        parser.parse("32767", SOURCE);
        assertEquals(Short.valueOf((short)32767), got[0]);
        parser.parse("-32767", SOURCE);
        assertEquals(Short.valueOf((short)-32767), got[0]);

        parser.parse("2147483647", SOURCE);
        assertEquals(Integer.valueOf(2147483647), got[0]);
        parser.parse("-2147483647", SOURCE);
        assertEquals(Integer.valueOf(-2147483647), got[0]);

        parser.parse("9223372036854775807", SOURCE);
        assertEquals(Long.valueOf(Long.MAX_VALUE), got[0]);
        parser.parse("-9223372036854775807", SOURCE);
        assertEquals(Long.valueOf(-Long.MAX_VALUE), got[0]);

    }
}