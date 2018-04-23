package metaengine;

import java.util.List;

public class UCIOptionBundle {
    private List<UCIOption> opts;
    private String name;
    private int index;
    UCIOptionBundle (List<UCIOption> opts, String name, int index) {
        this.opts = opts;
        this.name = name;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public List<UCIOption> getOptions() {
        return opts;
    }
}
