package metaengine;

import java.util.List;
import java.util.ArrayList;

public class UCIOption {
    public static enum Type {
        NONE,
        CHECK,
        SPIN,
        COMBO,
        BUTTON,
        STRING
    }


    private String name = "";
    private Type type = Type.NONE;
    private Value minVal = null;
    private Value maxVal = null;
    private Value defaultVal = null;
    private List<Value> vars = new ArrayList<Value>();
    private Value.Type valueType = Value.Type.NONE;

    private static enum State {
        NONE, NAME, TYPE, DEFAULT, MIN, MAX, VAR
    }

    public static class Value {
        public static enum Type {
            BOOLEAN,
            STRING,
            INT,
            NONE
        }
        private Type type = Type.NONE;
        private Object val = null;

        /**
         * This default constructor constructs a {@code UCIOption.Value} with
         * {@code Type} {@code NONE}.
         */
        public Value () { }
        /**
         * If the first argument is {@code null},
         * this consructor creates a value {@code UCIOption.Value} object
         * holding the type indicated by {@code type} with an appropriate
         * conversion of {@code rep}.
         * If the first argument is not null, this constructor creates
         * a value that combines the value in old with the value indicated
         * by the other arguments.
         */
        public Value(Value old, String rep, Type type) {
            this.type = type;
            switch (type) {
            case BOOLEAN:
                val = new Boolean(rep);
                break;
            case STRING:
                if (old != null && old.type == Type.STRING) {
                    val = (String)old.val + " " + rep;
                } else {
                    val = rep;
                }
                break;
            case INT:
                val = new Integer(rep);
                break;
            }
        }
        /**
         * This constructor behaves as if the {@code (Value, String, Type)}
         * constructor were called with a first argument {@code null}.
         */
        public Value(String rep, Type type) {
            this(null, rep, type);
        }
        public Value(String str) {
            type = Type.STRING;
            val = str;
        }
        public Value(int integer) {
            type = Type.INT;
            val = new Integer(integer);
        }
        public Value(boolean bool) {
            type = Type.BOOLEAN;
            val = new Boolean(bool);
        }
        public Type getType() {
            return type;
        }
        public int asInt() {
            return (Integer)val;
        }
        public String asString() {
            return (String)val;
        }
        public boolean asBoolean() {
            return (Boolean)val;
        }
    }

    public UCIOption(List<String> optionTokens) {
        State st = State.NONE;
        int varIndex = 0;
        for (String token : optionTokens) {
            switch (token) {
            case "name":
                st = State.NAME;
                continue;
            case "type":
                st = State.TYPE;
                continue;
            case "default":
                st = State.DEFAULT;
                continue;
            case "min":
                st = State.MIN;
                continue;
            case "max":
                st = State.MAX;
                continue;
            case "var":
                st = State.VAR;
                ++varIndex;
                continue;
            }

            switch (st) {
            case NAME:
                if (name.equals("")) {
                    name = token;
                } else {
                    name = name + " " + token;
                }
                break;
            case TYPE:
                switch (token) {
                case "check":
                    type = Type.CHECK;
                    break;
                case "spin":
                    type = Type.SPIN;
                    break;
                case "combo":
                    type = Type.COMBO;
                    break;
                case "button":
                    type = Type.BUTTON;
                    break;
                case "string":
                    type = Type.STRING;
                    break;
                }
                break;
            case DEFAULT:
                defaultVal = new Value(defaultVal, token, valueType);
                break;
            case MIN:
                minVal = new Value(minVal, token, valueType);
                break;
            case MAX:
                maxVal = new Value(minVal, token, valueType);
                break;
            case VAR:
                if (varIndex > vars.size()) {
                    vars.add(new Value(token, valueType));
                } else {
                    // lastIdx will never be negative.
                    int lastIdx = vars.size() - 1;
                    vars.set(lastIdx,
                             new Value(vars.get(lastIdx), token, valueType));
                }
                break;
            }

            switch (type) {
            case CHECK:
                valueType = Value.Type.BOOLEAN;
                break;
            case SPIN:
                valueType = Value.Type.INT;
                break;
            case STRING:
            case COMBO:
                valueType = Value.Type.STRING;
                break;
            // case BUTTON: valueType is already NONE;
            }
        }
    }
}
