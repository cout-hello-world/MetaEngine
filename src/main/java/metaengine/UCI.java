package metaengine;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintStream;

public class UCI {
    private final UCIEnginesManager engines;
    public UCI(UCIEnginesManager engines) {
        this.engines = engines;
    }

    private static enum State {
        BEFORE_UCI, AFTER_UCI
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
                    out.println("uciok");
                    state = State.AFTER_UCI;
                }
                break;
            default:
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
