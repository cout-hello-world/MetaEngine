package metaengine;

import java.util.List;
import java.util.ArrayList;

import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public class Configuration {

    public static class EngineConfiguration {
        private final List<String> argv;
        private final EngineRoles roles;
        private final int bias;
        private final int index;
        public EngineConfiguration(List<String> argv, List<String> roles,
                                   int bias, int index) {
            this.argv = argv;
            this.roles = new EngineRoles(roles);
            this.bias = bias;
            this.index = index;
        }

        public List<String> getEngineArgv() {
            return argv;
        }

        public EngineRoles getEngineRoles() {
            return roles;
        }

        public int getIndex() {
            return index;
        }

        public int getBias() {
            return bias;
        }
    }

    private enum RemoveCommentsState {
        NORMAL, IN_STRING, SAW_STRING_ESCAPE, SAW_SLASH, IN_C_COMMENT,
        SAW_STAR, IN_CPP_COMMENT
    }

    public static String removeComments(String str) {
        StringBuilder builder = new StringBuilder(str.length());
        RemoveCommentsState state = RemoveCommentsState.NORMAL;
        for (int i = 0; i != str.length(); ++i) {
            char ch = str.charAt(i);
            switch (state) {
            case NORMAL:
                if (ch == '/') {
                    state = RemoveCommentsState.SAW_SLASH;
                } else {
                    builder.append(ch);
                }
                if (ch == '"') {
                    state = RemoveCommentsState.IN_STRING;
                }
                break;
            case IN_STRING:
                if (ch == '"') {
                    state = RemoveCommentsState.NORMAL;
                } else if (ch == '\\') {
                    state = RemoveCommentsState.SAW_STRING_ESCAPE;
                }
                builder.append(ch);
                break;
            case SAW_STRING_ESCAPE:
                state = RemoveCommentsState.IN_STRING;
                builder.append(ch);
                break;
            case SAW_SLASH:
                if (ch == '/') {
                    state = RemoveCommentsState.IN_CPP_COMMENT;
                } else if (ch == '*') {
                    state = RemoveCommentsState.IN_C_COMMENT;
                } else {
                    state = RemoveCommentsState.NORMAL;
                    builder.append('/');
                    builder.append(ch);
                }
                break;
            case IN_C_COMMENT:
                if (ch == '*') {
                    state = RemoveCommentsState.SAW_STAR;
                }
                break;
            case SAW_STAR:
                if (ch == '/') {
                    state = RemoveCommentsState.NORMAL;
                }
                break;
            case IN_CPP_COMMENT:
                if (ch == '\n') {
                    state = RemoveCommentsState.NORMAL;
                }
                break;
            }
        }
        if (state == RemoveCommentsState.SAW_SLASH) {
            builder.append('/');
        }

        return builder.toString();
    }

    public static Configuration newConfigurationFromString(String input)
      throws InvalidConfigurationException {
        String json = removeComments(input);

        int index = 0;
        List<EngineConfiguration> engineConfigurationList
          = new ArrayList<EngineConfiguration>();
        JsonParser parser = new JsonParser();
        JsonElement rootElement = parser.parse(json);
        if (!rootElement.isJsonObject()) {
            throw new InvalidConfigurationException(
              "Root JSON element must be an object");
        }
        JsonObject rootObject = rootElement.getAsJsonObject();
        if (!rootObject.has("engines")) {
            throw new InvalidConfigurationException(
              "Root object must have the key \"engines\"");
        }
        JsonElement enginesElement = rootObject.get("engines");
        if (!enginesElement.isJsonArray()) {
            throw new InvalidConfigurationException(
              "Value of \"engines\" key must be an array");
        }
        JsonArray enginesArray = enginesElement.getAsJsonArray();
        for (JsonElement engineElem : enginesArray) {
            if (!engineElem.isJsonObject()) {
                throw new InvalidConfigurationException(
                  "Elements of \"engines\" array must be objects");
            }
            JsonObject engineObject = engineElem.getAsJsonObject();
            if (!engineObject.has("argv")) {
                throw new InvalidConfigurationException(
                  "engine object missing required element \"argv\"");
            }
            JsonElement argvElement = engineObject.get("argv");
            if (!argvElement.isJsonArray()) {
                throw new InvalidConfigurationException(
                  "\"argv\" key must must have array value");
            }
            JsonArray argvArray = argvElement.getAsJsonArray();
            List<String> argvStrings = new ArrayList<String>();
            for (JsonElement argvArg : argvArray) {
                if (!argvArg.isJsonPrimitive()) {
                    throw new InvalidConfigurationException(
                      "Elements of \"argv\" array must be primitives");
                }
                argvStrings.add(argvArg.getAsString());
            }

            if (!engineObject.has("roles")) {
                throw new InvalidConfigurationException(
                  "Engine object must have \"roles\" key");
            }
            JsonElement rolesElement = engineObject.get("roles");
            if (!rolesElement.isJsonArray()) {
                throw new InvalidConfigurationException(
                  "\"roles\" key must have value of type array");
            }
            JsonArray rolesArray = rolesElement.getAsJsonArray();
            List<String> roleStrings = new ArrayList<String>();
            for (JsonElement role : rolesArray) {
                if (!role.isJsonPrimitive()) {
                    throw new InvalidConfigurationException(
                      "element of \"roles\" array must be primitive");
                }
                roleStrings.add(role.getAsString());
            }

            int copies = 1;
            if (engineObject.has("copies")) {
                copies = engineObject.get("copies").getAsInt();
            }
            int bias = 0;
            if (engineObject.has("bias")) {
                bias = engineObject.get("bias").getAsInt();
            }

            for (int i = 0; i != copies; ++i) {
                engineConfigurationList.add(new EngineConfiguration(
                  argvStrings, roleStrings, bias, index));
                ++index;
            }
        }

        return new Configuration(engineConfigurationList);
    }

    private final List<EngineConfiguration> engineConfigurations;

    private Configuration(List<EngineConfiguration> engineConfigs) {
        engineConfigurations = engineConfigs;
    }

    public List<EngineConfiguration> getEngineConfigurations() {
        return engineConfigurations;
    }
}
