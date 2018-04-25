package metaengine;

import java.io.File;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.nio.file.Paths;

import java.util.List;
import java.util.ArrayList;


public class UCIEngine {
    private final Process engineProcess;
    private final BufferedReader fromEngine;
    private final PrintWriter toEngine;
    private final String engineName;
    private List<UCIOption> options = null;

    private static final String NULL_READLINE_MESSAGE =
        "Unexpected EOF when reading from engine";

    // All constructors should ultimatly delagate to this one.
    public UCIEngine (List<String> argv)
            throws IOException {
        // It is okay to use argv without copying because the ProcessBuilder
        // is not maintained after this call.
        engineName = Paths.get(argv.get(0)).getFileName().toString()
                     .replaceAll("\\s", "");
        ProcessBuilder pb = new ProcessBuilder(argv);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        engineProcess = pb.start();

        fromEngine = new BufferedReader(new InputStreamReader(
                       engineProcess.getInputStream()));
        toEngine =
            new PrintWriter(new BufferedWriter(
              new OutputStreamWriter(engineProcess.getOutputStream())), true);
        populateOptions();
    }

    private static List<String> fileToArgv(File file) {
        List<String> argv = new ArrayList<String>();
        argv.add(file.getAbsolutePath());
        return argv;
    }

    public UCIEngine(File pathToEngine) throws IOException {
        this(fileToArgv(pathToEngine));
    }

    private static List<String> stringsToList(String engine, String... args) {
        List<String> argv = new ArrayList<String>();
        argv.add(engine);
        for (String str : args) {
            argv.add(str);
        }
        return argv;
    }

    /**
     * @return A whitespace-free string representing the name of this engine
     */
    public String getName() {
        return engineName;
    }

    public UCIEngine(String engine, String... args) throws IOException {
        this(stringsToList(engine, args));
    }

    public List<UCIOption> getOptions() {
        return options;
    }

    // This should only be called once at the end of the constructor.
    private void populateOptions() throws IOException {
        options = new ArrayList<UCIOption>();
        toEngine.println("uci");
        String line = "";
        while (true) {
            line = fromEngine.readLine();
            if (line == null) {
                throw new IOException(NULL_READLINE_MESSAGE);
            }
            String[] tokens = UCIUtils.tokenize(line);
            if (tokens.length != 0 && tokens[0].equals("uciok")) {
                break;
            }
            List<String> tokensList = new ArrayList<String>();
            if (tokens.length != 0 && tokens[0].equals("option")) {
                for (int i = 1; i != tokens.length; ++i) {
                    tokensList.add(tokens[i]);
                }
                options.add(new UCIOption(tokensList));
            }
        }
    }

    public void sendOption(UCIOption opt) throws IOException {
        toEngine.println(opt.getSetoptionString());
    }

    public void sendUcinewgame() {
        toEngine.println("ucinewgame");
    }

    public void synchronize() throws IOException {
        toEngine.println("isready");
        String response = "";
        while (true) {
            response = fromEngine.readLine();
            if (response == null) {
                throw new IOException(NULL_READLINE_MESSAGE);
            }
            String[] tokens = UCIUtils.tokenize(response);
            if (tokens.length != 0 && tokens[0].equals("readyok")) {
                break;
            }
        }
    }

    public void quit() throws IOException {
        toEngine.println("quit");
    }
}
