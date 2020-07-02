package com.novation.LaunchpadMiniMK3;

public class Pad {

    private static final int [][] array = new int[][] {
        {91,92,93,94,95,96,97,98,99},
        {81,82,83,84,85,86,87,88,89},
        {71,72,73,74,75,76,77,78,79},
        {61,62,63,64,65,66,67,68,69},
        {51,52,53,54,55,56,57,58,59},
        {41,42,43,44,45,46,47,48,49},
        {31,32,33,34,35,36,37,38,39},
        {21,22,23,24,25,26,27,28,29},
        {11,12,13,14,15,16,17,18,19}
    };

    RawPosition raw;

    private Pad(int x, int y) {
        raw = new RawPosition(x, y);
    }

    int note() {
        return Pad.raw(raw.x, raw.y);
    }

    boolean isTopControl() {
        return raw.y == 0;
    }

    boolean isRightControl() {
        return raw.x == 8;
    }

    boolean isControl() {
        return isTopControl() || isRightControl();
    }

    static int raw(int x, int y) {
        return array[y][x];
    }

    static int topControl(int x) {
        return array[0][x];
    }

    static int rightControl(int y) {
        return array[y+1][8];
    }

    static int pad(int x, int y) {
        return array[y+1][x];
    }

    static class RawPosition {
        int x, y;

        RawPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
