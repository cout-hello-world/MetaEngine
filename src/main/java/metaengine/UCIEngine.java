package metaengine;

import java.io.File;
import java.io.IOException;

import java.nio.file.Paths;

import java.util.List;
import java.util.ArrayList;


public class UCIEngine {
    private static final boolean DEBUG_IO = true;

    private final Process engineProcess;
    private final String engineName;
    private List<UCIOption> options = null;
    private final ProcessIO engineIO;
    private final Score score = new Score();

    private final static class Score {
        int score = 0;
        boolean hasScore = false;
        Object lock = new Object();

        void setScore(int score) {
            synchronized (lock) {
                this.score = score;
                hasScore = true;
            }
        }

        /**
         * This function returns an Integer representing the score held by this
         * object or null if there is no score.
         */
        Integer getScore() {
            synchronized (lock) {
                if (hasScore) {
                    return score;
                } else {
                    return null;
                }
            }
        }
    }


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
        engineIO.sendLine(opt.getSetoptionString());
    }

    public void sendUcinewgame() {
        engineIO.sendLine("ucinewgame");
    }

    // This function stops the search as soon as possible and returns the most
    // recent evaluation.
    public int stop() {
        return 0;
    }

    public UCIMove go(UCIGo searchParams) {
        engineIO.sendLine(searchParams.toString());
        while (true) {
            String response = engineIO.readLine();
            if (response == null) {
                throw new RuntimeException(NULL_READLINE_MESSAGE);
            }
            String[] tokens = UCIUtils.tokenize(response);
            if (tokens.length != 0) {
                if (tokens[0].equals("bestmove")) {
                    return new UCIMove(tokens[1]);
                } else if (tokens.equals("info")) {

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
        engineIO.sendLine("position " + pos.toString());
    }

    public void quit() {
        engineIO.sendLine("quit");
    }
}
