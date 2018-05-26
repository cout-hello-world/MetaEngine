package metaengine;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintStream;

import java.util.List;

import java.util.concurrent.Future;

public class UCI {
    private final UCIEnginesManager engines;
    private final List<UCIOptionBundle> optionBundles;
    public UCI(UCIEnginesManager engines) {
        this.engines = engines;
        optionBundles = this.engines.getUCIOptions();
    }

    private static enum State {
        BEFORE_UCI, AFTER_UCI
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

    private static void printIDInfo(PrintStream out) {
        out.println("id name " + IDInfo.getName());
        out.println("id author " + IDInfo.getAuthor());
        out.println();
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
                    printIDInfo(out);
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
                    engines.dispatchOption(new SetoptionInfo(tokens));
                } else if (cmd.equals("isready")) {
                    engines.synchronizeAll();
                    out.println("readyok");
                } else if (cmd.equals("go")) {
                    Future<UCIMove> searchFuture =
                        engines.search(new UCIGo(tokens));
                    Main.threadPool.submit(() -> {
                        try {
                            System.out.println("bestmove " +
                                searchFuture.get().toString());
                        } catch (Exception e) {
                            throw new RuntimeException(
                              "Unexpected exception thrown getting go result",
                              e);
                        }
                    });
                } else if (cmd.equals("position")) {
                    engines.setPosition(new UCIPosition(tokens));
                } else if (cmd.equals("ucinewgame")) {
                    engines.ucinewgameAll();
                } else if (cmd.equals("debug")) {
                    if (tokens.length > 1) {
                        if (tokens[1].equals("on")) {
                            ProcessIO.debug.set();
                        } else if (tokens[1].equals("off")) {
                            ProcessIO.debug.clear();
                        }
                    } else {
                        ProcessIO.debug.toggle();
                    }
                }
                break;
            }
        }

        engines.quitAll();
        return 0;
    }
}
