public class C {
    // pieces
    public static final int EMPTY = 0;
    public static final int PAWN = 1;
    public static final int ROOK = 2;
    public static final int KNIGHT = 3;
    public static final int BISHOP = 4;
    public static final int QUEEN = 5;
    public static final int KING = 6;
    public static final double[] pieceval = {0, 100, 500, 320, 325, 975, 100000};
    public static final double inf = 1e30;
    public static final long teamHash = 754734785438383L;
    
    public static final double[][] wptable = {
        {0,  0,  0,  0,  0,  0,  0,  0},
        {50, 50, 50, 50, 50, 50, 50, 50},
        {10, 10, 20, 30, 30, 20, 10, 10},
        {5,  5, 10, 27, 27, 10,  5,  5},
        {0,  0,  0, 25, 25,  0,  0,  0},
        {5, -5,-10,  0,  0,-10, -5,  5},
        {5, 10, 10,-25,-25, 10, 10,  5},
        {0,  0,  0,  0,  0,  0,  0,  0}
    }; public static final double[][] bptable = reverse(wptable);
    
    public static final double[][] wntable = {
        {-50,-40,-30,-30,-30,-30,-40,-50},
        {-40,-20,  0,  0,  0,  0,-20,-40},
        {-30,  0, 10, 15, 15, 10,  0,-30},
        {-30,  5, 15, 20, 20, 15,  5,-30},
        {-30,  0, 15, 20, 20, 15,  0,-30},
        {-30,  5, 10, 15, 15, 10,  5,-30},
        {-40,-20,  0,  5,  5,  0,-20,-40},
        {-50,-40,-20,-30,-30,-20,-40,-50},
    }; public static final double[][] bntable = reverse(wntable);
    
    public static final double[][] wbtable = {
        {-20,-10,-10,-10,-10,-10,-10,-20},
        {-10,  0,  0,  0,  0,  0,  0,-10},
        {-10,  0,  5, 10, 10,  5,  0,-10},
        {-10,  5,  5, 10, 10,  5,  5,-10},
        {-10,  0, 10, 10, 10, 10,  0,-10},
        {-10, 10, 10, 10, 10, 10, 10,-10},
        {-10,  5,  0,  0,  0,  0,  5,-10},
        {-20,-10,-40,-10,-10,-40,-10,-20},
    }; public static final double[][] bbtable = reverse(wbtable);
    
    public static final double[][] wktable = {
        {-30, -40, -40, -50, -50, -40, -40, -30},
        {-30, -40, -40, -50, -50, -40, -40, -30},
        {-30, -40, -40, -50, -50, -40, -40, -30},
        {-30, -40, -40, -50, -50, -40, -40, -30},
        {-20, -30, -30, -40, -40, -30, -30, -20},
        {-10, -20, -20, -20, -20, -20, -20, -10}, 
        {20,  20,   0,   0,   0,   0,  20,  20},
        {20,  30,  10,   0,   0,  10,  30,  20}
    }; public static final double[][] bktable = reverse(wktable);
    
    public static final double[][] wktableEND = {
        {-50,-40,-30,-20,-20,-30,-40,-50},
        {-30,-20,-10,  0,  0,-10,-20,-30},
        {-30,-10, 20, 30, 30, 20,-10,-30},
        {-30,-10, 30, 40, 40, 30,-10,-30},
        {-30,-10, 30, 40, 40, 30,-10,-30},
        {-30,-10, 20, 30, 30, 20,-10,-30},
        {-30,-30,  0,  0,  0,  0,-30,-30},
        {-50,-30,-30,-30,-30,-30,-30,-50}
    }; public static final double[][] bktableEND = reverse(wktableEND);
    
    public static final long[][][] hashes = hashTable();
    
    static double[][] reverse(double[][] table) {
        double[][] res = new double[8][8];
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                res[i][j] = table[7-i][j];
            }
        }
        return res;
    }
    static long[][][] hashTable() {
        long[][][] res = new long[8][8][12];
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                for(int k = 0; k < 12; k++) {
                    res[i][j][k] = (long)(Math.random()*1e18);
                }
            }
        }
        return res;
    }
}