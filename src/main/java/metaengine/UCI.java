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


            SearchInfo info = new SearchInfo();
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
                    System.err.println("in setoption processing");
                    engines.dispatchOption(new SetoptionInfo(tokens));
                } else if (cmd.equals("isready")) {
                    engines.synchronizeAll();
                    out.println("readyok");
                    state = State.INITIALIZED;
                }
                break;
            case INITIALIZED:
                if (cmd.equals("quit")) {
                    loop = false;
                } if (cmd.equals("go")) {
                    info = engines.search(new UCIGo(tokens));
                }
                break;
            }
        }

        engines.quitAll();
        return 0;
    }
}
