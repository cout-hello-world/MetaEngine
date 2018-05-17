package metaengine;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class UCIGoTest {
    @Test
    public void testMovetime() {
        String[] testInput = {"go", "movetime", "3000"};
        UCIGo go = new UCIGo(testInput);
        assertEquals(go.getSearchType(), UCIGo.SearchType.MOVETIME);
        System.err.println(go.toString());
        assertEquals(go.toString(), "go movetime 3000");
    }
}
