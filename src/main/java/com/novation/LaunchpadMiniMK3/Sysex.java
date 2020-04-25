package com.novation.LaunchpadMiniMK3;

public class Sysex {
    static final String PREFIX = "f0002029020d";
    static final String SUFFIX = "f7";
    static final String SET_DAW_MODE = sysex("1001");
    static final String CLEAR_DAW_MODE = sysex("12000000");
    static final String SESSION_LAYOUT = sysex("0000");
    static final String EXIT = sysex("1000");

    static String sysex(String msg) {
       return PREFIX + msg + SUFFIX;
    }
}
