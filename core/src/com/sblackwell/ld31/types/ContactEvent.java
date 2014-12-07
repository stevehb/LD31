package com.sblackwell.ld31.types;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Pool;

public class ContactEvent implements Pool.Poolable{
    public static enum ContactEventType { BEGIN, END }

    public ContactEventType type;
    public Body a, b;

    @Override
    public void reset() {
        type = null;
        a = null;
        b = null;
    }
}
