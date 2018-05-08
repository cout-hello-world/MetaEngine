package metaengine;

import java.util.List;

public class EngineRoles {
    private boolean timer = false;
    private boolean recomender = false;
    private boolean judge = false;
    public EngineRoles(List<String> roles) {
        for (String role : roles) {
            switch (role) {
            case "TIMER":
                timer = true;
                break;
            case "RECOMENDER":
                recomender = true;
                break;
            case "JUDGE":
                judge = true;
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

    public boolean isJudge() {
        return judge;
    }
}
