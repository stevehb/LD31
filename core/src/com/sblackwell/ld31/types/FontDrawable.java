package com.sblackwell.ld31.types;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

public class FontDrawable implements Pool.Poolable {
    public String text;
    public float scale;
    public Color tint;
    public BitmapFont.TextBounds bounds;
    public Vector2 pos;

    public FontDrawable() {
        text = null;
        scale = 1f;
        tint = new Color(Color.WHITE);
        bounds = new BitmapFont.TextBounds();
        pos = new Vector2();
    }

    @Override
    public void reset() {
        text = null;
        scale = 1f;
        tint.set(Color.WHITE);
        bounds.height = 0;
        bounds.width = 0;
        pos.set(Vector2.Zero);
    }
}
