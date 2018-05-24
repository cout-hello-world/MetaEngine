package metaengine;

import java.io.File;
import java.io.IOException;

import java.nio.file.Paths;

import java.util.List;
import java.util.ArrayList;


public class UCIEngine {
    private final Process engineProcess;
    private final String engineName;
    private final int engineIndex;
    private List<UCIOption> options = null;
    private final ProcessIO engineIO;

    private static final String NULL_READLINE_MESSAGE =
        "Unexpected EOF when reading from engine";

    // All constructors should ultimatly delagate to this one.
    public UCIEngine (List<String> argv, int index)
            throws IOException {
        // It is okay to use argv without copying because the ProcessBuilder
        // is not maintained after this call.
        engineName = Paths.get(argv.get(0)).getFileName().toString()
                     .replaceAll("\\s", "");
        engineIndex = index;
        ProcessBuilder pb = new ProcessBuilder(argv);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        engineProcess = pb.start();
        engineIO = new ProcessIO(engineProcess, engineIndex + " " + engineName);
        populateOptions();
    }

    private static List<String> fileToArgv(File file) {
        List<String> argv = new ArrayList<String>();
        argv.add(file.getAbsolutePath());
        return argv;
    }

    public UCIEngine(File pathToEngine, int index) throws IOException {
        this(fileToArgv(pathToEngine), index);
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

    public UCIEngine(int index, String engine, String... args) throws IOException {
        this(stringsToList(engine, args), index);
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
        engineIO.sendLine(opt.getSetoptionString());
    }

    public void sendUcinewgame() {
        engineIO.sendLine("ucinewgame");
    }

    // This function stops the search as soon as possible.
    public void stop() {
        engineIO.sendLine("stop");
    }

    public GoResult go(UCIGo searchParams) {
        engineIO.sendLine(searchParams.toString());
        GoResult.Score lastScore = new GoResult.Score(Integer.MIN_VALUE, true);
        while (true) {
            String response = engineIO.readLine();
            if (response == null) {
                throw new RuntimeException(NULL_READLINE_MESSAGE);
            }
            String[] tokens = UCIUtils.tokenize(response);
            if (tokens.length != 0) {
                if (tokens[0].equals("bestmove")) {
                    // Consider handling error case (no tokens[1])
                    return new GoResult(lastScore, new UCIMove(tokens[1]));
                } else if (tokens[0].equals("info")) {
                    boolean mate = false;
                    int idx = UCIUtils.findIndex(tokens, "cp");
                    if (idx == -1) {
                        idx = UCIUtils.findIndex(tokens, "mate");
                        mate = true;
                    }
                    if (idx != -1 && idx + 1 < tokens.length) {
                        int value = Integer.parseInt(tokens[idx + 1]);
                        boolean replace = true;
                        if (idx + 2 < tokens.length) {
                            if (tokens[idx + 2].equals("lowerbound") ||
                                tokens[idx + 2].equals("upperbound")) {
                                replace = false;
                            }
                        }
                        if (replace) {
                            lastScore = new GoResult.Score(value, mate);
                        }
                    }
                }
            }
        }
    }

    public void synchronize() {
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

    public void position(UCIPosition pos) {
        engineIO.sendLine(pos.toString());
    }

    public void quit() {
        engineIO.sendLine("quit");
    }
}
