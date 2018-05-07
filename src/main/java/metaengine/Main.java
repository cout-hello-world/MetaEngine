package metaengine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

public class Main {

    private static String readUTF8FileAsString(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void main(String[] args)
      // TODO: Real exception handling
      throws IOException, InvalidConfigurationException {
        if (args.length != 1) {
            printHelp(System.err);
            System.exit(1);
            return;
        }
        switch (args[0]) {
        case "-h":
        case "--help":
            printHelp(System.out);
            return;
        }

        String configFileContents = readUTF8FileAsString(args[0]);

        Configuration configuration =
          Configuration.newConfigurationFromString(configFileContents);
        UCIEnginesManager engines = UCIEnginesManager.create(configuration);

        System.exit(new UCI(engines).loop());
    }

    private static void printHelp(PrintStream out) {
        out.println("Usage: java -jar <MetaEngine_jar> <json_config_file>");
    }
}
