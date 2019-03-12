public class piece {
    public int type;
    public int team;
    public int numMoves;
    public piece() {
        this.type = -1; this.team = -1; this.numMoves = 0;
    }
    public piece(int typ, int tea) {
        this.type = typ; this.team = tea; this.numMoves = 0;
    }
    public piece(piece p) {
        this.type = p.type;
        this.team = p.team;
        this.numMoves = p.numMoves;
    }
    boolean hasPiece() {
        return this.type != -1;
    }
    boolean hasMoved() {
        return this.numMoves > 0;
    }
}
