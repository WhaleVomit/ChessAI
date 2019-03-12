public class eval {
    public static int mode = 0;
    public static double evaluate(board b, int team) {
        piece[][] state = new piece[8][8];
        for(int i = 0; i < 8; i++) for(int j = 0; j < 8; j++) state[i][j] = new piece(b.state[i][j]);
        if(b.gameDone(team)) {
            if(b.kingEndangered(team)) { // checkmate
                double tot = 0;
                for(int i = 0; i < 8; i++) for(int j = 0; j < 8; j++) tot += state[i][j].numMoves;
                return -1000000*(1000-tot);
            } else { // stalemate
                return 0;
            }
        }
        if(b.isThreefoldRep(team)) return 0;
        double res = 0;
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                if(b.containsPiece(i,j)) {
                    piece p = state[i][j];
                    if(p.team == team) res += C.pieceval[p.type];
                    else res -= C.pieceval[p.type];
                }
            }
        }
        res += pieceSquare(b, team);
        return res;
    }
    public static double mobility(board b, int team) {
        double res = 0;
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                if(b.containsTeam(i,j,team)) {
                    res += b.possMoves(i,j).size();
                }
            }
        }
        return res;
    }
    public static double pieceSquare(board b, int t) {
        double res = 0;
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) if(b.containsPiece(i,j)) {
                int team = b.state[i][j].team;
                int type = b.state[i][j].type;
                if(team == 0) {
                    switch(type) {
                        case C.PAWN: res += C.wptable[i][j]; break;
                        case C.KNIGHT: res += C.wntable[i][j]; break;
                        case C.BISHOP: res += C.wbtable[i][j]; break;
                        case C.KING: res += b.endGame(team) ? C.wktableEND[i][j] : C.wktable[i][j]; break;
                    }
                } else {
                    switch(type) {
                        case C.PAWN: res -= C.bptable[i][j]; break;
                        case C.KNIGHT: res -= C.bntable[i][j]; break;
                        case C.BISHOP: res -= C.bbtable[i][j]; break;
                        case C.KING: res -= b.endGame(team) ? C.bktableEND[i][j] : C.bktable[i][j]; break;
                    }
                }
            }
        }
        if(t == 1) res = -res;
        return res;
    }
}
