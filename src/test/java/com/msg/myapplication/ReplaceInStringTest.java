package com.msg.myapplication;

import group.msg.jpowermonitor.junit.JPowerMonitorExtension;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
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
    private static String INPUT_CR_LF;
    private static String INPUT_LF;
    private static String OUTPUT_LF; //
    private static String OUTPUT_LF2; //
    private static final int NUM_RUNS = 300_000;

    // A carriage return means moving the cursor to the beginning of the line. The code is \r.
    // A line feed means moving one line forward. The code is \n.
    private static final Pattern PATTERN = Pattern.compile("\r\n");

    @BeforeAll
    static void prepare() throws IOException {
        byte[] contentLf = Files.readAllBytes(Paths.get("src/test/resources/replace-string-test/content-lf.txt"));
        byte[] contentCrLf = Files.readAllBytes(Paths.get("src/test/resources/replace-string-test/content-cr-lf.txt"));
        INPUT_LF = new String(contentLf, StandardCharsets.UTF_8);
        INPUT_CR_LF = new String(contentCrLf, StandardCharsets.UTF_8);
        log.info("Input with LF has length: {} and with CR LF: {}", INPUT_LF.length(), INPUT_CR_LF.length());
        //Assertions.assertEquals(INPUT_CR_LF, INPUT_LF);
        OUTPUT_LF = ReplaceInStringTest.replaceUsingRegex(INPUT_LF);
        OUTPUT_LF2 = ReplaceInStringTest.replaceUsingRegex(INPUT_CR_LF);
        Assertions.assertEquals(OUTPUT_LF, OUTPUT_LF2); // works, LF and CRLF is removed.
        OUTPUT_LF = replaceCarriageReturn(INPUT_LF);
        OUTPUT_LF2 = replaceCarriageReturn(INPUT_CR_LF);
        Assertions.assertEquals(OUTPUT_LF, OUTPUT_LF2);
        log.info("Prepared test");
    }

    static Stream<Arguments> provideStringsForReplacing() {
        return Stream.of(
            Arguments.of(INPUT_LF, OUTPUT_LF),
            Arguments.of(INPUT_CR_LF, OUTPUT_LF2)
        );
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
            String res = ReplaceInStringTest.replaceCarriageReturn(input);
            assertThat(res).isEqualTo(expected);
        }
        log.info("testReplaceUsingStringBuffer: {} ms", System.currentTimeMillis() - start);
    }

    @ParameterizedTest
    @MethodSource("provideStringsForReplacing")
    @Disabled
    void testReplaceCrLfWithLf(String input, String expected) {
        final long start = System.currentTimeMillis();
        for (int i = 0; i < NUM_RUNS; i++) {
            String res = ReplaceInStringTest.replaceCrLfWithLf(input);
            assertThat(res).isEqualTo(expected);
        }
        log.info("testReplaceUsingStringBuffer: {} ms", System.currentTimeMillis() - start);
    }

    /**
     * Replaces "linefeed" and/or "carriage return" with the string "\n" (linefeed).
     *
     * @param input the input that contains carriage return/linefeed.
     * @return String with "\n" instead of carriage return/linefeed.
     */
    private static String replaceUsingRegex(String input) {
        return PATTERN.matcher(input).replaceAll("\n");
    }

    public static String replaceCarriageReturn(String input) {
        char[] chars = input.toCharArray();
        StringBuilder result = new StringBuilder();
        // replace \r\n with \n
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '\r' && i + 1 < chars.length && chars[i + 1] == '\n') {
                // If we find \r followed by \n, append \n and skip the next character
                result.append('\n');
                i++; // Skip the '\n'
            } else {
                result.append(chars[i]); // Otherwise, just append the current character
            }
        }
        return result.toString();
    }

    /**
     * Replaces "linefeed" and/or "carriage return" with the string "\\n".
     *
     * @param input the input that contains carriage return/linefeed.
     * @return String with "\\n" instead of carriage return/linefeed.
     */
    public static String replaceCrLfWithLf(final String input) {
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
                if ("\\n".equals(String.valueOf(sb.charAt(i))) ||
                    "\r".equals(String.valueOf(sb.charAt(i)))) {
                    sb.replace(i, i + 1, "\\n");
                }
                i++;
            }
        }
        return sb.toString();
    }
}
