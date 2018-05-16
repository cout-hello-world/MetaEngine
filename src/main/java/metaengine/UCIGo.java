package metaengine;

import java.util.List;
import java.util.ArrayList;

public class UCIGo {
    public enum SearchType {
        GAME, MATE, DEPTH, NODES, MOVETIME, INFINITE
    }

    private enum State {
        BEFORE_GO, EXPECT_COMMAND, SEARCHMOVES, WTIME, BTIME, WINC, BINC,
        MOVESTOGO, DEPTH, NODES, MATE, MOVETIME
    }

    private static final String[] infiniteArray = {"go", "infinite"};
    public static final UCIGo INFINITE = new UCIGo(infiniteArray);

    public UCIGo(String[] tokens) {
        State st = State.BEFORE_GO;
        for (String token : tokens) {
            switch (st) {
            case BEFORE_GO:
                if (token.equals("go")) {
                    st = State.EXPECT_COMMAND;
                }
                break;
            case EXPECT_COMMAND:
            case SEARCHMOVES:
                switch (token) {
                case "searchmoves":
                    st = State.SEARCHMOVES;
                    break;
                case "ponder":
                    ponder = true;
                    break;
                case "wtime":
                    st = State.WTIME;
                    break;
                case "btime":
                    st = State.BTIME;
                    break;
                case "winc":
                    st = State.WINC;
                    break;
                case "binc":
                    st = State.BINC;
                    break;
                case "movestogo":
                    st = State.MOVESTOGO;
                    break;
                case "depth":
                    st = State.DEPTH;
                    break;
                case "nodes":
                    st = State.NODES;
                    break;
                case "mate":
                    st = State.MATE;
                    break;
                case "movetime":
                    st = State.MOVETIME;
                    break;
                case "infinite":
                    infinite = true;
                    break;
                default:
                    if (st == State.SEARCHMOVES) {
                        moves.add(new UCIMove(token));
                    }
                    break;
                }
                break;
            case WTIME:
                wtime = UCIUtils.tryParseUnsigned(token);
                st = State.EXPECT_COMMAND;
                break;
            case BTIME:
                btime = UCIUtils.tryParseUnsigned(token);
                st = State.EXPECT_COMMAND;
                break;
            case WINC:
                winc = UCIUtils.tryParseUnsigned(token);
                st = State.EXPECT_COMMAND;
                break;
            case BINC:
                binc = UCIUtils.tryParseUnsigned(token);
                st = State.EXPECT_COMMAND;
                break;
            case MOVESTOGO:
                movestogo = UCIUtils.tryParseUnsigned(token);
                st = State.EXPECT_COMMAND;
                break;
            case DEPTH:
                depth = UCIUtils.tryParseUnsigned(token);
                st = State.EXPECT_COMMAND;
                break;
            case NODES:
                nodes = UCIUtils.tryParseUnsigned(token);
                st = State.EXPECT_COMMAND;
                break;
            case MATE:
                mate = UCIUtils.tryParseUnsigned(token);
                st = State.EXPECT_COMMAND;
            case MOVETIME:
                movetime = UCIUtils.tryParseUnsigned(token);
                st = State.EXPECT_COMMAND;
                break;
            }
        }
    }

    public SearchType getSearchType() {
        if (infinite) {
            return SearchType.INFINITE;
        } else if (wtime != -1 || btime != -1 || winc != -1 || binc != -1 ||
            movestogo != -1) {
            return SearchType.GAME;
        } else if (depth != -1) {
            return SearchType.DEPTH;
        } else if (nodes != -1) {
            return SearchType.NODES;
        } else if (mate != -1) {
            return SearchType.MATE;
        } else if (movetime != -1) {
            return SearchType.MOVETIME;
        } else {
            throw new RuntimeException("Invalid state in UCIGo");
        }
    }

    public boolean isPonder() {
        return ponder;
    }

    /**
     * This function converts a UCIGo from an input UCIGo to one suitable for
     * a timer.
     *
     * In other words, it halves the values of the increment and time remaining.
     */
    public UCIGo getConvertedForTimer() {
        SearchType searchType = getSearchType();
        UCIGo res = new UCIGo(this);

        switch (searchType) {
        case GAME:
            if (res.winc != -1) {
                res.winc /= 2;
            }
            if (res.binc != -1) {
                res.binc /= 2;
            }
            if (res.wtime != -1) {
                res.wtime /= 2;
            }
            break;
        case MOVETIME:
            if (res.movetime != -1) {
                res.movetime /= 2;
            }
            break;
        default:
            // TODO: Other types of searches
            throw new RuntimeException(
              "Only game and movetime type searches are presently implemented");
        }
        return res;
    }

    private UCIGo(UCIGo orig) {
        for (UCIMove move : orig.moves) {
            moves.add(move);
        }
        ponder = orig.ponder;
        infinite = orig.infinite;
        wtime = orig.wtime;
        btime = orig.btime;
        winc = orig.winc;
        movestogo = orig.movestogo;
        depth = orig.depth;
        mate = orig.mate;
        movetime = orig.movetime;
    }

    @Override
    public String toString() {
        // If performace becomes an issue consider caching this string.
        StringBuilder builder = new StringBuilder("go");
        if (moves.size() != 0) {
            builder.append(" searchmoves");
            for (UCIMove move : moves) {
                builder.append(" " + move.toString());
            }
        }
        if (ponder) {
            builder.append(" ponder");
        }
        if (wtime != -1) {
            builder.append(" wtime " + wtime);
        }
        if (btime != -1) {
            builder.append(" btime " + btime);
        }
        if (winc != -1) {
            builder.append(" winc " + winc);
        }
        if (binc != -1) {
            builder.append(" binc " + binc);
        }
        if (movestogo != -1) {
            builder.append(" movestogo " + movestogo);
        }
        if (depth != -1) {
            builder.append(" depth " + depth);
        }
        if (mate != -1) {
            builder.append(" mate " + mate);
        }
        if (infinite) {
            builder.append(" infinite");
        }
        return builder.toString();
    }

    // NOTE: Any fields added must be handled in the copy ctor
    private List<UCIMove> moves = new ArrayList<UCIMove>();
    private boolean ponder = false;
    private boolean infinite = false;
    private int wtime = -1;
    private int btime = -1;
    private int winc = -1;
    private int binc = -1;
    private int movestogo = -1;
    private int depth = -1;
    private int nodes = -1;
    private int mate = -1;
    private int movetime = -1;
}
