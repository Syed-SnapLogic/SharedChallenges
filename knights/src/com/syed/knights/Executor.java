package com.syed.knights;

public class Executor {
    public static void main(String str[]) {
        int[][] chessBoard = new int[8][8];
        int count = 1;
        int times = 1;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                System.out.println("Attempting at (" + i + ", " + j + ")");
                cleanChessBoard(chessBoard);
                chessBoard[i][j] = 1;
                count = fillChessBoard(chessBoard, i, j, 2);
                if (count == 65) {
                    System.out.println("Found solution#" + (times++) + " starting at (" + i + ", " + j + ")");
                    printChessBoard(chessBoard);
                    System.out.println("\n\n");
                } else {
                    System.out.println("No solution for (" + i + ", " + j + ")\n\n");
                }
            }
        }
    }

    private static void cleanChessBoard(int[][] chessBoard) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                chessBoard[i][j] = 0;
            }
        }
    }

    private static int fillChessBoard(int[][] chessBoard, int i, int j, int count) {
        if ((i < 0 || i > 7) || (j < 0 || j > 7)) {
            return count;
        }

        for (int k = -2; k < 3; k++) {
            if (k == 0) {
                continue;
            }
            for (int l = -2; l < 3; l++) {
                if (l == 0) {
                    continue;
                }
                if (Math.abs(l) == Math.abs(k)) {
                    continue;
                }
                int m = i + k;
                int n = j + l;
                if ((m < 0 || m > 7) || (n < 0 || n > 7)) {
                    continue;
                }
                if (Math.abs(m - i) > 2 || Math.abs(n - j) > 2) {
                    continue;
                }
                if (chessBoard[m][n] != 0) {
                    continue;
                }
                chessBoard[m][n] = count;
                int newCount = fillChessBoard(chessBoard, m, n, count + 1);
                if (newCount == 65) {
                    return 65;
                } else {
                    chessBoard[m][n] = 0;
                }
            }
        }
        return count;
    }

    private static void printChessBoard(int[][] chessBoard) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                System.out.printf("%4d", chessBoard[i][j]);
            }
            System.out.println();
        }
    }
}
