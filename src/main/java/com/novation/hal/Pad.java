package com.novation.hal;

public class Pad {

    public enum PadType {
        play,
        rec,
        loop,
        click,
        page1,
        page2,
        page3,
        page4,
        up,
        down,
        left,
        right,
        session,
        user1,
        user2,
        user3
    }

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

    public int note() {
        return Pad.raw(raw.x, raw.y);
    }

    boolean isTopControl() {
        return raw.y == 0;
    }

    boolean isRightControl() {
        return raw.x == 8;
    }

    public boolean isControl() {
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

    public static int pad(int x, int y) {
        return array[y+1][x];
    }

    public static Pad controlPad(PadType type) {
        switch (type) {
            case play: return new Pad(8, 8);
            case rec: return new Pad(8, 7);
            case loop: return new Pad(8, 6);
            case click: return new Pad(8, 5);
            case page1: return new Pad(8, 1);
            case page2: return new Pad(8, 2);
            case page3: return new Pad(8, 3);
            case page4: return new Pad(8, 4);
            case up: return new Pad(0, 0);
            case down: return new Pad(1, 0);
            case left: return new Pad(2, 0);
            case right: return new Pad(3, 0);
            case session: return new Pad(4, 0);
            case user1: return new Pad(5, 0);
            case user2: return new Pad(6, 0);
            case user3: return new Pad(7, 0);
        }
        return null;
    }

    public static Pad regularPad(int x, int y) {
        return new Pad(x, y+1);
    }

    static class RawPosition {
        int x, y;

        RawPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
