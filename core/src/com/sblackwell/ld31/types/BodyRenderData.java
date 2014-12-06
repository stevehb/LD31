package com.sblackwell.ld31.types;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Pool;

public class BodyRenderData implements Pool.Poolable {
    public Color fill, outline;

    public BodyRenderData() {
        fill = new Color(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, 0f);
        outline = new Color(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, 0f);
    }

    @Override
    public void reset() {
        fill.set(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, 0f);
        outline.set(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, 0f);
    }
}
