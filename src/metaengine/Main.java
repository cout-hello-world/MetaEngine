package metaengine;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        UCIEngine sf = new UCIEngine(new File("/usr/local/bin/stockfish"));
        sf.getOptions();
    }
}
