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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.sblackwell.ld31.types.FontDrawable;
import com.sblackwell.ld31.types.GameState;
import com.sblackwell.ld31.types.InputSingleton;
import com.sblackwell.ld31.utils.L;

public class LD31Game extends ApplicationAdapter {
    private final int FONT_VIEWPORT_WIDTH = 900;
    private final int FONT_VIEWPORT_HEIGHT = 900;
    private final int WORLD_WIDTH = 100;
    private final int WORLD_HEIGHT = 100;

    private final String TITLE = "Snowman Ski Jump";
    private final String INSTRUCTIONS_1 = "left & right to position";
    private final String INSTRUCTIONS_2 = "down to drop";
    private final String INSTRUCTIONS_3 = "any key to start";

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
    private Box2DDebugRenderer physRenderer;

    private World world;
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

        physRenderer = new Box2DDebugRenderer(true, false, true, true, true, true);
        world = new World(new Vector2(0f, -10f), true);
        fontDrawables = new Array<FontDrawable>(false, 32, FontDrawable.class);

        Gdx.input.setInputProcessor(InputSingleton.get());

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


        // update world & state
        world.step(1f/60f, 8, 6);
        switch(state) {
        case INTRO: {
            if(InputSingleton.keysPressed > 0) {
                L.l("move to next stage: " + InputSingleton.keysPressed);
            }
            break;
        }
        case INTRO_2_PLAY: {
            break;
        }
        case PLAY_DROP_BOTTOM: {
            break;
        }
        case PLAY_DROP_TOP: {
            break;
        }
        case PLAY_2_OUTRO: {
            break;
        }
        case OUTRO: {
            break;
        }
        case OUTRO_2_INTRO: {
            // clear old text
            Pools.freeAll(fontDrawables, true);

            // make title
            FontDrawable title = createFontDrawable(TITLE, 1f, Color.WHITE, 0.5f, 5f / 6f);
            fontDrawables.add(title);

            // make instructions text
            FontDrawable inst1 = createFontDrawable(INSTRUCTIONS_1, 0.75f, Color.WHITE, 0.5f, 1f / 4f);
            fontDrawables.add(inst1);
            FontDrawable inst2 = createFontDrawable(INSTRUCTIONS_2, 0.75f, Color.WHITE, 0.5f, 1f / 4f);
            inst2.pos.y -= inst1.bounds.height * 1.5f;
            fontDrawables.add(inst2);
            FontDrawable inst3 = createFontDrawable(INSTRUCTIONS_3, 0.75f, Color.WHITE, 0.5f, 1f / 4f);
            inst3.pos.y -= inst1.bounds.height * 3f;
            fontDrawables.add(inst3);

            state = GameState.INTRO;
            break;
        }
        }


        // render it all
        Gdx.gl.glClearColor(0.07f, 0.09f, 0.11f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        worldViewport.getCamera().update();
        shapeRenderer.setProjectionMatrix(worldViewport.getCamera().combined);
        shapeRenderer.begin();
        shapeRenderer.identity();
        shapeRenderer.translate(50, 50, 0);
        shapeRenderer.rotate(0, 0, 1, (TimeUtils.millis() / 10L) % 360);
        shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.triangle(-10f, -10f, 0f, 10f, 10f, -10f, Color.RED, Color.WHITE, Color.BLUE);
        shapeRenderer.end();

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

        physRenderer.render(world, worldViewport.getCamera().combined);

        //shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        //shapeRenderer.identity();
        //shapeRenderer.translate(20, 12, 2);
        //shapeRenderer.rotate(0, 0, 1, 90);
        //shapeRenderer.rect(-20 / 2, -20 / 2, width, height);
        //shapeRenderer.end();

    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        fontTexture.dispose();
    }

    private FontDrawable createFontDrawable(String text, float scale, Color tint, float posX, float posY) {
        FontDrawable fd = Pools.obtain(FontDrawable.class);
        fd.text = text;
        fd.scale = scale;
        fd.bounds.set(font.getBounds(fd.text));
        fd.bounds.width *= scale;
        fd.bounds.height *= scale;
        fd.tint.set(tint);
        fd.pos.x = (FONT_VIEWPORT_WIDTH - fd.bounds.width) * posX;
        fd.pos.y = (FONT_VIEWPORT_HEIGHT - fd.bounds.height) * posY;
        return fd;
    }
}
