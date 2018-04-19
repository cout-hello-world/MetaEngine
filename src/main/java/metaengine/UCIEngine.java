package metaengine;

import java.io.File;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;


public class UCIEngine {
    private final Process engineProcess;
    private final BufferedReader fromEngine;
    private final PrintWriter toEngine;
    private final String enginePath;

    public UCIEngine (File pathToEngine, List<String> arguments)
            throws IOException {
        ArrayList<String> argv = new ArrayList<String>();
        enginePath = pathToEngine.getAbsolutePath();
        argv.add(enginePath);
        for (String str : arguments) {
            argv.add(str);
        }

        ProcessBuilder pb = new ProcessBuilder(argv);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        engineProcess = pb.start();

        fromEngine = new BufferedReader(new InputStreamReader(
                       engineProcess.getInputStream()));
        toEngine =
            new PrintWriter(new BufferedWriter(
              new OutputStreamWriter(engineProcess.getOutputStream())), true);
    }

    public UCIEngine(File pathToEngine) throws IOException {
        this(pathToEngine, new ArrayList<String>());
    }

    // FIXME
    List<UCIOption> getOptions() throws IOException {
        List<UCIOption> options = new ArrayList<UCIOption>();
        toEngine.println("uci");
        String line = "";
        while (!line.equals("uciok")) {
            line = fromEngine.readLine();
            if (line == null) {
                // Consider using custom exception class.
                throw new IOException("Early EOF in engine output");
            }
            String[] tokens = UCIUtils.tokenize(line);
            List<String> tokensList = new ArrayList<String>();
            if (tokens.length != 0 && tokens[0].equals("option")) {
                for (int i = 1; i != tokens.length; ++i) {
                    tokensList.add(tokens[i]);
                }
                options.add(new UCIOption(tokensList));
            }
        }

        return options;
    }
}
