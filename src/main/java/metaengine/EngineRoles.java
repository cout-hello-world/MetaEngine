package metaengine;

import java.util.List;

public class EngineRoles {
    private boolean timer = false;
    private boolean recomender = false;
    private boolean analyzer = false;
    public EngineRoles(List<String> roles) {
        for (String role : roles) {
            switch (role) {
            case "timer":
                timer = true;
                break;
            case "recomender":
                recomender = true;
                break;
            case "analyzer":
                analyzer = true;
                break;
            }
        }
    }

    public boolean isTimer() {
        return timer;
    }

    public boolean isRecomender() {
        return recomender;
    }

    public boolean isAnalyzer() {
        return analyzer;
    }
}
