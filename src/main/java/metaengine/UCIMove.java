package metaengine;

public class UCIMove {
    private final String move;
    UCIMove(String moveString) {
        move = moveString;
    }

    @Override
    public String toString() {
        return move;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        UCIMove other = (UCIMove)obj;
        return move.equals(other.move);
    }

    @Override
    public int hashCode() {
        return move.hashCode();
    }
}
