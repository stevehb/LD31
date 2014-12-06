package com.sblackwell.ld31;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.sblackwell.ld31.types.FontDrawable;
import com.sblackwell.ld31.types.GameState;

public class LD31Game extends ApplicationAdapter {
    private final int FONT_VIEWPORT_WIDTH = 900;
    private final int FONT_VIEWPORT_HEIGHT = 900;
    private final int WORLD_WIDTH = 100;
    private final int WORLD_HEIGHT = 100;

    private final String TITLE = "Snowman Ski Jump";

    private static final String VERT_SHADER =
                    "attribute vec4 a_position;\n" +
                    "attribute vec4 a_color;\n" +
                    "uniform mat4 u_projModelView;\n" +
                    "varying vec4 v_col;\n" +
                    "void main() {\n" +
                    "   gl_Position = u_projModelView * a_position;\n" +
                    "   v_col = a_color;\n" +
                    "   gl_PointSize = 1.0;\n" +
                    "}\n";
    private static final String FRAG_SHADER =
                    "#ifdef GL_ES\n" +
                    "precision mediump float;\n" +
                    "#endif\n" +
                    "varying vec4 v_col;\n" +
                    "void main() {\n" +
                    "   gl_FragColor = v_col;\n" +
                    "}";

    private Texture fontTexture;
    private BitmapFont font;
    private SpriteBatch batch;
    private ScreenViewport fontViewport, worldViewport;
    private ShapeRenderer shapeRenderer;

    private GameState state;
    private Array<FontDrawable> fontDrawables;


    @Override
	public void create () {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        fontTexture = new Texture(Gdx.files.internal("fonts/trench_64.png"));
        fontTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font = new BitmapFont(Gdx.files.internal("fonts/trench_64.fnt"), new TextureRegion(fontTexture), false);
        //font = new BitmapFont();
        fontViewport = new ScreenViewport(new OrthographicCamera(FONT_VIEWPORT_WIDTH, FONT_VIEWPORT_HEIGHT));
        worldViewport = new ScreenViewport(new OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT));
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        ImmediateModeRenderer20 imr = (ImmediateModeRenderer20) shapeRenderer.getRenderer();
        ShaderProgram shaderProgram = new ShaderProgram(VERT_SHADER, FRAG_SHADER);
        imr.setShader(shaderProgram);

        fontDrawables = new Array<FontDrawable>(false, 32, FontDrawable.class);

        // set up intro
        state = GameState.OUTRO_2_INTRO;
	}

    @Override
    public void resize(int width, int height) {
        fontViewport.setUnitsPerPixel((float) FONT_VIEWPORT_WIDTH / (float) width);
        fontViewport.update(width, height, true);
        worldViewport.setUnitsPerPixel((float) WORLD_WIDTH / (float) width);
        worldViewport.update(width, height, true);
    }

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.07f, 0.09f, 0.11f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        switch(state) {
        case INTRO:
            break;
        case INTRO_2_PLAY:
            break;
        case PLAY_DROP_BOTTOM:
            break;
        case PLAY_DROP_TOP:
            break;
        case PLAY_2_OUTRO:
            break;
        case OUTRO: {
            break;
        }
        case OUTRO_2_INTRO: {
            // clear old text
            Pools.freeAll(fontDrawables, true);

            // make title
            FontDrawable title = Pools.obtain(FontDrawable.class);
            title.text = TITLE;
            title.bounds.set(font.getBounds(title.text));
            title.pos.x = (FONT_VIEWPORT_WIDTH - title.bounds.width) / 2f;
            title.pos.y = (FONT_VIEWPORT_HEIGHT - title.bounds.height) * (5f / 6f);
            fontDrawables.add(title);

            // make instructions text


            state = GameState.INTRO;
            break;
        }
        }

        // render text
        fontViewport.getCamera().update();
        batch.setProjectionMatrix(fontViewport.getCamera().combined);
        batch.begin();
        for(int i = 0; i < fontDrawables.size; i++) {
            FontDrawable fd = fontDrawables.get(i);
            font.setScale(fd.scale);
            font.setColor(fd.tint);
            font.draw(batch, fd.text, fd.pos.x, fd.pos.y);
        }
        batch.end();

        worldViewport.getCamera().update();
        shapeRenderer.setProjectionMatrix(worldViewport.getCamera().combined);
        shapeRenderer.begin();
        shapeRenderer.identity();
        shapeRenderer.translate(50, 50, 0);
        shapeRenderer.rotate(0, 0, 1, (TimeUtils.millis() / 10L) % 360);
        shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.triangle(-10f, -10f, 0f, 10f, 10f, -10f, Color.RED, Color.WHITE, Color.BLUE);
        shapeRenderer.end();


        //shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        //shapeRenderer.identity();
        //shapeRenderer.translate(20, 12, 2);
        //shapeRenderer.rotate(0, 0, 1, 90);
        //shapeRenderer.rect(-20 / 2, -20 / 2, width, height);
        //shapeRenderer.end();

    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }

}
