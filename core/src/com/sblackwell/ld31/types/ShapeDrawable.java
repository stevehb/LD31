package com.sblackwell.ld31.types;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.Pool;

public class ShapeDrawable implements Pool.Poolable {
    public Color fill, outline;
    public FloatArray verts;
    public Vector2 pos;
    public float rot;

    public ShapeDrawable() {
        fill = new Color(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, 0f);
        outline = new Color(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, 0f);
        verts = new FloatArray(true, 32);
        pos = new Vector2();
        rot = 0f;
    }

    @Override
    public void reset() {
        fill.set(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, 0f);
        outline.set(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, 0f);
        verts.clear();
        pos.set(Vector2.Zero);
        rot = 0f;
    }
}
