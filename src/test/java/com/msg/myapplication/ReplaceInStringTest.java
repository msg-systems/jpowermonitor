package com.msg.myapplication;

import group.msg.jpowermonitor.junit.JPowerMonitorExtension;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
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
    private static String SIMPLE_LF;
    private static String SIMPLE_CR_LF;
    private static String INPUT_LF;
    private static String INPUT_CR_LF;
    private static String OUT_SIMPLE_LF;
    private static String OUTPUT_LF; //
    private static final int NUM_RUNS = 1_000_000;

    // A carriage return means moving the cursor to the beginning of the line. The code is \r.
    // A line feed means moving one line forward. The code is \n.
    private static final Pattern PATTERN = Pattern.compile("\\R"); // same as "(\r)*\n"

    @BeforeAll
    static void prepare() throws IOException {
        // create input for parametrized tests
        byte[] simpleLf = Files.readAllBytes(Paths.get("src/test/resources/replace-string-test/simple-lf.txt"));
        byte[] simpleCrLf = Files.readAllBytes(Paths.get("src/test/resources/replace-string-test/simple-cr-lf.txt"));
        byte[] contentLf = Files.readAllBytes(Paths.get("src/test/resources/replace-string-test/content-lf.txt"));
        byte[] contentCrLf = Files.readAllBytes(Paths.get("src/test/resources/replace-string-test/content-cr-lf.txt"));
        SIMPLE_LF = new String(simpleLf, StandardCharsets.UTF_8);
        SIMPLE_CR_LF = new String(simpleCrLf, StandardCharsets.UTF_8);
        INPUT_LF = new String(contentLf, StandardCharsets.UTF_8);
        INPUT_CR_LF = new String(contentCrLf, StandardCharsets.UTF_8);

        // assert that all three methods produce the same result:
        Assertions.assertEquals(ReplaceInStringTest.replaceUsingRegex(SIMPLE_LF), ReplaceInStringTest.replaceUsingForwardSearchAndChars(SIMPLE_LF));
        Assertions.assertEquals(ReplaceInStringTest.replaceUsingForwardSearchAndChars(SIMPLE_LF), ReplaceInStringTest.replaceUsingIndexOfAndStringReplace(SIMPLE_LF));
        Assertions.assertEquals(ReplaceInStringTest.replaceUsingRegex(SIMPLE_CR_LF), ReplaceInStringTest.replaceUsingForwardSearchAndChars(SIMPLE_CR_LF));
        Assertions.assertEquals(ReplaceInStringTest.replaceUsingForwardSearchAndChars(SIMPLE_CR_LF), ReplaceInStringTest.replaceUsingIndexOfAndStringReplace(SIMPLE_CR_LF));
        log.info("All methods produce same result");

        // create input for parametrized tests and one reference output per file:
        OUT_SIMPLE_LF = ReplaceInStringTest.replaceUsingRegex(SIMPLE_LF);
        OUTPUT_LF = ReplaceInStringTest.replaceUsingRegex(INPUT_LF);
        log.info("Prepared test");
    }

    static Stream<Arguments> provideStringsForReplacing() {
        return Stream.of(
            Arguments.of(SIMPLE_LF, OUT_SIMPLE_LF),
            Arguments.of(SIMPLE_CR_LF, OUT_SIMPLE_LF),
            Arguments.of(INPUT_LF, OUTPUT_LF),
            Arguments.of(INPUT_CR_LF, OUTPUT_LF));
    }

    @ParameterizedTest
    @MethodSource("provideStringsForReplacing")
    void testReplaceUsingRegularExpressions(String input, String expected) {
        final long start = System.currentTimeMillis();
        for (int i = 0; i < NUM_RUNS; i++) {
            String res = replaceUsingRegex(input);
            assertThat(res).isEqualTo(expected);
        }
        log.info("testReplaceUsingRegularExpressions: {} ms", System.currentTimeMillis() - start);
    }

    @ParameterizedTest
    @MethodSource("provideStringsForReplacing")
    void testReplaceUsingStringBuffer(String input, String expected) {
        final long start = System.currentTimeMillis();
        for (int i = 0; i < NUM_RUNS; i++) {
            String res = ReplaceInStringTest.replaceUsingForwardSearchAndChars(input);
            assertThat(res).isEqualTo(expected);
        }
        log.info("testReplaceUsingStringBuffer: {} ms", System.currentTimeMillis() - start);
    }

    @ParameterizedTest
    @MethodSource("provideStringsForReplacing")
    void testReplaceCrLfWithLf(String input, String expected) {
        final long start = System.currentTimeMillis();
        for (int i = 0; i < NUM_RUNS; i++) {
            String res = ReplaceInStringTest.replaceUsingIndexOfAndStringReplace(input);
            assertThat(res).isEqualTo(expected);
        }
        log.info("testReplaceUsingStringBuffer: {} ms", System.currentTimeMillis() - start);
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
        int i = 0;
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
            while (sb.length() > i) {
                if ("\n".equals(String.valueOf(sb.charAt(i))) ||
                    "\r".equals(String.valueOf(sb.charAt(i)))) {
                    sb.replace(i, i + 1, "\\n");
                }
                i++;
            }
        }
        return sb.toString();
    }
}
