package metaengine;

import java.util.List;
import java.util.ArrayList;
import java.io.File;

import com.moandjiezana.toml.Toml;

public class Configuration {

    public static class EngineConfiguration {
        private final List<String> argv;
        private final EngineRoles roles;
        private final int bias;
        private final int index;
        private final File dir;

        /*
         * Note: dir can be null if the current working directory is desired.
         * All other arguments must have valid values.
         */
        public EngineConfiguration(File dir, List<String> argv,
                                   List<String> roles, int bias, int index) {
            this.dir = dir;
            this.argv = argv;
            this.roles = new EngineRoles(roles);
            this.bias = bias;
            this.index = index;
        }

        public List<String> getEngineArgv() {
            return argv;
        }

        /**
         * @return The working directory for this engine or {@code null} for
         * the current working directory.
         */
        public File getWorkingDirectory() {
            return dir;
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

    public static Configuration newConfigurationFromString(String input)
      throws InvalidConfigurationException {
        Toml toml = new Toml().read(input);
        List<Toml> engines = toml.getTables("engine");
        int index = 0;
        List<EngineConfiguration> configs = new ArrayList<>();
        boolean hasTimer = false;
        for (Toml engine : engines) {
            List<String> argv = engine.getList("argv");
            if (argv == null) {
                throw new InvalidConfigurationException(
                  "Engine must have \"argv\"");
            }
            List<String> roles = engine.getList("roles");
            if (roles == null) {
                throw new InvalidConfigurationException(
                  "Engine must have \"roles\"");
            }
            if (roles.contains("GENERATOR") && !hasTimer) {
                roles.add("TIMER");
                hasTimer = true;
            }
            String dir = engine.getString("dir");
            File dirFile = null;
            if (dir != null) {
                dirFile = new File(dir);
            }
            Long copiesLong = engine.getLong("copies");
            int copies;
            if (copiesLong == null) {
                copies = 1;
            } else {
                copies = copiesLong.intValue();
            }
            Long biasLong = engine.getLong("bias");
            int bias;
            if (biasLong == null) {
                bias = 0;
            } else {
                bias = biasLong.intValue();
            }
            configs.add(new EngineConfiguration(dirFile, argv, roles,
                                                bias, index));
            ++index;
        }

        return new Configuration(configs);
    }

    private final List<EngineConfiguration> engineConfigurations;

    private Configuration(List<EngineConfiguration> engineConfigs) {
        engineConfigurations = engineConfigs;
    }

    public List<EngineConfiguration> getEngineConfigurations() {
        return engineConfigurations;
    }
}
