package metaengine;

public class SearchInfo {
    private boolean isDone = false;
    private UCIMove bestMove = null;

    public synchronized boolean isDone() {
        return isDone;
    }

    public synchronized UCIMove getBestMove() {
        return bestMove;
    }

    public synchronized void setBestMove(UCIMove move, boolean isFinal) {
        if (!isDone) {
            bestMove = move;
            isDone = isFinal;
        }
    }

    public synchronized void setBestMove(UCIMove move) {
        setBestMove(move, true);
    }
}
