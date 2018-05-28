package metaengine;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;

public class UCIEngineTest {
    private static List<String> argv =
      Arrays.asList(new String[] {"stockfish"});
    private static List<String> roles =
      Arrays.asList(new String[] {"EVALUATOR"});
    private static final Configuration.EngineConfiguration sfConfig =
      new Configuration.EngineConfiguration(null, argv, roles, 0, 0);

    @Test(timeout=2000)
    public void uciEngineShouldConstructQuickly() throws IOException
    {
        UCIEngine engine = new UCIEngine(sfConfig);
    }

    @Test
    public void setoptionAndOptionTest() throws IOException
    {
        UCIEngine engine = new UCIEngine(sfConfig);
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
