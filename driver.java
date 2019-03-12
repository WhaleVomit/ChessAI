import java.util.*;
public class driver {
    static boolean holdingPiece = false;
    static piece heldPiece;
    static int heldx, heldy;
    static board b;
    static long[] startTime = new long[100];
    static int timeLimit = 1000;
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
    static double euclidDist(double x0, double y0, double x1, double y1) {
        return Math.sqrt(Math.pow(x0-x1,2) + Math.pow(y0-y1, 2));
    }
    static void cMove(int team) {
        // choose your move (x0,y0) -> (x1,y1) here
        
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
        System.out.println("Started chess engine");
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
