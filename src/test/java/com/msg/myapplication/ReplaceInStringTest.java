package com.msg.myapplication;

import group.msg.jpowermonitor.junit.JPowerMonitorExtension;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Pattern;

@ExtendWith({ JPowerMonitorExtension.class })
@Slf4j
public class ReplaceInStringTest {

    private static final String INPUT = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Nulla fermentum fringilla nunc. Pellentesque id ante. Nunc justo. In hac habitasse platea dictumst. Mauris erat libero, tempus quis, hendrerit semper, pharetra quis, augue. Mauris consectetuer molestie diam. Aenean nec tellus in enim feugiat convallis. Nam vitae arcu. Cras eget eros nec wisi aliquet laoreet. Donec lobortis vestibulum odio. Sed luctus. Aenean pellentesque.\r\n"
            +
            "Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Maecenas vitae neque. Aenean ornare mollis sem. In hac habitasse platea dictumst. Suspendisse condimentum suscipit neque. Donec sollicitudin. Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aliquam mauris felis, vehicula at, euismod vel, dictum ac, tellus. Donec metus. Nunc libero orci, accumsan non, pellentesque vel, pellentesque non, felis.\r\n"
            +
            "Aenean nulla. Integer vitae sem ac purus auctor tristique. Suspendisse potenti. Donec lobortis metus a mi. Praesent ante lacus, tempus eu, aliquet semper, adipiscing at, dolor. Donec tincidunt neque a elit. Vivamus suscipit. Nullam quam.\r\n"
            +
            "Nunc velit leo, porttitor nec, tempus sed, tincidunt quis, nunc. Phasellus quam enim, lacinia ut, varius vel, dapibus vitae, nisl. Sed egestas pretium dui. Proin turpis eros, ornare sed, pharetra vitae, imperdiet vitae, metus. Suspendisse volutpat. Fusce accumsan. Donec fermentum pellentesque tortor. Vivamus mi. Nulla facilisi. Morbi non velit non dolor elementum imperdiet. Aenean tincidunt varius elit.\r\n"
            +
            "Maecenas vestibulum orci et purus. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Curabitur congue gravida lectus. Suspendisse adipiscing. Aliquam quis lectus ut velit ornare fringilla.\r\n"
            +
            "Phasellus tempus, justo sit amet sollicitudin tempor, quam libero vehicula leo, sed facilisis erat massa non wisi. Pellentesque at wisi ac lorem lacinia pellentesque. Morbi dui. Mauris scelerisque massa ac purus. Etiam bibendum metus eu diam. Pellentesque purus erat, malesuada vel, volutpat in, tincidunt a, wisi. Sed dui. Quisque semper lectus. Curabitur odio felis, cursus eget, placerat vel, feugiat at, quam. Cras ultricies, lorem vel vestibulum luctus, dui libero aliquet libero, id facilisis mauris dui vel orci.\r\n"
            +
            "Sed interdum. Mauris fermentum quam sed elit. Donec et diam. Phasellus tellus metus, tempor a, eleifend quis, semper id, felis. Cras non sem in lacus interdum blandit.\r\n"
            +
            "Nulla convallis magna nec elit. Nulla lectus lorem, convallis nec, ultrices et, malesuada et, lacus. Sed vestibulum. Donec tristique iaculis libero. Etiam mattis. Pellentesque lectus. Curabitur consequat, sem vitae convallis egestas, ante arcu scelerisque purus, a vestibulum lacus urna ac ipsum. Duis cursus leo vitae dolor.\r\n"
            +
            "Proin iaculis, enim vitae elementum bibendum, odio diam semper risus, vel iaculis dolor purus vel enim. Proin rutrum ante et neque. Cras dolor ante, lobortis et, tempor quis, cursus sed, elit. Duis sagittis sapien sit amet lorem. Aenean nibh. Praesent venenatis, leo non semper sodales, tortor neque dignissim pede, eu molestie ipsum pede eget ipsum. Sed sem.\r\n"
            +
            "Sed suscipit. In ut est. Pellentesque non odio. Suspendisse leo. Sed blandit. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Pellentesque congue ipsum nec wisi.\r\n"
            +
            "Nullam posuere nulla eget odio. Duis eu odio vel orci aliquet convallis. Nullam magna tortor, bibendum a, nonummy non, egestas sed, pede. Aenean lobortis pede id est. Sed vehicula consectetuer enim. Etiam hendrerit quam semper nulla. Curabitur dolor. Pellentesque ac wisi. Cras egestas semper lectus. In id nunc.\r\n"
            +
            "Ut sit amet enim eu elit pulvinar congue. Nullam cursus, enim non venenatis hendrerit, ipsum metus semper nulla, vel iaculis velit sapien in ante. Aliquam gravida, metus ac sollicitudin viverra, risus nibh pellentesque orci, semper or";
    private static final String OUTPUT = ReplaceInStringTest.replaceLineFeedWithString(INPUT);

    private static final int REPS = 1;
    private static final int NUM_RUNS = 1_000_000;

    @RepeatedTest(REPS)
    void originalAlgorithmTest() {
        String res = null;
        final long start = System.currentTimeMillis();
        for (int i = 0; i < NUM_RUNS; i++) {
            res = ReplaceInStringTest.replaceLineFeedWithString(INPUT);
        }
        System.out.println("originalAlgorithmTest: " + (System.currentTimeMillis() - start) + " ms");
        assertThat(res).isEqualTo(OUTPUT);
    }

    /**
     * Replaces "linefeed" and/or "carridge return" with the string "\\n".
     *
     * @param input
     * @return String with "\\n" instead of linefeed.
     */
    public static String replaceLineFeedWithString(final String input) {

        if (input == null) {
            return null;
        }
        int i = 0;
        final StringBuilder sb = new StringBuilder(input);

        int pos = sb.indexOf("\r\n");
        // Case 1: "linefeed" and "carridge return" exist in the string
        if (pos != -1) {
            while (pos != -1) {
                sb.replace(pos, pos + 2, "\\n");
                pos = sb.indexOf("\r\n", pos + 3);
            }
            // Case 2: "linefeed" or "carridge return" exist in the string
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

    private static final String REGEX = "\r\n";

    private static final String REPLACEMENT = "\\\\n";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    @RepeatedTest(REPS)
    void myOptimizedAlgorithmCompiledRegexTest() {
        String res = null;
        final long start = System.currentTimeMillis();
        for (int i = 0; i < 1_000_000; i++) {
            res = PATTERN.matcher(INPUT).replaceAll(REPLACEMENT);
        }
        System.out.println(
                "myOptimizedAlgorithmCompiledRegexTest: " +
                        (System.currentTimeMillis() - start) + " ms");
        assertThat(res).isEqualTo(OUTPUT);
    }
}
