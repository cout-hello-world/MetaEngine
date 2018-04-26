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
