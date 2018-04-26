package metaengine;

import java.util.List;
import java.util.ArrayList;

public class UCIGo {
    public enum SearchType {
        GAME, PONDER, MATE, DEPTH, NODES, MOVETIME, INFINITE
    }

    private enum State {
        BEFORE_GO, EXPECT_COMMAND, SEARCHMOVES, PONDER, WTIME, BTIME, WINC, BINC,
        MOVESTOGO, DEPTH, NODES, MATE, MOVETIME, INFINITE
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
                switch (token) {
                case "searchmoves":
                    st = State.SEARCHMOVES;
                    break;
                case "ponder":
                    st = State.PONDER;
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
                    st = State.INFINITE;
                    break;
                }
                break;
            case SEARCHMOVES:
                moves.add(new UCIMove(token));
                break;
            // TODO: Rest of cases.
            }
        }
    }

    private List<UCIMove> moves = new ArrayList<UCIMove>();
}
