package metaengine;

import java.util.List;
import java.util.ArrayList;

import com.google.gson.JsonParser;
import com.google.gson.JsonElement;

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

    public static Configuration newConfigurationFromString(String json) {
        JsonParser parser = new JsonParser();
        JsonElement root = parser.parse(json);
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
