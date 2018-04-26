package metaengine;

/**
 * This class contains static pure functions which aid in the implementation of
 * the UCI protocol. This class should not be instantiated and it maintains no
 * state.
 */
public class UCIUtils {
    public static String[] tokenize(String line) {
        return line.trim().split("\\s+");
    }

    public static int tryParseUnsigned(String str) {
        int result = -1;
        try {
            result = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            // Do nothing
        }
        if (result < 0) {
            result = -1;
        }
        return result;
    }
}
