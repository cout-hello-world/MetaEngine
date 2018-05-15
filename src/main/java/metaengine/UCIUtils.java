package metaengine;

/**
 * This class contains static pure functions which aid in the implementation of
 * the UCI protocol. This class should not be instantiated and it maintains no
 * state.
 */
public class UCIUtils {
    private UCIUtils() { } // enforce non-instantiation
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

    /**
     * This function returns the index of {@code str} in {@code arr}.
     *
     * If {@code str} cannot be found, {@code -1} is returned.
     * @param arr The array to look in
     * @param str The string to find
     * @return The index of {@code str} in {@code arr} (or {@code -1})
     */
    public static int findIndex(String[] arr, String str) {
        for (int i = 0; i != arr.length; ++i) {
            if (arr[i].equals(str)) {
                return i;
            }
        }
        return -1;
    }
}
