package com.novation.hal;

public enum TopRow {
    undefined(-1),
    // arrows
    up(91), down(92), left(93), right(94),
    // layout
    session(95), drums(96), keys(97), user(98);

    public static boolean isTopRow(Integer value) {
        return value >= up.value && value <= user.value;
    }

    public static boolean isArrow(Integer value) {
        return value >= up.value && value <= right.value;
    }

    public static TopRow from(Integer value) {
        if (value == up.getValue()) {
            return up;
        } else if (value == down.getValue()) {
            return down;
        } else if (value == left.getValue()) {
            return left;
        } else if (value == right.getValue()) {
            return right;
        } else if (value == session.getValue()) {
            return session;
        } else if (value == drums.getValue()) {
            return drums;
        } else if (value == keys.getValue()) {
            return keys;
        } else if (value == user.getValue()) {
            return user;
        }

        return undefined;
    }

    private final int value;

    TopRow(int value) {
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
            case left: return "left";
            case right: return "right";
            case session: return "session";
            case drums: return "drums";
            case keys: return "keys";
            case user: return "user";
            default: return "undefined";
        }
    }
}
