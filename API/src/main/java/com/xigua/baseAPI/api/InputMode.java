package com.xigua.baseAPI.api;

public enum InputMode {
    UNDEFINED,
    MOUSE,
    TOUCH,
    GAMEPAD,
    MOTION_CONTROLLER;

    private static final InputMode[] VALUES = values();

    public static InputMode from(int id) {
        return VALUES[id];
    }
}
