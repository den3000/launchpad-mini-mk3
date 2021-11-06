package com.novation.hal;

public enum PressState {
    unknown(-1), up(0), down(127);

    public static PressState from(Integer value) {
        if (value == up.getValue()) {
            return up;
        } else if (value == down.getValue()) {
            return down;
        }

        return unknown;
    }

    private final int value;

    PressState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        switch (this) {
            case up: return "up";
            case down: return "down";
            default: return "undefined";
        }
    }
}

