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

public class JSON5LexerTest {
    public static List<String> json5Tests() throws IOException {
        return Files.walk(Paths.get("src/test/resources/json5-tests")).filter(
                path -> !path.toFile().isDirectory() && path.getFileName().toString().matches(".*[.](json5?|js|txt)$"))
                .map(Path::toString).collect(Collectors.toList());
    }

    @Tag("unit")
    @ParameterizedTest
    @MethodSource
    public void json5Tests(String path) throws IOException {
        JSON5Lexer lexer = new JSON5Lexer(new JSON5Visitor() {
        });
        FileChannel input;
        try {
            input = FileChannel.open(Paths.get(path), READ);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        boolean expectSuccess = path.matches(".*[.]json5?$");
        boolean ok = false;
        try {
            lexer.lex(input);
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
}