package com.novation.hal;

public enum PrerollType {
    unknown(null), none("none"), oneBar("one_bar"), twoBars("two_bars"), fourBars("four_bars");

    public static PrerollType from(String value) {
        if (value.equals(none.value)) {
            return none;
        } else if (value.equals(oneBar.value)) {
            return oneBar;
        } else if (value.equals(twoBars.value)) {
            return twoBars;
        } else if (value.equals(fourBars.value)) {
            return fourBars;
        }

        return unknown;
    }

    private final String value;

    PrerollType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
