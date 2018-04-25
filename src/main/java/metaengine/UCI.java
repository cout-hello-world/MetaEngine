package metaengine;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintStream;

import java.util.List;

public class UCI {
    private final UCIEnginesManager engines;
    private final List<UCIOptionBundle> optionBundles;
    public UCI(UCIEnginesManager engines) {
        this.engines = engines;
        optionBundles = this.engines.getUCIOptions();
    }

    private static enum State {
        BEFORE_UCI, AFTER_UCI, INITIALIZED
    }

    private void printEngineOptions(PrintStream out) {
        for (UCIOptionBundle bundle : optionBundles) {
            String name = bundle.getName();
            int index = bundle.getIndex();
            List<UCIOption> options = bundle.getOptions();
            for (UCIOption option : options) {
                out.println(option.getOptionString(index + " " + name + " "));
            }
        }
    }

    /**
     * This function executes a main UCI loop. Its return value is
     * suitable as a return code for a calling process.
     */
    public int loop() {
        boolean loop = true;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintStream out = System.out;
        State state = State.BEFORE_UCI;
        while (loop) {
            String line;
            try {
                line = in.readLine();
            } catch (IOException e) {
                System.err.println("Error reading from stdin");
                return 1;
            }
            String[] tokens = UCIUtils.tokenize(line);
            if (tokens.length == 0) {
                continue;
            }

            String cmd = tokens[0];

            switch (state) {
            case BEFORE_UCI:
                if (cmd.equals("uci")) {
                    printEngineOptions(out);
                    out.println("uciok");
                    state = State.AFTER_UCI;
                }
                break;
            case AFTER_UCI:
                if (cmd.equals("quit")) {
                    loop = false;
                } else if (cmd.equals("setoption")) {
                    // Assert length?
                    try {
                        engines.dispatchOption(new SetoptionInfo(tokens));
                    } catch (IOException e) {
                        System.err.println("Error carrying out setoption command");
                    }
                } else if (cmd.equals("isready")) {
                    try {
                        engines.synchronizeAll();
                    } catch (IOException e) {
                        System.err.println("Error initializing engines");
                        // Consider failure exit here
                    }
                    out.println("readyok");
                    state = State.INITIALIZED;
                }
                break;
            case INITIALIZED:
                if (cmd.equals("quit")) {
                    loop = false;
                }
                break;
            }
        }

        try {
            engines.quitAll();
        } catch (IOException e) {
            System.err.println("Error quitting child engines.");
            return 1;
        }
        return 0;
    }
}
