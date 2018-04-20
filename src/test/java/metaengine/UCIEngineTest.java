package metaengine;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class UCIEngineTest {
    @Test(timeout=1000)
    public void uciEngineShouldConstructQuickly() throws IOException
    {
        UCIEngine engine = new UCIEngine("stockfish");
    }

    @Test
    public void setoptionAndOptionTest() throws IOException
    {
        UCIEngine engine = new UCIEngine("stockfish");
        List<UCIOption> options = engine.getOptions();
        for (UCIOption opt : options) {
            System.out.println(opt.getSetoptionString());
        }
        for (UCIOption opt : options) {
            System.out.println(opt.getOptionString("SF"));
        }
        engine.quit();
    }
}
