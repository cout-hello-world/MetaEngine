package metaengine;

import java.util.List;
import java.util.ArrayList;

import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public class Configuration {

    public static class EngineConfiguration {
        private List<String> argv = new ArrayList<String>();
        private List<String> roles;
        private int bias;
        private int index;
        public EngineConfiguration(List<String> argv, List<String> roles, int bias, int index) {
            this.argv = argv;
            this.roles = roles;
            this.bias = bias;
            this.index = index;
        }

        public List<String> getEngineArgv() {
            return argv;
        }

        public EngineRoles getEngineRoles() {
            return new EngineRoles(roles);
        }

        public int getIndex() {
            return index;
        }

        public int getBias() {
            return bias;
        }
    }

    public static Configuration newConfigurationFromString(String json)
      throws InvalidConfigurationException {
        JsonParser parser = new JsonParser();
        JsonElement rootElement = parser.parse(json);
        if (!rootElement.isJsonObject()) {
            throw new InvalidConfigurationException("Root JSON element must be an object");
        }
        JsonObject rootObject = rootElement.getAsJsonObject();
        if (!rootObject.has("engines")) {
            throw new InvalidConfigurationException("Root object must have the key \"engines\"");
        }
        JsonElement enginesElement = rootObject.get("engines");
        if (!enginesElement.isJsonArray()) {
            throw new InvalidConfigurationException("Value of \"engines\" key must be an array");
        }
        JsonArray enginesArray = enginesElement.getAsJsonArray();
        for (JsonElement engineElem : enginesArray) {
            if (!engineElem.isJsonObject()) {
                throw new InvalidConfigurationException("Elements of \"engines\" array must be objects");
            }
            JsonObject engineObject = engineElem.getAsJsonObject();
            if (!engineObject.has("argv")) {
                throw new InvalidConfigurationException("engine object missing required element \"argv\"");
            }
            JsonElement argvElement = engineObject.get("argv");
            if (!argvElement.isJsonArray()) {
                throw new InvalidConfigurationException("\"argv\" key must must have array value");
            }
            JsonArray argvArray = argvElement.getAsJsonArray();
            List<String> argvStrings = new ArrayList<String>();
            for (JsonElement argvArg : argvArray) {
                if (!argvArg.isJsonPrimative()) {
                    throw new InvalidConfigurationException("Elements of \"argv\" array must be primitives");
                }
                argvString.add(argvAry.toString());
            }

            if (!engineObject.has("roles"))
        }
        // Temporary
        return new Configuration(new ArrayList<EngineConfiguration>());
    }

    private final List<EngineConfiguration> engineConfigurations;

    private Configuration(List<EngineConfiguration> engineConfigs) {
        engineConfigurations = engineConfigs;
    }

    public List<EngineConfiguration> getEngineConfigurations() {
        return engineConfigurations;
    }
}
