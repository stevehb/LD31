package com.sblackwell.ld31.listeners;

import com.badlogic.gdx.InputAdapter;

public class InputSingleton extends InputAdapter {
    private static InputSingleton instance;
    public static InputSingleton get() {
        if(instance == null) { instance = new InputSingleton(); }
        return instance;
    }
    private InputSingleton() {
        keysPressed = 0;
    }

    public static int keysPressed;


    @Override
    public boolean keyDown(int keycode) {
        keysPressed++;
        return super.keyDown(keycode);
    }

    @Override
    public boolean keyUp(int keycode) {
        keysPressed--;
        return super.keyUp(keycode);
    }
}
