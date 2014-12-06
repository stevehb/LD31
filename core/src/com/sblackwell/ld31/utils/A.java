package com.sblackwell.ld31.utils;

public class A {
    public static void NN(Object obj, String msg) {
        assert obj != null : msg;
    }

    public static void T(boolean val, String msg) {
        assert val : msg;
    }

    public static void F(boolean val, String msg) {
        assert !val : msg;
    }

    public static void EVEN(int val, String msg) {
        assert (val & 1) == 0 : msg;
    }
}
