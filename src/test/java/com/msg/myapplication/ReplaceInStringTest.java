package com.msg.myapplication;

import group.msg.jpowermonitor.junit.JPowerMonitorExtension;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({JPowerMonitorExtension.class})
@Slf4j
public class ReplaceInStringTest {
    private static final int NUM_RUNS = 300_000;
    //  private static final int NUM_RUNS = 1;
    // A carriage return means moving the cursor to the beginning of the line. The code is \r.
    // A line feed means moving one line forward. The code is \n.
    private static final Pattern PATTERN = Pattern.compile("\\R"); // same as "(\r)*\n"

    @BeforeAll
    static void prepare() throws IOException {
        // create input for parametrized tests
        byte[] simpleLf = Files.readAllBytes(Paths.get("src/test/resources/replace-string-test/simple-lf.txt"));
        byte[] simpleCrLf = Files.readAllBytes(Paths.get("src/test/resources/replace-string-test/simple-cr-lf.txt"));
        String simpleLfInput = new String(simpleLf, StandardCharsets.UTF_8);
        String simpleCrLfInput = new String(simpleCrLf, StandardCharsets.UTF_8);
        // assert that all three methods produce the same result:
        Assertions.assertEquals(ReplaceInStringTest.replaceUsingRegex(simpleLfInput), ReplaceInStringTest.replaceUsingForwardSearchAndChars(simpleLfInput));
        Assertions.assertEquals(ReplaceInStringTest.replaceUsingForwardSearchAndChars(simpleLfInput), ReplaceInStringTest.replaceUsingIndexOfAndStringReplace(simpleLfInput));
        Assertions.assertEquals(ReplaceInStringTest.replaceUsingRegex(simpleCrLfInput), ReplaceInStringTest.replaceUsingForwardSearchAndChars(simpleCrLfInput));
        Assertions.assertEquals(ReplaceInStringTest.replaceUsingForwardSearchAndChars(simpleCrLfInput), ReplaceInStringTest.replaceUsingIndexOfAndStringReplace(simpleCrLfInput));
        log.info("All methods produce same result");
    }

    static Stream<Arguments> provideStringsForReplacing() {
        return Stream.of(
            Arguments.of(Named.of("Text LF", "src/test/resources/replace-string-test/content-lf.txt"))
            , Arguments.of(Named.of("Text CRLF", "src/test/resources/replace-string-test/content-cr-lf.txt"))
            //  , Arguments.of(Named.of("Xml CRLF", "src/test/resources/replace-string-test/big-xml.xml"))
        );
    }

    @ParameterizedTest
    @MethodSource("provideStringsForReplacing")
    void testReplaceUsingRegularExpressions(String file) throws IOException {
        byte[] content = Files.readAllBytes(Paths.get(file));
        String input = new String(content, StandardCharsets.UTF_8);
        String expected = ReplaceInStringTest.replaceUsingForwardSearchAndChars(input);

        final long start = System.currentTimeMillis();
        String res = null;
        for (int i = 0; i < NUM_RUNS; i++) {
            res = ReplaceInStringTest.replaceUsingRegex(input);
        }
        assertThat(res).isEqualTo(expected);
        log.info("testReplaceUsingRegularExpressions: {} ms", System.currentTimeMillis() - start);
    }

    @ParameterizedTest
    @MethodSource("provideStringsForReplacing")
    void testReplaceUsingForwardSearchAndChars(String file) throws IOException {
        byte[] content = Files.readAllBytes(Paths.get(file));
        String input = new String(content, StandardCharsets.UTF_8);
        String expected = ReplaceInStringTest.replaceUsingRegex(input);

        final long start = System.currentTimeMillis();
        String res = null;
        for (int i = 0; i < NUM_RUNS; i++) {
            res = ReplaceInStringTest.replaceUsingForwardSearchAndChars(input);
        }
        assertThat(res).isEqualTo(expected);
        log.info("testReplaceUsingForwardSearchAndChars: {} ms", System.currentTimeMillis() - start);
    }

    @ParameterizedTest
    @MethodSource("provideStringsForReplacing")
    void testReplaceUsingIndexOfAndStringReplace(String file) throws IOException {
        byte[] content = Files.readAllBytes(Paths.get(file));
        String input = new String(content, StandardCharsets.UTF_8);
        String expected = ReplaceInStringTest.replaceUsingRegex(input);

        final long start = System.currentTimeMillis();
        String res = null;
        for (int i = 0; i < NUM_RUNS; i++) {
            res = ReplaceInStringTest.replaceUsingIndexOfAndStringReplace(input);
        }
        assertThat(res).isEqualTo(expected);
        log.info("testReplaceUsingIndexOfAndStringReplace: {} ms", System.currentTimeMillis() - start);
    }

    /**
     * Replaces "linefeed" or "carriage return+linefeed" with the string "\n" (the characters 'backslash' and 'n').
     *
     * @param input the input that contains carriage return/linefeed.
     * @return String with "\n" instead of carriage return/linefeed.
     */
    private static String replaceUsingRegex(String input) {
        return PATTERN.matcher(input).replaceAll("\\\\n");
    }

    public static String replaceUsingForwardSearchAndChars(String input) {
        char[] chars = input.toCharArray();
        StringBuilder result = new StringBuilder();
        // replace \r\n with \n
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '\r' && i + 1 < chars.length && chars[i + 1] == '\n') {
                // If we find \r followed by \n, append \n and skip the next character
                result.append("\\n");
                i++; // Skip the '\n'
            } else if (chars[i] == '\n') {
                result.append("\\n");
            } else {
                result.append(chars[i]); // Otherwise, just append the current character
            }
        }
        return result.toString();
    }

    /**
     * Replaces "linefeed" or "carriage return+linefeed" with the string "\n" (the characters 'backslash' and 'n').
     *
     * @param input the input that contains carriage return/linefeed.
     * @return String with "\\n" instead of carriage return/linefeed.
     */
    public static String replaceUsingIndexOfAndStringReplace(final String input) {
        if (input == null) {
            return null;
        }
        final StringBuilder sb = new StringBuilder(input);
        int pos = sb.indexOf("\r\n");
        // Case 1: "linefeed" and "carriage return" exist in the string
        if (pos != -1) {
            while (pos != -1) {
                sb.replace(pos, pos + 2, "\\n");
                pos = sb.indexOf("\r\n", pos + 3);
            }
            // Case 2: "linefeed" or "carriage return" exist in the string
        } else {
            pos = sb.indexOf("\n");
            while (pos != -1) {
                sb.replace(pos, pos + 1, "\\n");
                pos = sb.indexOf("\n", pos + 2);
            }
        }
        return sb.toString();
    }
}
