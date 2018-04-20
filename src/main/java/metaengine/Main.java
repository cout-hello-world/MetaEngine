package metaengine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        UCIEngine sf = new UCIEngine("stockfish");
        UCIEngine gchess = new UCIEngine("gnuchess", "--uci");
        sf.sendOptionsAndWait(new ArrayList<UCIOption>());
        List<UCIOption> gopts = gchess.getOptions();
        for (UCIOption opt : gopts) {
            System.out.println(opt.getSetoptionString());
            System.out.println(opt.getOptionString());
        }
        gchess.sendOptionsAndWait(new ArrayList<UCIOption>());
        gchess.quit();
        sf.quit();
    }
}
