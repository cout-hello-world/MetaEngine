package metaengine;

public class UCIPosition {
    // Right now this class is implemented the lazy way. We just store
    // the string as-is. This may change if further functionality is needed.
    public static final UCIPosition STARTPOS = new UCIPosition(new String[] {
        "position", "startpos"
    });
    private final String pos;

    UCIPosition(String[] tokens) {
        String temp = "";
        boolean afterFirstToken = false;
        for (String token : tokens) {
            if (afterFirstToken) {
                temp += " " + token;
            } else {
                temp = token;
            }
            afterFirstToken = true;
        }
        pos = temp;
    }

    UCIPosition plus(UCIMove move) {
        String[] tokens = UCIUtils.tokenize(pos);
        boolean hasMoves = false;
        for (String str : tokens) {
            if (str.equals("moves")) {
                hasMoves = true;
                break;
            }
        }

        if (hasMoves) {
            String[] ctorParam = new String[tokens.length + 1];
            for (int i = 0; i != tokens.length; ++i) {
                ctorParam[i] = tokens[i];
            }
            ctorParam[tokens.length] = move.toString();
            return new UCIPosition(ctorParam);
        } else {
            String[] ctorParam = new String[tokens.length + 2];
            for (int i = 0; i != tokens.length; ++i) {
                ctorParam[i] = tokens[i];
            }
            ctorParam[tokens.length] = "moves";
            ctorParam[tokens.length + 1] = move.toString();
            return new UCIPosition(ctorParam);
        }
    }

    @Override
    public String toString() {
        return pos;
    }
}
