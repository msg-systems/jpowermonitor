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
    private static String INPUT_CR_LF;
    private static String INPUT_LF;
    private static String OUTPUT_LF; //
    private static String OUTPUT_LF2; //
    private static final int REPETIONS = 1;
    private static final int NUM_RUNS = 1_000_000;

    private static final String REGEX = "[\r]*\n";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    private static final String REPLACEMENT = "\\\\n";

    @BeforeAll
    static void prepare() throws IOException {
        byte[] contentLf = Files.readAllBytes(Paths.get("src/test/resources/replace-string-test/content-lf.txt"));
        byte[] contentCrLf = Files.readAllBytes(Paths.get("src/test/resources/replace-string-test/content-cr-lf.txt"));
        INPUT_LF = new String(contentLf, StandardCharsets.UTF_8);
        INPUT_CR_LF = new String(contentCrLf, StandardCharsets.UTF_8);
        // Assertions.assertEquals(INPUT_CR_LF, INPUT_LF);
        OUTPUT_LF = ReplaceInStringTest.replaceUsingRegex(INPUT_LF);
        OUTPUT_LF2 = ReplaceInStringTest.replaceUsingRegex(INPUT_CR_LF);
        Assertions.assertEquals(OUTPUT_LF, OUTPUT_LF2);

        //  OUTPUT_LF = ReplaceInStringTest.replaceLineFeedWithString(INPUT_LF);
        //  OUTPUT_LF2 = ReplaceInStringTest.replaceLineFeedWithString(INPUT_CR_LF);
        //  Assertions.assertEquals(OUTPUT_LF, OUTPUT_LF2); ==> DOES NOT WORK ==> replaceLineFeedWithString does not produce same output!!!

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
            String res = ReplaceInStringTest.replaceLineFeedWithString(input);
            assertThat(res).isEqualTo(expected);
        }
        log.info("testReplaceUsingStringBuffer: {} ms", System.currentTimeMillis() - start);
    }

    /**
     * Replaces "linefeed" and/or "carriage return" with the string "\\n".
     *
     * @param input the input that contains carriage return/linefeed.
     * @return String with "\\n" instead of carriage return/linefeed.
     */
    private static String replaceUsingRegex(String input) {
        return PATTERN.matcher(input).replaceAll(REPLACEMENT);
    }

    /**
     * Replaces "linefeed" and/or "carriage return" with the string "\\n".
     *
     * @param input the input that contains carriage return/linefeed.
     * @return String with "\\n" instead of carriage return/linefeed.
     */
    public static String replaceLineFeedWithString(final String input) {
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
