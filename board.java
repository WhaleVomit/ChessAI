import java.awt.*;
import java.util.*;
public class board {
    public piece[][] state;
    public int side = 0;
    public int lastMove = -1;
    public HashMap<Long, Integer> seen = new HashMap<>();
    public board() {
        state = new piece[8][8];
        for(int i = 0; i < 8; i++) for(int j = 0; j < 8; j++) state[i][j] = new piece();
        for(int i = 0; i < 8; i++) {
            state[1][i] = new piece(C.PAWN, 1);
            state[6][i] = new piece(C.PAWN, 0);
        }
        state[0][0] = new piece(C.ROOK, 1); state[0][7] = new piece(C.ROOK, 1);
        state[0][1] = new piece(C.KNIGHT, 1); state[0][6] = new piece(C.KNIGHT, 1);
        state[0][2] = new piece(C.BISHOP, 1); state[0][5] = new piece(C.BISHOP, 1);
        state[0][3] = new piece(C.QUEEN, 1); state[0][4] = new piece(C.KING, 1);
        
        state[7][0] = new piece(C.ROOK, 0); state[7][7] = new piece(C.ROOK, 0);
        state[7][1] = new piece(C.KNIGHT, 0); state[7][6] = new piece(C.KNIGHT, 0);
        state[7][2] = new piece(C.BISHOP, 0); state[7][5] = new piece(C.BISHOP, 0);
        state[7][3] = new piece(C.QUEEN, 0); state[7][4] = new piece(C.KING, 0);
        /*state[4][4] = new piece(C.KING, 0);
        state[7][7] = new piece(C.KING, 1);
        state[1][6] = new piece(C.PAWN, 1);*/
        addHash(0);
    }
    double getScore(int team) {
        return eval.evaluate(this, team);
    }
    boolean gameDone(int team) {
       if(isThreefoldRep(team)) return true;
       if(!canMove(team)) return true;
       return false;
    }
    boolean canMove(int team) {
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                if(containsTeam(i,j, team)){
                    if(possMoves(i,j).size() > 0) return true;
                }
            }
        }
        return false;
    }
    public boolean isThreefoldRep(int team) {
        long h = getHash(team);
        if(!seen.containsKey(h)) return false;
        //System.out.println(seen.get(h));
        return seen.get(h) >= 3;
    }
    pd toPD(int x, int y) {
        if(side == 0) return new pd(y+0.5, 8-(x+0.5));
        else return new pd(8-(y+0.5), x+0.5);
    }
    pi toPI(double x, double y) {
        if(side == 0) return new pi(7-(int)y, (int)x);
        else return new pi((int)y, 7-(int)x);
    }
    void drawEmpty(int i, int j) {
        pd p = toPD(i,j);
        double x = p.F;
        double y = p.S;
        if((i+j)%2 == 0) {
            StdDraw.setPenColor(247,236,207);
        } else {
            StdDraw.setPenColor(109,99,66);
        }
        StdDraw.filledSquare(x,y,0.5);
    }
    void shade(int i, int j, boolean drawPiece) {
        drawEmpty(i,j);
        pd p = toPD(i,j);
        double x = p.F;
        double y = p.S;
        StdDraw.setPenColor(new Color(240,240,77,150));
        StdDraw.filledSquare(x,y,0.5);
        if(drawPiece && containsPiece(i,j)) {
            StdDraw.picture(toPD(i,j).F, toPD(i,j).S, getPNG(state[i][j]), 0.8);
        }
    }
    void draw() {
        StdDraw.setScale(0,8);
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                pd p = toPD(i,j);
                double x = p.F;
                double y = p.S;
                drawEmpty(i,j);
                if(containsPiece(i,j)) {
                    String filename = getPNG(state[i][j]);
                    StdDraw.picture(x,y,filename, 0.8, 0.8);
                }
            }
        }
    }
    void drawPromotion(int i, int j, int team) { // shows 4 options to promote
        drawEmpty(i,j);
        pd p = toPD(i,j);
        double xx = p.F;
        double yy = p.S;
        StdDraw.picture(xx-0.25,yy-0.25,getPNG(new piece(C.QUEEN,team)),0.4,0.4);
        StdDraw.picture(xx-0.25,yy+0.25,getPNG(new piece(C.KNIGHT,team)),0.4,0.4);
        StdDraw.picture(xx+0.25,yy-0.25,getPNG(new piece(C.ROOK,team)),0.4,0.4);
        StdDraw.picture(xx+0.25,yy+0.25,getPNG(new piece(C.BISHOP,team)),0.4,0.4);
    }
    void drawMove(int i, int j) {
        pd p = toPD(i,j);
        double drawx = p.F;
        double drawy = p.S;
        if(containsPiece(i,j)) StdDraw.setPenColor(new Color(255,0,0,100));
        else StdDraw.setPenColor(new Color(0,0,0,100));
        StdDraw.filledCircle(drawx,drawy,0.15);
    }
    void showmoves(int x, int y) {
        ArrayList<Integer> poss = possMoves(x,y);
        boolean[][] drawn = new boolean[8][8];
        for(int m: poss) {
            int temp = m;
            if(temp%10 == 9) temp++;
            temp /= 10;
            int p1 = temp%64;
            int x1 = p1/8; int y1 = p1%8;
            if(!drawn[x1][y1]) drawMove(x1,y1);
            drawn[x1][y1] = true;
        }
    }
    boolean isAttacking(int x0, int y0, int x1, int y1) { // does piece at (x0,y0) attack (x1,y1)?
        int type = state[x0][y0].type;
        int team = state[x0][y0].team;
        if(x0 == x1 && y0 == y1) return false;
        if(type == C.PAWN) {
            if(r2(x0,y0,x1,y1) != 2) return false;
            if(team == 0) return x1 < x0;
            else return x1 > x0;
        } else if(type == C.ROOK) {
            if(x0 != x1 && y0 != y1) return false;
            int cx = x0; int cy = y0;
            while(cx != x1 || cy != y1) {
                if(!(cx == x0 && cy == y0) && !(cx == x1 && cy == y1) && containsPiece(cx,cy)) return false;
                if(cx < x1) cx++;
                else if(cx > x1) cx--;
                else if(cy < y1) cy++;
                else cy--;
            }
            return true;
        } else if(type == C.KNIGHT) {
            int dx = x1 - x0;
            int dy = y1 - y0;
            int mx = Math.max(Math.abs(dx), Math.abs(dy));
            int mn = Math.min(Math.abs(dx), Math.abs(dy));
            return mx == 2 && mn == 1;
        } else if(type == C.BISHOP) {
            if(x0-y0 != x1-y1 && x0+y0 != x1+y1) return false;
            int cx = x0; int cy = y0;
            while(cx != x1 || cy != y1) {
                if(!(cx == x0 && cy == y0) && !(cx == x1 && cy == y1) && containsPiece(cx,cy)) return false;
                if(cx < x1 && cy < y1) {
                    cx++; cy++;
                } else if(cx < x1 && cy > y1) {
                    cx++; cy--;
                } else if(cx > x1 && cy < y1) {
                    cx--; cy++;
                } else {
                    cx--; cy--;
                }
            }
            return true;
        } else if(type == C.QUEEN) {
            if(x0 == x1 || y0 == y1) {
                int cx = x0; int cy = y0;
                while(cx != x1 || cy != y1) {
                    if(!(cx == x0 && cy == y0) && !(cx == x1 && cy == y1) && containsPiece(cx,cy)) return false;
                    if(cx < x1) cx++;
                    else if(cx > x1) cx--;
                    else if(cy < y1) cy++;
                    else cy--;
                }
                return true;
            } else if(x0-y0 == x1-y1 || x0+y0 == x1+y1) {
                int cx = x0; int cy = y0;
                while(cx != x1 || cy != y1) {
                    if(!(cx == x0 && cy == y0) && !(cx == x1 && cy == y1) && containsPiece(cx,cy)) return false;
                    if(cx < x1 && cy < y1) {
                        cx++; cy++;
                    } else if(cx < x1 && cy > y1) {
                        cx++; cy--;
                    } else if(cx > x1 && cy < y1) {
                        cx--; cy++;
                    } else {
                        cx--; cy--;
                    }
                }
                return true;
            } else {
                return false;
            }
        } else {
            return r2(x0,y0,x1,y1) <= 2;
        }
    }
    boolean isSquareAttacked(int x, int y, int team) {
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) if(containsTeam(i,j,team)) {
                if(isAttacking(i,j, x,y)) return true;
            }
        }
        return false;
    }
    boolean kingEndangered(int team) {
        int x = -1; int y = -1;
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                if(containsTeam(i,j,team) && state[i][j].type == C.KING) {
                    x = i; y = j;
                    break;
                }
            }
        }
        if(x == -1) return false; // no king
        return isSquareAttacked(x,y, 1-team);
    }
    boolean containsPiece(int x, int y) {
        return state[x][y].hasPiece();
    }
    boolean containsTeam(int x, int y, int team) {
        return containsPiece(x,y) && state[x][y].team == team;
    }
    boolean isPromotion(int x0, int y0, int x1, int y1) {
        int type = state[x0][y0].type;
        int team = state[x0][y0].team;
        if(!containsTeam(x0,y0,team)) return false;
        if(type != C.PAWN) return false;
        if(team == 0 && x1 != 0) return false;
        if(team == 1 && x1 != 7) return false;
        return true;
    }
    int hashMove(int x0, int y0, int x1, int y1, int pro) {
        return 10*(64*(8*x0+y0)+(8*x1+y1))+pro;
    }
    boolean inBoard(int x, int y) {
        return x >= 0 && x < 8 && y >= 0 && y < 8;
    }
    boolean isEnPassant(int x0, int y0, int x1, int y1) {
        int type = state[x0][y0].type;
        int team = state[x0][y0].team;
        int m = lastMove; if(m%10 == 9) m++;
        m /= 10;
        int p1 = m%64;
        int px = p1/8; int py = p1%8;
        if(team == 0) {
            if(x0 != 3) return false;
            if(x1 != 2) return false;
            if(Math.abs(y1-y0) != 1) return false;
            return containsTeam(3,y1,1) && state[3][y1].type == C.PAWN && state[3][y1].numMoves == 1 && 3 == px && y1 == py;
        } else {
            if(x0 != 4) return false;
            if(x1 != 5) return false;
            if(Math.abs(y1-y0) != 1) return false;
            return containsTeam(4,y1,0) && state[4][y1].type == C.PAWN && state[4][y1].numMoves == 1 && 4 == px && y1 == py;
        }
    }
    piece getCapture(int x0, int y0, int x1, int y1) {
        if(containsPiece(x1,y1)) return new piece(state[x1][y1]);
        if(isEnPassant(x0,y0,x1,y1)) {
            if(state[x0][y0].team == 0) return new piece(state[3][y1]);
            else return new piece(state[4][y1]);
        }
        return new piece();
    }
    ArrayList<Integer> possMoves(int x0, int y0) {
        ArrayList<Integer> res = new ArrayList<>();
        if(!containsPiece(x0,y0)) return res;
        int type = state[x0][y0].type;
        int team = state[x0][y0].team;
        if(type == C.PAWN) {
            if(team == 0) {
                if(x0 == 6) {
                    if(!containsPiece(x0-1,y0) && !containsPiece(x0-2,y0)) res.add(hashMove(x0,y0,x0-2,y0,-1));
                }
                if(!containsPiece(x0-1,y0)) {
                    if(isPromotion(x0,y0,x0-1,y0)) {
                        res.add(hashMove(x0,y0,x0-1,y0,C.QUEEN));
                        res.add(hashMove(x0,y0,x0-1,y0,C.KNIGHT));
                        res.add(hashMove(x0,y0,x0-1,y0,C.ROOK));
                        res.add(hashMove(x0,y0,x0-1,y0,C.BISHOP));
                    } else {
                        res.add(hashMove(x0,y0,x0-1,y0,-1));
                    }
                }
                if(inBoard(x0-1,y0+1) && (containsTeam(x0-1,y0+1,1-team) || isEnPassant(x0,y0,x0-1,y0+1))) {
                    if(isPromotion(x0,y0,x0-1,y0+1)) {
                        res.add(hashMove(x0,y0,x0-1,y0+1,C.QUEEN));
                        res.add(hashMove(x0,y0,x0-1,y0+1,C.KNIGHT));
                        res.add(hashMove(x0,y0,x0-1,y0+1,C.ROOK));
                        res.add(hashMove(x0,y0,x0-1,y0+1,C.BISHOP));
                    } else {
                        res.add(hashMove(x0,y0,x0-1,y0+1,-1));
                    }
                }
                if(inBoard(x0-1,y0-1) && (containsTeam(x0-1,y0-1,1-team) || isEnPassant(x0,y0,x0-1,y0-1))) {
                    if(isPromotion(x0,y0,x0-1,y0-1)) {
                        res.add(hashMove(x0,y0,x0-1,y0-1,C.QUEEN));
                        res.add(hashMove(x0,y0,x0-1,y0-1,C.KNIGHT));
                        res.add(hashMove(x0,y0,x0-1,y0-1,C.ROOK));
                        res.add(hashMove(x0,y0,x0-1,y0-1,C.BISHOP));
                    } else {
                        res.add(hashMove(x0,y0,x0-1,y0-1,-1));
                    }
                }
            } else {
                if(x0 == 1) {
                    if(!containsPiece(x0+1,y0) && !containsPiece(x0+2,y0)) res.add(hashMove(x0,y0,x0+2,y0,-1));
                }
                if(!containsPiece(x0+1,y0)) {
                    if(isPromotion(x0,y0,x0+1,y0)) {
                        res.add(hashMove(x0,y0,x0+1,y0,C.QUEEN));
                        res.add(hashMove(x0,y0,x0+1,y0,C.KNIGHT));
                        res.add(hashMove(x0,y0,x0+1,y0,C.ROOK));
                        res.add(hashMove(x0,y0,x0+1,y0,C.BISHOP));
                    } else {
                        res.add(hashMove(x0,y0,x0+1,y0,-1));
                    }
                }
                if(inBoard(x0+1,y0+1) && (containsTeam(x0+1,y0+1,1-team) || isEnPassant(x0,y0,x0+1,y0+1))) {
                    if(isPromotion(x0,y0,x0+1,y0+1)) {
                        res.add(hashMove(x0,y0,x0+1,y0+1,C.QUEEN));
                        res.add(hashMove(x0,y0,x0+1,y0+1,C.KNIGHT));
                        res.add(hashMove(x0,y0,x0+1,y0+1,C.ROOK));
                        res.add(hashMove(x0,y0,x0+1,y0+1,C.BISHOP));
                    } else {
                        res.add(hashMove(x0,y0,x0+1,y0+1,-1));
                    }
                }
                if(inBoard(x0+1,y0-1) && (containsTeam(x0+1,y0-1,1-team) || isEnPassant(x0,y0,x0+1,y0-1))) {
                    if(isPromotion(x0,y0,x0+1,y0-1)) {
                        res.add(hashMove(x0,y0,x0+1,y0-1,C.QUEEN));
                        res.add(hashMove(x0,y0,x0+1,y0-1,C.KNIGHT));
                        res.add(hashMove(x0,y0,x0+1,y0-1,C.ROOK));
                        res.add(hashMove(x0,y0,x0+1,y0-1,C.BISHOP));
                    } else {
                        res.add(hashMove(x0,y0,x0+1,y0-1,-1));
                    }
                }
            }
        } else if(type == C.ROOK) {
            int cx = x0; int cy = y0;
            boolean enemyFound = false;
            while(true) {
                cx++;
                if(!inBoard(cx,cy) || containsTeam(cx,cy,team) || enemyFound) break;
                if(containsTeam(cx,cy,1-team)) enemyFound = true;
                res.add(hashMove(x0,y0,cx,cy,-1));
            }
            
            cx = x0; cy = y0;
            enemyFound = false;
            while(true) {
                cx--;
                if(!inBoard(cx,cy) || containsTeam(cx,cy,team) || enemyFound) break;
                if(containsTeam(cx,cy,1-team)) enemyFound = true;
                res.add(hashMove(x0,y0,cx,cy,-1));
            }
            
            cx = x0; cy = y0;
            enemyFound = false;
            while(true) {
                cy++;
                if(!inBoard(cx,cy) || containsTeam(cx,cy,team) || enemyFound) break;
                if(containsTeam(cx,cy,1-team)) enemyFound = true;
                res.add(hashMove(x0,y0,cx,cy,-1));
            }
            
            cx = x0; cy = y0;
            enemyFound = false;
            while(true) {
                cy--;
                if(!inBoard(cx,cy) || containsTeam(cx,cy,team) || enemyFound) break;
                if(containsTeam(cx,cy,1-team)) enemyFound = true;
                res.add(hashMove(x0,y0,cx,cy,-1));
            }
        } else if(type == C.BISHOP) {
            int cx = x0; int cy = y0;
            boolean enemyFound = false;
            while(true) {
                cx++; cy++;
                if(!inBoard(cx,cy) || containsTeam(cx,cy,team) || enemyFound) break;
                if(containsTeam(cx,cy,1-team)) enemyFound = true;
                res.add(hashMove(x0,y0,cx,cy,-1));
            }
            
            cx = x0; cy = y0;
            enemyFound = false;
            while(true) {
                cx++; cy--;
                if(!inBoard(cx,cy) || containsTeam(cx,cy,team) || enemyFound) break;
                if(containsTeam(cx,cy,1-team)) enemyFound = true;
                res.add(hashMove(x0,y0,cx,cy,-1));
            }
            
            cx = x0; cy = y0;
            enemyFound = false;
            while(true) {
                cx--; cy++;
                if(!inBoard(cx,cy) || containsTeam(cx,cy,team) || enemyFound) break;
                if(containsTeam(cx,cy,1-team)) enemyFound = true;
                res.add(hashMove(x0,y0,cx,cy,-1));
            }
            
            cx = x0; cy = y0;
            enemyFound = false;
            while(true) {
                cx--; cy--;
                if(!inBoard(cx,cy) || containsTeam(cx,cy,team) || enemyFound) break;
                if(containsTeam(cx,cy,1-team)) enemyFound = true;
                res.add(hashMove(x0,y0,cx,cy,-1));
            }
        } else if(type == C.QUEEN) {
            int cx = x0; int cy = y0;
            boolean enemyFound = false;
            while(true) {
                cx++;
                if(!inBoard(cx,cy) || containsTeam(cx,cy,team) || enemyFound) break;
                if(containsTeam(cx,cy,1-team)) enemyFound = true;
                res.add(hashMove(x0,y0,cx,cy,-1));
            }
            
            cx = x0; cy = y0;
            enemyFound = false;
            while(true) {
                cx--;
                if(!inBoard(cx,cy) || containsTeam(cx,cy,team) || enemyFound) break;
                if(containsTeam(cx,cy,1-team)) enemyFound = true;
                res.add(hashMove(x0,y0,cx,cy,-1));
            }
            
            cx = x0; cy = y0;
            enemyFound = false;
            while(true) {
                cy++;
                if(!inBoard(cx,cy) || containsTeam(cx,cy,team) || enemyFound) break;
                if(containsTeam(cx,cy,1-team)) enemyFound = true;
                res.add(hashMove(x0,y0,cx,cy,-1));
            }
            
            cx = x0; cy = y0;
            enemyFound = false;
            while(true) {
                cy--;
                if(!inBoard(cx,cy) || containsTeam(cx,cy,team) || enemyFound) break;
                if(containsTeam(cx,cy,1-team)) enemyFound = true;
                res.add(hashMove(x0,y0,cx,cy,-1));
            }
            
            cx = x0; cy = y0;
            enemyFound = false;
            while(true) {
                cx++; cy++;
                if(!inBoard(cx,cy) || containsTeam(cx,cy,team) || enemyFound) break;
                if(containsTeam(cx,cy,1-team)) enemyFound = true;
                res.add(hashMove(x0,y0,cx,cy,-1));
            }
            
            cx = x0; cy = y0;
            enemyFound = false;
            while(true) {
                cx++; cy--;
                if(!inBoard(cx,cy) || containsTeam(cx,cy,team) || enemyFound) break;
                if(containsTeam(cx,cy,1-team)) enemyFound = true;
                res.add(hashMove(x0,y0,cx,cy,-1));
            }
            
            cx = x0; cy = y0;
            enemyFound = false;
            while(true) {
                cx--; cy++;
                if(!inBoard(cx,cy) || containsTeam(cx,cy,team) || enemyFound) break;
                if(containsTeam(cx,cy,1-team)) enemyFound = true;
                res.add(hashMove(x0,y0,cx,cy,-1));
            }
            
            cx = x0; cy = y0;
            enemyFound = false;
            while(true) {
                cx--; cy--;
                if(!inBoard(cx,cy) || containsTeam(cx,cy,team) || enemyFound) break;
                if(containsTeam(cx,cy,1-team)) enemyFound = true;
                res.add(hashMove(x0,y0,cx,cy,-1));
            }
        } else if(type == C.KNIGHT) {
            if(inBoard(x0+2,y0+1) && !containsTeam(x0+2,y0+1,team)) res.add(hashMove(x0,y0,x0+2,y0+1,-1));
            if(inBoard(x0+2,y0-1) && !containsTeam(x0+2,y0-1,team)) res.add(hashMove(x0,y0,x0+2,y0-1,-1));
            if(inBoard(x0-2,y0+1) && !containsTeam(x0-2,y0+1,team)) res.add(hashMove(x0,y0,x0-2,y0+1,-1));
            if(inBoard(x0-2,y0-1) && !containsTeam(x0-2,y0-1,team)) res.add(hashMove(x0,y0,x0-2,y0-1,-1));
            
            if(inBoard(x0+1,y0+2) && !containsTeam(x0+1,y0+2,team)) res.add(hashMove(x0,y0,x0+1,y0+2,-1));
            if(inBoard(x0+1,y0-2) && !containsTeam(x0+1,y0-2,team)) res.add(hashMove(x0,y0,x0+1,y0-2,-1));
            if(inBoard(x0-1,y0+2) && !containsTeam(x0-1,y0+2,team)) res.add(hashMove(x0,y0,x0-1,y0+2,-1));
            if(inBoard(x0-1,y0-2) && !containsTeam(x0-1,y0-2,team)) res.add(hashMove(x0,y0,x0-1,y0-2,-1));
        } else {
            if(inBoard(x0+1,y0) && !containsTeam(x0+1,y0,team)) res.add(hashMove(x0,y0,x0+1,y0,-1));
            if(inBoard(x0-1,y0) && !containsTeam(x0-1,y0,team)) res.add(hashMove(x0,y0,x0-1,y0,-1));
            if(inBoard(x0,y0+1) && !containsTeam(x0,y0+1,team)) res.add(hashMove(x0,y0,x0,y0+1,-1));
            if(inBoard(x0,y0-1) && !containsTeam(x0,y0-1,team)) res.add(hashMove(x0,y0,x0,y0-1,-1));
            
            if(inBoard(x0+1,y0+1) && !containsTeam(x0+1,y0+1,team)) res.add(hashMove(x0,y0,x0+1,y0+1,-1));
            if(inBoard(x0-1,y0+1) && !containsTeam(x0-1,y0+1,team)) res.add(hashMove(x0,y0,x0-1,y0+1,-1));
            if(inBoard(x0+1,y0-1) && !containsTeam(x0+1,y0-1,team)) res.add(hashMove(x0,y0,x0+1,y0-1,-1));
            if(inBoard(x0-1,y0-1) && !containsTeam(x0-1,y0-1,team)) res.add(hashMove(x0,y0,x0-1,y0-1,-1));
            
            // castle
            if(!state[x0][y0].hasMoved() && !isSquareAttacked(x0,y0,1-team)) {
                if(team == 0) {
                    // king side
                    if(containsTeam(7,7,team) && state[7][7].type == C.ROOK && !state[7][7].hasMoved()) { // rook in place?
                        if(!containsPiece(7,5) && !isSquareAttacked(7,5,1-team)) {
                            if(!containsPiece(7,6) && !isSquareAttacked(7,6,1-team)) {
                                res.add(hashMove(x0,y0,7,6,-1));
                            }
                        }
                    }
                    // queen's side
                    if(containsTeam(7,0,team) && state[7][0].type == C.ROOK && !state[7][0].hasMoved()) { // rook in place?
                        if(!containsPiece(7,3) && !isSquareAttacked(7,3,1-team)) {
                            if(!containsPiece(7,2) && !isSquareAttacked(7,2,1-team)) {
                                if(!containsPiece(7,1) && !isSquareAttacked(7,1,1-team)) {
                                    res.add(hashMove(x0,y0,7,2,-1));
                                }
                            }
                        }
                    }
                } else {
                    // king side
                    if(containsTeam(0,7,team) && state[0][7].type == C.ROOK && !state[0][7].hasMoved()) { // rook in place?
                        if(!containsPiece(0,5) && !isSquareAttacked(0,5,1-team)) {
                            if(!containsPiece(0,6) && !isSquareAttacked(0,6,1-team)) {
                                res.add(hashMove(x0,y0,0,6,-1));
                            }
                        }
                    }
                    // queen's side
                    if(containsTeam(0,0,team) && state[0][0].type == C.ROOK && !state[0][0].hasMoved()) { // rook in place?
                        if(!containsPiece(0,3) && !isSquareAttacked(0,3,1-team)) {
                            if(!containsPiece(0,2) && !isSquareAttacked(0,2,1-team)) {
                                if(!containsPiece(0,1) && !isSquareAttacked(0,1,1-team)) {
                                    res.add(hashMove(x0,y0,0,2,-1));
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // remove non legal (king in check)
        piece[][] initialState = new piece[8][8];
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                initialState[i][j] = new piece(state[i][j]);
            }
        }
        ArrayList<Integer> ans = new ArrayList<Integer>();
        for(int m: res) {
            int temp = m;
            int pro = temp%10;
            if(pro == 9) {
                pro = -1;
                temp++;
            }
            temp /= 10;
            int p1 = temp%64;
            int x1 = p1/8; int y1 = p1%8;
            move(x0,y0,x1,y1,pro);
            if(!kingEndangered(team)) ans.add(m);
            remHash(1-team);
            for(int i = 0; i < 8; i++) {
                for(int j = 0; j < 8; j++) {
                    state[i][j] = new piece(initialState[i][j]);
                }
            }
        }
        return ans;
    }
    boolean canMove(int x0, int y0, int x1, int y1) {
        if(!containsPiece(x0,y0)) return false;
        int type = state[x0][y0].type;
        int team = state[x0][y0].team;
        if(containsTeam(x1,y1,team)) return false;

        if(x0 == x1 && y0 == y1) return false;
        
        boolean res = false;
        if(type == C.PAWN) {
            int d = r2(x0,y0,x1,y1);
            if(d == 1) {
                if(containsPiece(x1,y1)) return false;
                else {
                    if(team == 0) res = x1 < x0;
                    else res = x1 > x0;
                }
            } else if(d == 4) {
                if(containsPiece(x1,y1)) return false;
                else if(containsPiece((x0+x1)/2,(y0+y1)/2)) return false;
                else {
                   if(team == 0) {
                       if(x0 != 6) return false;
                       else res = x1 < x0;
                   } else {
                       if(x0 != 1) return false;
                       else res = x1 > x0;
                   } 
                }
            } else if(d == 2) {
                if(isEnPassant(x0,y0,x1,y1)) res = true;
                else {
                    if(!containsTeam(x1,y1,1-team)) return false;
                    else {
                        if(team == 0) res = x1 < x0;
                        else res = x1 > x0;
                    }
                }
            } else {
                res = false;
            }
        } else if(type == C.ROOK) {
            if(x0 != x1 && y0 != y1) return false;
            else {
                int cx = x0; int cy = y0;
                while(cx != x1 || cy != y1) {
                    if(!(cx == x0 && cy == y0) && !(cx == x1 && cy == y1) && containsPiece(cx,cy)) return false;
                    if(cx < x1) cx++;
                    else if(cx > x1) cx--;
                    else if(cy < y1) cy++;
                    else cy--;
                }
                res = true;
            }
        } else if(type == C.KNIGHT) {
            int dx = x1 - x0;
            int dy = y1 - y0;
            int mx = Math.max(Math.abs(dx), Math.abs(dy));
            int mn = Math.min(Math.abs(dx), Math.abs(dy));
            res = (mx == 2 && mn == 1);
        } else if(type == C.BISHOP) {
            if(x0-y0 != x1-y1 && x0+y0 != x1+y1) return false;
            int cx = x0; int cy = y0;
            while(cx != x1 || cy != y1) {
                if(!(cx == x0 && cy == y0) && !(cx == x1 && cy == y1) && containsPiece(cx,cy)) return false;
                if(cx < x1 && cy < y1) {
                    cx++; cy++;
                } else if(cx < x1 && cy > y1) {
                    cx++; cy--;
                } else if(cx > x1 && cy < y1) {
                    cx--; cy++;
                } else {
                    cx--; cy--;
                }
            }
            res = true;
        } else if(type == C.QUEEN) {
            if(x0 == x1 || y0 == y1) {
                int cx = x0; int cy = y0;
                while(cx != x1 || cy != y1) {
                    if(!(cx == x0 && cy == y0) && !(cx == x1 && cy == y1) && containsPiece(cx,cy)) return false;
                    if(cx < x1) cx++;
                    else if(cx > x1) cx--;
                    else if(cy < y1) cy++;
                    else cy--;
                }
                res = true;
            } else if(x0-y0 == x1-y1 || x0+y0 == x1+y1) {
                int cx = x0; int cy = y0;
                while(cx != x1 || cy != y1) {
                    if(!(cx == x0 && cy == y0) && !(cx == x1 && cy == y1) && containsPiece(cx,cy)) return false;
                    if(cx < x1 && cy < y1) {
                        cx++; cy++;
                    } else if(cx < x1 && cy > y1) {
                        cx++; cy--;
                    } else if(cx > x1 && cy < y1) {
                        cx--; cy++;
                    } else {
                        cx--; cy--;
                    }
                }
                res = true;
            } else {
                res = false;
            }
        } else {
            if(r2(x0,y0,x1,y1) <= 2) res = true;
            else if(r2(x0,y0,x1,y1) == 4) { // check for castling
                // king is in check
                if(isSquareAttacked(x0,y0,1-team)) return false;
                // king has moved
                if(state[x0][y0].hasMoved()) return false;
                // rook has moved
                int rx, ry;
                if(!(x1 == 0 || x1 == 7)) return false;
                if(y1 == 2) {
                    rx = x0;
                    ry = 0;
                } else {
                    rx = x0;
                    ry = 7;
                }
                if(!containsTeam(rx,ry,team) || state[rx][ry].type != C.ROOK || state[rx][ry].hasMoved()) return false;
                // there are pieces between king and rook
                int cx = x0; int cy = y0;
                while(cx != rx && cy != ry) {
                    if(!(cx == x0 && cy == y0) && !(cx == rx && cy == ry) && containsPiece(cx,cy)) return false;
                    if(cy < ry) cy++;
                    else if(cy > ry) cy--;
                }
                // king has to move through checked square
                cx = x0; cy = y0;
                while(cx != x1 && cy != y1) {
                    if(!(cx == x0 && cy == y0) && !(cx == x1 && cy == y1) && isSquareAttacked(cx,cy,1-team)) return false;
                    if(cy < y1) cy++;
                    else if(cy > y1) cy--;
                }
                res = true;
            } else {
                return false;
            }
        }
        if(!res) return false;
        piece[][] initialState = new piece[8][8];
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                initialState[i][j] = new piece(state[i][j]);
            }
        }
        move(x0,y0,x1,y1,-1);
        if(kingEndangered(state[x1][y1].team)) res = false;
        remHash(1-team);
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                state[i][j] = new piece(initialState[i][j]);
            }
        }
        return res;
    }
    int r2(int x0, int y0, int x1, int y1) {
        return (x0-x1)*(x0-x1) + (y0-y1)*(y0-y1);
    }
    void move(int x0, int y0, int x1, int y1, int pro) {
        int team = state[x0][y0].team;
        if(pro != -1) state[x0][y0].type = pro;
        if(isEnPassant(x0,y0,x1,y1)) state[x0][y1] = new piece();
        state[x1][y1] = state[x0][y0];
        state[x1][y1].numMoves++;
        state[x0][y0] = new piece();
        // check for castle
        if(state[x1][y1].type == C.KING && r2(x0,y0,x1,y1) == 4) {
            int rx, ry, newy;
            if(y1 == 2) {
                rx = x0;
                ry = 0;
                newy = 3;
            } else {
                rx = x0;
                ry = 7;
                newy = 5;
            }
            state[x0][newy] = state[rx][ry];
            state[rx][ry] = new piece();
        }
        addHash(1-team);
    }
    String getPNG(piece p) {
        String filename;
        if(p.team == 0) filename = "w";
        else filename = "b";
        switch(p.type) {
            case C.PAWN: filename += "p"; break;
            case C.ROOK: filename += "r"; break;
            case C.KNIGHT: filename += "n"; break;
            case C.BISHOP: filename += "b"; break;
            case C.QUEEN: filename += "q"; break;
            case C.KING: filename += "k"; break;
        }
        filename += ".png";
        return filename;
    }
    String toChessPos(int x, int y) {
        String s = "abcdefgh";
        String res = s.substring(y,y+1);
        res += Integer.toString(8-x);
        return res;
    }
    String toChessMove(int x0, int y0, int x1, int y1) {
        return toChessPos(x0,y0)+"-"+toChessPos(x1,y1);
    }
    boolean endGame(int team) {
        int pieces = 0;
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                if(containsTeam(i,j,team) && state[i][j].type != C.PAWN) pieces++;
            }
        }
        return pieces <= 4;
    }
    void drawMove(int x0, int y0, int x1, int y1) {
        StdDraw.setPenColor(new Color(0,255,0,80));
        StdDraw.setPenRadius(0.03);
        StdDraw.line(toPD(x0,y0).F,toPD(x0,y0).S,toPD(x1,y1).F,toPD(x1,y1).S);
        StdDraw.setPenRadius(0.005);
    }
    void drawMove(int m) {
        if(m == -1) return;
        int temp = m;
        if(temp%10 == 9) temp++;
        temp /= 10;
        int p0 = temp/64; int p1 = temp%64;
        int x0 = p0/8; int y0 = p0%8;
        int x1 = p1/8; int y1 = p1%8;
        drawMove(x0,y0,x1,y1);
    }
    long getHash(int team) {
        long h = (team == 0) ? 0 : C.teamHash;
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) if(containsPiece(i,j)) {
                int k = state[i][j].type * (state[i][j].team+1) - 1;
                h ^= C.hashes[i][j][k];
            }
        }
        return h;
    }
    void addHash(int team) {
        long h = getHash(team);
        //System.out.println("added " + h + " (team " + team+")");
        if(seen.containsKey(h)) {
            seen.put(h, seen.get(h) + 1);
        } else {
            seen.put(h, 1);
        }
    }
    void remHash(int team) {
        long h = getHash(team);
        //System.out.println("removing " + h + " (team " + team+")");
        if(seen.containsKey(h)) {
            seen.put(h, seen.get(h) - 1);
            if(seen.get(h) == 0) seen.remove(h);
        } else {
            throw new RuntimeException();
        }
    }
}
