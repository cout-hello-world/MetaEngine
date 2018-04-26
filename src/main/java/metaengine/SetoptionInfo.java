package metaengine;

public class SetoptionInfo {
    private String name = "";
    private String value = "";
    private int idx = -1;

    private static enum State {
        NO_NAME_YET, HAVE_NAME, SLEEPING, IN_NAME_MAIN, IN_VALUE
    }

    public SetoptionInfo(String[] tokens) {
        State st = State.NO_NAME_YET;
        for (int i = 0; i != tokens.length; ++i) {
            switch (st) {
            case NO_NAME_YET:
                if (tokens[i].equals("name")) {
                    st = State.HAVE_NAME;
                }
                break;
            case HAVE_NAME:
                idx = UCIUtils.tryParseUnsigned(tokens[i]);
                if (idx != -1) {
                    st = State.SLEEPING;
                }
                break;
            case SLEEPING:
                st = State.IN_NAME_MAIN;
                break;
            case IN_NAME_MAIN:
                if (tokens[i].equals("value")) {
                    st = State.IN_VALUE;
                } else if (name.equals("")) {
                    name = tokens[i];
                } else {
                    name += " " + tokens[i];
                }
                break;
            case IN_VALUE:
                if (value.equals("")) {
                    value = tokens[i];
                } else {
                    value += " " + tokens[i];
                }
                break;
            }
        }
    }

    public String getNameString() {
        return name;
    }

    public String getValueString() {
        return value;
    }

    public int getEngineIndex() {
        return idx;
    }
}
