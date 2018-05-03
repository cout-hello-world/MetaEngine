package metaengine;

import java.io.File;
import java.io.IOException;

import java.nio.file.Paths;

import java.util.List;
import java.util.ArrayList;


public class UCIEngine {

    private final Object mutex = new Object();

    private final Process engineProcess;
    private final String engineName;
    private List<UCIOption> options = null;
    private final ProcessIO engineIO;
    private static final boolean DEBUG_IO = true;

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
        engineIO = new ProcessIO(engineProcess, engineName, DEBUG_IO);
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
    private void populateOptions() {
        options = new ArrayList<UCIOption>();
        engineIO.sendLine("uci");
        String line = "";
        while (true) {
            line = engineIO.readLine();
            if (line == null) {
                throw new RuntimeException(NULL_READLINE_MESSAGE);
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

    public void sendOption(UCIOption opt) {
        synchronized (mutex) {
            engineIO.sendLine(opt.getSetoptionString());
        }
    }

    public void sendUcinewgame() {
        synchronized (mutex) {
            engineIO.sendLine("ucinewgame");
        }
    }

    public void synchronize() {
        synchronized (mutex) {
            engineIO.sendLine("isready");
            String response = "";
            while (true) {
                response = engineIO.readLine();
                if (response == null) {
                    throw new RuntimeException(NULL_READLINE_MESSAGE);
                }
                String[] tokens = UCIUtils.tokenize(response);
                if (tokens.length != 0 && tokens[0].equals("readyok")) {
                    break;
                }
            }
        }
    }

    public void position(UCIPosition pos) {
        synchronized (mutex) {
            engineIO.sendLine("position " + pos.toString());
        }
    }

    public void quit() {
        synchronized (mutex) {
            engineIO.sendLine("quit");
        }
    }
}
