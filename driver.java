import java.util.*;
public class driver {
    static boolean holdingPiece = false;
    static piece heldPiece;
    static int heldx, heldy;
    static board b;
    static pi[] killerMoves = new pi[100];
    static pi[] killerMovesQ = new pi[100];
    static long[] startTime = new long[100];
    static int cnt = 0;
    static int timeLimit = 1000;
    static boolean finishedSearch = true;
    static long seed;
    static Random rand;
    static int searchdepth = 0;
    public static void hMove(int team) {
        boolean clickCheck = false;
        while(true) {
            b.draw();
            // get mouse position
            double x = StdDraw.mouseX();
            double y = StdDraw.mouseY();
            int i = b.toPI(x,y).F; int j = b.toPI(x,y).S;
            if(i < 0 || i >= 8 || j < 0 || j >= 8) continue;
            b.shade(i,j,true);
            
            // drag and drop
            if(StdDraw.isMousePressed()) {
                if(!clickCheck) { // new click
                    if(b.containsTeam(i,j,team)) {
                        holdingPiece = true;
                        heldx = i; heldy = j;
                        heldPiece = new piece(b.state[i][j]);
                    }
                }
                if(holdingPiece) {
                    b.showmoves(heldx,heldy);
                    b.drawEmpty(heldx,heldy);
                    if(i == heldx && j == heldy) b.shade(i,j, false);
                    StdDraw.picture(x,y,b.getPNG(heldPiece),0.9,0.9);
                }
                clickCheck = true;
            } else {
                if(holdingPiece) {
                    if(b.canMove(heldx, heldy, i, j)) {
                        if(b.isPromotion(heldx,heldy,i,j)) {
                            b.drawPromotion(i,j,heldPiece.team);
                            b.drawEmpty(heldx,heldy);
                            b.drawMove(b.lastMove);
                            StdDraw.show();
                            int pro = -1;
                            while(true) {
                                double px = StdDraw.mouseX();
                                double py = StdDraw.mouseY();
                                int pi = b.toPI(px,py).F; int pj = b.toPI(px,py).S;
                                if(StdDraw.isMousePressed() && pi == i && pj == j) {
                                    double x1 = px - Math.floor(px);
                                    double y1 = py - Math.floor(py);
                                    if(x1 < 0.5 && y1 < 0.5) {
                                        pro = C.QUEEN;
                                    } else if(x1 < 0.5 && y1 > 0.5) {
                                        pro = C.KNIGHT;
                                    } else if(x1 > 0.5 && y1 < 0.5) {
                                        pro = C.ROOK;
                                    } else {
                                        pro = C.BISHOP;
                                    }
                                    break;
                                }
                            }
                            b.move(heldx, heldy, i, j, pro);
                            b.lastMove = b.hashMove(heldx,heldy,i,j,pro);
                        } else {
                            b.move(heldx, heldy, i, j, -1);
                            b.lastMove = b.hashMove(heldx,heldy,i,j,-1);
                        }
                        b.draw();
                        b.drawMove(b.lastMove);
                        StdDraw.show();
                        break;
                    }
                    holdingPiece = false;
                    heldPiece = new piece();
                }
                clickCheck = false;
                if(b.containsTeam(i,j,team)) b.showmoves(i,j);
            }
            StdDraw.pause(3);
            b.drawMove(b.lastMove);
            StdDraw.show();
        }
    }
    static boolean isKillerMove(int move, int depth) {
        return killerMoves[depth].F == move || killerMoves[depth].S == move;
    }
    static boolean isKillerMoveQ(int move, int depth) {
        return killerMovesQ[depth].F == move || killerMovesQ[depth].S == move;
    }
    static ArrayList<Integer> possibleMoves(int team, int depth) {
        HashMap<Integer, Double> toScore = new HashMap<>();
        ArrayList<Integer> res = new ArrayList<>();
        if(b.gameDone(team)) return res;
        for(int x0 = 0; x0 < 8; x0++) {
            for(int y0 = 0; y0 < 8; y0++) if(b.containsTeam(x0,y0,team)) {
                ArrayList<Integer> p = b.possMoves(x0,y0);
                for(int m: p) {
                    res.add(m);
                    int temp = m;
                    if(temp % 10 == 9) temp++;
                    temp /= 10; temp %= 64;
                    int x1 = temp/8; int y1 = temp%8;
                    piece ene = b.getCapture(x0,y0,x1,y1);
                    if(ene.hasPiece()) {
                        toScore.put(m,C.pieceval[ene.type] - C.pieceval[b.state[x0][y0].type]);
                    } else {
                        toScore.put(m,-100.0);
                    }
                }
            }
        }
        Collections.shuffle(res);
        Collections.sort(res, new Comparator<Integer>(){
            @Override
            public int compare(final Integer lhs, final Integer rhs) {
                if(isKillerMove(lhs,depth) && !isKillerMove(rhs,depth)) return -1;
                else if(!isKillerMove(lhs,depth) && isKillerMove(rhs,depth)) return 1;
                return toScore.get(rhs).compareTo(toScore.get(lhs));
            }
        });
        return res;
    }
    static ArrayList<Integer> possibleMovesQ(int team, int depth) {
        HashMap<Integer, Double> toScore = new HashMap<>();
        ArrayList<Integer> res = new ArrayList<>();
        if(b.gameDone(team)) return res;
        for(int x0 = 0; x0 < 8; x0++) {
            for(int y0 = 0; y0 < 8; y0++) if(b.containsTeam(x0,y0,team)) {
                ArrayList<Integer> p = b.possMoves(x0,y0);
                for(int m: p) {
                    int temp = m;
                    if(temp % 10 == 9) temp++;
                    temp /= 10; temp %= 64;
                    int x1 = temp/8; int y1 = temp%8;
                    piece ene = b.getCapture(x0,y0,x1,y1);
                    if(ene.hasPiece()) {
                        toScore.put(m,C.pieceval[ene.type] - C.pieceval[b.state[x0][y0].type]);
                        res.add(m);
                    }
                }
            }
        }
        Collections.shuffle(res);
        Collections.sort(res, new Comparator<Integer>(){
            @Override
            public int compare(final Integer lhs, final Integer rhs) {
                if(isKillerMoveQ(lhs,depth) && !isKillerMoveQ(rhs,depth)) return -1;
                else if(!isKillerMoveQ(lhs,depth) && isKillerMoveQ(rhs,depth)) return 1;
                return toScore.get(rhs).compareTo(toScore.get(lhs));
            }
        });
        return res;
    }
    static double getScoreQ(int team, int turnsleft, double alpha, double beta) {
        if(getTime(1) >= timeLimit) {
            finishedSearch = false;
            return -1;
        }
        double standpat = b.getScore(team);
        if(standpat > alpha) alpha = standpat;
        if(alpha >= beta) return beta;
        ArrayList<Integer> moves = possibleMovesQ(team, turnsleft);
        if(moves.size() == 0) return b.getScore(team);
        piece[][] initialState = new piece[8][8];
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                initialState[i][j] = new piece(b.state[i][j]);
            }
        }
        double bes = -C.inf;
        int besmove = -1;
        if(turnsleft == 0) {
            return standpat;
        } else {
            for(Integer m: moves) {
                int temp = m;
                int pro = temp%10;
                if(pro == 9) {
                    pro = -1;
                    temp++;
                }
                temp /= 10;
                int p0 = temp/64; int p1 = temp%64;
                int x0 = p0/8; int y0 = p0%8;
                int x1 = p1/8; int y1 = p1%8;
                b.move(x0,y0,x1,y1,pro);
                double s = -getScoreQ(1-team, turnsleft-1, -beta, -alpha);
                if(s > bes) {
                    bes = s;
                    alpha = Math.max(alpha, bes);
                    besmove = m;
                }
                b.remHash(1-team);
                for(int i = 0; i < 8; i++) {
                    for(int j = 0; j < 8; j++) {
                        b.state[i][j] = new piece(initialState[i][j]);
                    }
                }
                if(alpha >= beta) break;
            }
        }
        if(!isKillerMoveQ(besmove, turnsleft)) {
            killerMovesQ[turnsleft].F = killerMovesQ[turnsleft].S;
            killerMovesQ[turnsleft].S = besmove;
        }
        return bes;
    }
    static double getScore(int team, int turnsleft, double alpha, double beta) {
        if(getTime(1) >= timeLimit) {
            finishedSearch = false;
            return -1;
        }
        ArrayList<Integer> moves = possibleMoves(team, turnsleft);
        if(moves.size() == 0) return b.getScore(team);
        piece[][] initialState = new piece[8][8];
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                initialState[i][j] = new piece(b.state[i][j]);
            }
        }
        double bes = -C.inf;
        int besmove = -1;
        if(turnsleft == 0) {
            return b.getScore(team);
            //return getScoreQ(team,turnsleft,alpha,beta);
        } else {
            for(Integer m: moves) {
                int temp = m;
                int pro = temp%10;
                if(pro == 9) {
                    pro = -1;
                    temp++;
                }
                temp /= 10;
                int p0 = temp/64; int p1 = temp%64;
                int x0 = p0/8; int y0 = p0%8;
                int x1 = p1/8; int y1 = p1%8;
                b.move(x0,y0,x1,y1,pro);
                double s = -getScore(1-team, turnsleft-1, -beta, -alpha);
                if(s > bes) {
                    bes = s;
                    alpha = Math.max(alpha, bes);
                    besmove = m;
                }
                b.remHash(1-team);
                for(int i = 0; i < 8; i++) {
                    for(int j = 0; j < 8; j++) {
                        b.state[i][j] = new piece(initialState[i][j]);
                    }
                }
                if(alpha >= beta) break;
            }
        }
        if(!isKillerMove(besmove, turnsleft)) {
            killerMoves[turnsleft].F = killerMoves[turnsleft].S;
            killerMoves[turnsleft].S = besmove;
        }
        return bes;
    }
    static double euclidDist(double x0, double y0, double x1, double y1) {
        return Math.sqrt(Math.pow(x0-x1,2) + Math.pow(y0-y1, 2));
    }
    static int besMove(int team, int depth) {
        piece[][] initialState = new piece[8][8];
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                initialState[i][j] = new piece(b.state[i][j]);
            }
        }
        double alpha = -C.inf; double beta = C.inf;
        ArrayList<Integer> moves = possibleMoves(team, depth);
        int besmove = -1;
        double bes = -C.inf;
        for(Integer m: moves) {
            int temp = m;
            int pro = temp%10;
            if(pro == 9) {
                pro = -1;
                temp++;
            }
            temp /= 10;
            int p0 = temp/64; int p1 = temp%64;
            int x0 = p0/8; int y0 = p0%8;
            int x1 = p1/8; int y1 = p1%8;
            b.move(x0,y0,x1,y1,pro);
            double s = -getScore(1-team, depth-1, -beta, -alpha);
            if(s > bes) {
                bes = s;
                alpha = Math.max(alpha, bes);
                besmove = m;
            }
            b.remHash(1-team);
            for(int i = 0; i < 8; i++) {
                for(int j = 0; j < 8; j++) {
                    b.state[i][j] = new piece(initialState[i][j]);
                }
            }
            if(alpha >= beta) break;
        }
        return besmove;
    }
    static void cMove(int team) {
        int m = 0;
        setTime(1); finishedSearch = true;
        for(int depth = 1; depth < 20; depth++) {
            searchdepth = depth;
            for(int i = 0; i < 100; i++) killerMoves[i] = new pi(-1, -1);
            for(int i = 0; i < 100; i++) killerMovesQ[i] = new pi(-1, -1);
            cnt = 0;
            int move = besMove(team, depth);
            if(finishedSearch) {
                m = move;
            } else {
                break;
            }
        }
        System.out.println("Search depth: " + searchdepth);
        
        b.lastMove = m;
        int pro = m%10;
        if(pro == 9) {
            pro = -1;
            m++;
        }
        m /= 10;
        int p0 = m/64; int p1 = m%64;
        int x0 = p0/8; int y0 = p0%8;
        int x1 = p1/8; int y1 = p1%8;
        System.out.println(b.toChessMove(x0,y0,x1,y1));
        b.move(x0,y0,x1,y1,pro);
        
        // animation
        double cx = b.toPD(x0,y0).F; double cy = b.toPD(x0,y0).S;
        double desx = b.toPD(x1,y1).F; double desy = b.toPD(x1,y1).S;
        double xx = desx-cx; double yy = desy-cy;
        double dis = Math.sqrt(xx*xx+yy*yy);
        double v = dis/50;
        double dx = v*xx/dis; double dy = v*yy/dis;
        for(int i = 0; i < dis/v; i++) {
            b.draw(); b.drawEmpty(x1,y1);
            StdDraw.picture(cx,cy,b.getPNG(b.state[x1][y1]), 0.8, 0.8);
            b.drawMove(b.lastMove);
            StdDraw.show();
            //StdDraw.pause(1);
            cx += dx;
            cy += dy;
        }
        b.draw(); b.drawMove(b.lastMove); StdDraw.show();
    }
    public static void setTime(int ind) {
        startTime[ind] = System.currentTimeMillis();
    }
    public static long getTime(int ind) {
        return System.currentTimeMillis() - startTime[ind];
    }
    public static void main(String[] args) {
        System.out.println("Team");
        Scanner sc = new Scanner(System.in);
        b = new board(); //int team = sc.nextInt(); b.side = team;
        StdDraw.enableDoubleBuffering();
        StdDraw.setScale(0,8);
        double tot = 0; double cnt = 0;
        b.draw(); StdDraw.show();
        for(int i = 0; i < 1000; i++) {
            setTime(0);
            //System.out.println(b.seen);
            if(b.gameDone(i%2)) break;
            if(i%2 == 0) {hMove(i%2);}
            else {cMove(i%2);}
            
            //System.out.println("White score: " + b.getScore(0));
            //System.out.println("Black score: " + b.getScore(1));
            /*if(i%2 == 0) {
                //hMove(i%2);
                if(team == i%2) cMove(i%2);
                else hMove(i%2);
            } else {
                //cMove(i%2);
                if(team == i%2) cMove(i%2);
                else hMove(i%2);
            }*/
            tot += getTime(0); cnt++;
            System.out.println("Time taken: " + getTime(0));
            //System.out.println("Average time: " + tot/cnt);
            System.out.println("---------------------------------------------");
        }
    }
}