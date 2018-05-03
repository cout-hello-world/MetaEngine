package metaengine;

public class UCIPosition {
    // Right now this class is implemented the lazy way. We just store
    // the string as-is. This may change if further functionality is needed.
    private final String pos;

    UCIPosition(String[] tokens) {
        String temp = "";
        boolean afterFirstToken = false;
        for (String token : tokens) {
            if (afterFirstToken) {
                temp += " " + token;
            }
            afterFirstToken = true;
        }
        pos = temp;
    }

    @Override
    public String toString() {
        return pos;
    }
}
