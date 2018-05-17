package metaengine;

public class GoResult {
    private final Score score;
    private final UCIMove move;
    private static final int MATE_CUTOFF = 100;
    public GoResult(Score score, UCIMove move) {
        this.score = score;
        this.move = move;
    }

    public UCIMove getMove() {
        return move;
    }

    public Score getScore() {
        return score;
    }

    public final static class Score implements Comparable<Score> {
        private final int comparisonScore;
        public Score(int score) {
            this(score, false);
        }
        public Score(int score, boolean isMateScore) {
            if (!isMateScore) {
                comparisonScore = score;
            } else {
                if (score < 0) {
                    comparisonScore = Integer.MIN_VALUE - score;
                } else {
                    comparisonScore = Integer.MAX_VALUE - score;
                }
            }
        }

        public Score getBiasedScore(int bias) {
            if (isMateScore()) {
                return new Score(comparisonScore, true);
            } else {
                return new Score(comparisonScore + bias);
            }
        }

        public Score flip() {
            if (comparisonScore > Integer.MAX_VALUE - MATE_CUTOFF) {
                return new Score(comparisonScore - Integer.MAX_VALUE, true);
            } else if (comparisonScore < Integer.MIN_VALUE + MATE_CUTOFF) {
                return new Score(comparisonScore - Integer.MIN_VALUE, true);
            } else {
                return new Score(-comparisonScore);
            }
        }

        @Override
        public boolean equals(Object other) {
            if (other == null || !(other instanceof Score)) {
                return false;
            }
            Score otherScore = (Score)other;
            return compareTo(otherScore) == 0;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(comparisonScore);
        }

        @Override
        public int compareTo(Score other) {
            return Integer.compare(comparisonScore, other.comparisonScore);
        }

        @Override
        public String toString() {
            if (comparisonScore > Integer.MAX_VALUE - MATE_CUTOFF) {
                return "mate " + (Integer.MAX_VALUE - comparisonScore);
            } else if (comparisonScore < Integer.MIN_VALUE + MATE_CUTOFF) {
                return "mate " + (Integer.MIN_VALUE - comparisonScore);
            } else {
                return "cp " + comparisonScore;
            }
        }

        public boolean isMateScore() {
            return comparisonScore > Integer.MAX_VALUE - MATE_CUTOFF ||
                   comparisonScore < Integer.MIN_VALUE + MATE_CUTOFF;
        }
    }
}
