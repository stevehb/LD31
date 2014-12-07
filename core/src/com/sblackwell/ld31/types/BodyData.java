package com.sblackwell.ld31.types;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Pool;

public class BodyData implements Pool.Poolable {
    public EntityType type;
    public Color fillColor, outlineColor;

    public BodyData() {
        fillColor = new Color(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, 0f);
        outlineColor = new Color(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, 0f);
    }

    @Override
    public void reset() {
        fillColor.set(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, 0f);
        outlineColor.set(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, 0f);
    }
}
