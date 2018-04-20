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

    private static final Value VAL_NONE = new Value();

    private String name = "";
    private Type type = Type.NONE;
    private Value minVal = Value.VAL_NONE;
    private Value maxVal = Value.VAL_NONE;
    private Value defaultVal = Value.VAL_NONE;
    private List<Value> vars = new ArrayList<Value>();
    private Value currentVal = Value.VAL_NONE;

    private static enum State {
        NONE, NAME, TYPE, DEFAULT, MIN, MAX, VAR
    }

    public static class Value {
        public static final Value VAL_NONE = new Value();
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
         * If the first argument is {@code Value.VAL_NONE},
         * this consructor creates a value {@code UCIOption.Value} object
         * holding the type indicated by {@code type} with an appropriate
         * conversion of {@code rep}.
         * If the first argument is not {@code Value.VAL_NONE},
         * this constructor creates
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
                if (!old.equals(VAL_NONE) && old.type == Type.STRING) {
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

        @Override
        public boolean equals(Object other) {
            if (other == null) {
                return false;
            }
            if (!Value.class.isAssignableFrom(other.getClass())) {
                return false;
            }

            Value theOther = (Value)other;

            if (type == theOther.type) {
                int nullCount = 0;
                if (val == null) {
                    ++nullCount;
                }
                if (theOther.val == null) {
                    ++nullCount;
                }
                switch (nullCount) {
                case 0:
                    return val.equals(theOther.val);
                case 1:
                    return false;
                default:
                    return true;
                }
            } else {
                return false;
            }
        }

        /**
         * This constructor behaves as if the {@code (Value, String, Type)}
         * constructor were called with a first argument {@code Value.VAL_NONE}.
         */
        public Value(String rep, Type type) {
            this(VAL_NONE, rep, type);
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

        @Override
        public String toString() {
            if (type == Type.NONE) {
                return "";
            } else {
                return val.toString();
            }
        }
    }

    public UCIOption(List<String> optionTokens) {
        State st = State.NONE;
        Value.Type valueType = Value.Type.NONE;
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

        currentVal = defaultVal;
    }

    public String getName() {
        return name;
    }

    public Value getValue() {
        return currentVal;
    }

    public Value getMinValue() {
        return minVal;
    }

    public Value getMaxValue() {
        return maxVal;
    }

    public List<Value> getVarValues() {
        return vars;
    }

    public Value getDefaultValue() {
        return defaultVal;
    }

    public void setValue(Value val) {
        currentVal = val;
    }

    public String getOptionString(String namePrefix) {
        String retVal = "option name " + namePrefix + name;
        String typeStr = type.name().toLowerCase();
        retVal += " type " + typeStr;
        if (!getDefaultValue().equals(Value.VAL_NONE)) {
            retVal += " min " + getMinValue().toString();
        }
        if (!getMinValue().equals(Value.VAL_NONE)) {
            retVal += " min " + getMinValue().toString();
        }
        if (!getMaxValue().equals(Value.VAL_NONE)) {
            retVal += " max " + getMaxValue().toString();
        }
        for (Value v : vars) {
            retVal += " var " + v.toString();
        }

        return retVal;
    }

    public String getOptionString() {
        return getOptionString("");
    }

    public String getSetoptionString() {
        Value theVal = getValue();
        if (theVal.getType() != Value.Type.NONE) {
            return "setoption name " + name + " value " + theVal.toString();
        } else {
            return "setoption name " + name;
        }
    }
}
