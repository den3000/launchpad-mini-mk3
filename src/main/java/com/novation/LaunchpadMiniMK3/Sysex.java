package com.novation.LaunchpadMiniMK3;

public class Sysex {
    static final String PREFIX = "f0002029020d";
    static final String SUFFIX = "f7";
    static final String SET_DAW_MODE = sysex("1001");
    static final String CLEAR_DAW_MODE = sysex("12000000");
    static final String SESSION_LAYOUT = sysex("0000");
    static final String SESSION_DRUMS = sysex("0004");
    static final String SESSION_KEYS = sysex("0005");
    static final String SESSION_USER = sysex("0006");
    static final String PROGRAMMERS_LAYOUT = sysex("007f");
    static final String LIVE_MODE_ON = sysex("0e00");
    static final String EXIT = sysex("1000");

    static String sysex(String msg) {
       return PREFIX + msg + SUFFIX;
    }
}
