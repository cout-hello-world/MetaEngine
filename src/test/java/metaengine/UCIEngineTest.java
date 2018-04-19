package metaengine;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class UCIEngineTest {
    @Test(timeout=1000)
    public void uciEngineShouldConstructQuickly() throws IOException
    {
        File engineFile = new File("/usr/local/bin/stockfish");
        UCIEngine engine = new UCIEngine(engineFile);
    }
}
