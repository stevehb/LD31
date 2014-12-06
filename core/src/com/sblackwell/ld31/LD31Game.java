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
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.sblackwell.ld31.types.BodyRenderData;
import com.sblackwell.ld31.types.FontDrawable;
import com.sblackwell.ld31.types.GameState;
import com.sblackwell.ld31.types.InputSingleton;
import com.sblackwell.ld31.utils.A;
import com.sblackwell.ld31.utils.L;

public class LD31Game extends ApplicationAdapter {
    private final int FONT_VIEWPORT_WIDTH = 900;
    private final int FONT_VIEWPORT_HEIGHT = 900;
    private final int WORLD_WIDTH = 100;
    private final int WORLD_HEIGHT = 100;
    private final int CIRCLE_SEG_COUNT = 40;

    private final int DROPPER_MIN_X = 0;
    private final int DROPPER_MAX_X = 25;

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
    private Array<Body> worldBodies;
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
        shapeRenderer.setAutoShapeType(false);
        ImmediateModeRenderer20 imr = (ImmediateModeRenderer20) shapeRenderer.getRenderer();
        ShaderProgram shaderProgram = new ShaderProgram(VERT_SHADER, FRAG_SHADER);
        imr.setShader(shaderProgram);

        physRenderer = new Box2DDebugRenderer(true, false, true, true, true, true);
        world = new World(new Vector2(0f, -10f), true);
        worldBodies = new Array<Body>();
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
            if(InputSingleton.keysPressed > 0) { state = GameState.INTRO_2_PLAY; }
            break;
        }
        case INTRO_2_PLAY: {
            L.l("state=" + state);
            clearFontDrawables(fontDrawables);
            world.getBodies(worldBodies);
            clearWorld(world, worldBodies);

            // create ski slope
            FloatArray verts = Pools.obtain(FloatArray.class);
            verts.clear();
            verts.addAll(
                    0f, 75f,
                    10f, 40f,
                    20f, 25f,
                    30f, 15f,
                    35f, 12.5f,
                    40f, 15f,
                    45f, 20f);
            verts.shrink();
            BodyDef slopeBodyDef = createBodyDef(BodyDef.BodyType.StaticBody, 0f, 0f, 0f, 0f);
            FixtureDef slopeFixtureDef = createFixtureDef(Shape.Type.Chain, verts, 0f, 0f, 0f);
            Body slopeBody = createBody(world, slopeBodyDef, slopeFixtureDef);
            BodyRenderData slopeData = Pools.obtain(BodyRenderData.class);
            slopeData.outline.set(Color.RED);
            slopeBody.setUserData(slopeData);

            // create arrow
            verts.clear();


            Pools.free(verts);

            state = GameState.PLAY;
            break;
        }
        case PLAY: {
            break;
        }
        case PLAY_2_OUTRO: {
            break;
        }
        case OUTRO: {
            break;
        }
        case OUTRO_2_INTRO: {
            L.l("state=" + state);
            clearFontDrawables(fontDrawables);
            world.getBodies(worldBodies);
            clearWorld(world, worldBodies);

            // make title
            FontDrawable title = createFontDrawable(TITLE, 1f, Color.RED, 0.5f, 5f / 6f);
            fontDrawables.add(title);

            // make instructions text
            FontDrawable inst1 = createFontDrawable(INSTRUCTIONS_1, 0.75f, Color.RED, 0.5f, 1f / 4f);
            fontDrawables.add(inst1);
            FontDrawable inst2 = createFontDrawable(INSTRUCTIONS_2, 0.75f, Color.RED, 0.5f, 1f / 4f);
            inst2.pos.y -= inst1.bounds.height * 1.5f;
            fontDrawables.add(inst2);
            FontDrawable inst3 = createFontDrawable(INSTRUCTIONS_3, 0.75f, Color.RED, 0.5f, 1f / 4f);
            inst3.pos.y -= inst1.bounds.height * 3f;
            fontDrawables.add(inst3);

            // test circle
            FloatArray vert = Pools.obtain(FloatArray.class);
            vert.clear();
            vert.add(20f);
            BodyDef circleBodyDef = createBodyDef(BodyDef.BodyType.StaticBody, 50f, 50f, 0, 0);
            FixtureDef circleFixtureDef = createFixtureDef(Shape.Type.Circle, vert, 0f, 0f, 0f);
            Body circle = createBody(world, circleBodyDef, circleFixtureDef);
            BodyRenderData circleData = Pools.obtain(BodyRenderData.class);
            circleData.outline.set(Color.RED);
            circle.setUserData(circleData);
            Pools.free(vert);

            state = GameState.INTRO;
            break;
        }
        }


        // start the draw
        Gdx.gl.glClearColor(0.07f, 0.09f, 0.11f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // render the shapes
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        worldViewport.getCamera().update();
        shapeRenderer.setProjectionMatrix(worldViewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        world.getBodies(worldBodies);
        for(int i = 0; i < worldBodies.size; i++) {
            Body b = worldBodies.get(i);
            if(b.isActive()) { drawBody(shapeRenderer, b); }
        }
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for(int i = 0; i < worldBodies.size; i++) {
            Body b = worldBodies.get(i);
            if(b.isActive()) { drawBody(shapeRenderer, b); }
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        /*
        shapeRenderer.identity();
        shapeRenderer.translate(50, 50, 0);
        shapeRenderer.rotate(0, 0, 1, (TimeUtils.millis() / 10L) % 360);
        shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.triangle(-10f, -10f, 0f, 10f, 10f, -10f, Color.RED, Color.WHITE, Color.BLUE);
        shapeRenderer.end();
        */

        // render text
        fontViewport.getCamera().update();
        batch.setProjectionMatrix(fontViewport.getCamera().combined);
        batch.begin();
        for(int i = 0; i < fontDrawables.size; i++) {
            drawFontDrawable(batch, font, fontDrawables.get(i));
        }
        batch.end();

        //physRenderer.render(world, worldViewport.getCamera().combined);

    }

    @Override
    public void dispose() {
        physRenderer.dispose();
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        fontTexture.dispose();
    }

    private void clearFontDrawables(Array<FontDrawable> array) {
        Pools.freeAll(array, true);
        array.clear();
    }

    private void clearWorld(World world, Array<Body> worldBodies) {
        for(int i = 0; i < worldBodies.size; i++) {
            Body b = worldBodies.get(i);
            Pools.free(b.getUserData());
            world.destroyBody(b);
        }
        worldBodies.clear();
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

    private void drawFontDrawable(SpriteBatch batch, BitmapFont font, FontDrawable drawable) {
        font.setScale(drawable.scale);
        font.setColor(drawable.tint);
        font.draw(batch, drawable.text, drawable.pos.x, drawable.pos.y);
    }

    private BodyDef createBodyDef(BodyDef.BodyType type, float x, float y, float linearDampening, float angularDampening) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = type;
        bodyDef.position.set(x, y);
        bodyDef.linearDamping = linearDampening;
        bodyDef.angularDamping = angularDampening;
        return bodyDef;
    }

    private FixtureDef createFixtureDef(Shape.Type type, FloatArray verts, float massDensity, float friction, float restitution) {
        Shape shape = null;
        A.NN(verts, "verts != null");
        switch(type) {
        case Circle: {
            shape = new CircleShape();
            CircleShape cs = (CircleShape) shape;
            A.T(verts.size == 1, "verts.size == 1");
            cs.setRadius(verts.get(0));
            break;
        }
        case Edge: {
            shape = new EdgeShape();
            EdgeShape es = (EdgeShape) shape;
            A.T(verts.size == 4, "verts.size == 4");
            es.set(verts.get(0), verts.get(1), verts.get(2), verts.get(3));
            break;
        }
        case Polygon: {
            shape = new PolygonShape();
            PolygonShape ps = (PolygonShape) shape;
            A.T(verts.size > 0, "verts.size > 0");
            ps.set(verts.items);
            break;
        }
        case Chain: {
            shape = new ChainShape();
            ChainShape cs = (ChainShape) shape;
            A.EVEN(verts.size, "verts.size is even");
            A.T(verts.size >= 4, "verts.size >= 4");
            cs.createChain(verts.items);
            break;
        }
        }
        A.NN(shape, "shape != null");

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = massDensity;
        fixtureDef.friction = friction;
        fixtureDef.restitution = restitution;

        return fixtureDef;
    }

    private Body createBody(World world, BodyDef bodyDef, FixtureDef fixtureDef) {
        Body body = world.createBody(bodyDef);
        body.createFixture(fixtureDef);
        fixtureDef.shape.dispose();
        return body;
    }

    private void drawBody(ShapeRenderer renderer, Body body) {
        Array<Fixture> fixtures = body.getFixtureList();
        Vector2 bodyPos = body.getPosition();
        BodyRenderData bodyData = (BodyRenderData) body.getUserData();
        Color clr = renderer.getCurrentType() == ShapeRenderer.ShapeType.Line ? bodyData.outline : bodyData.fill;
        renderer.setColor(clr);
        for(int i = 0; i < fixtures.size; i++) {
            Fixture f = fixtures.get(i);
            switch(f.getType()) {
            case Circle: {
                CircleShape circle = (CircleShape) f.getShape();
                renderer.circle(bodyPos.x, bodyPos.y, circle.getRadius(), CIRCLE_SEG_COUNT);
                break;
            }
            case Edge: {
                Vector2 v0 = Pools.obtain(Vector2.class).set(Vector2.Zero);
                Vector2 v1 = Pools.obtain(Vector2.class).set(Vector2.Zero);
                EdgeShape edge = (EdgeShape) f.getShape();
                if(edge.hasVertex0()) {
                    edge.getVertex0(v0);
                    edge.getVertex1(v1);
                    renderer.line(v0.add(bodyPos), v1.add(bodyPos));
                }
                edge.getVertex1(v0);
                edge.getVertex2(v1);
                renderer.line(v0.add(bodyPos), v1.add(bodyPos));
                if(edge.hasVertex3()) {
                    edge.getVertex2(v0);
                    edge.getVertex3(v1);
                    renderer.line(v0.add(bodyPos), v1.add(bodyPos));
                }
                Pools.free(v0);
                Pools.free(v1);
                break;
            }
            case Polygon: {
                Vector2 v0 = Pools.obtain(Vector2.class).set(Vector2.Zero);
                Vector2 v1 = Pools.obtain(Vector2.class).set(Vector2.Zero);
                PolygonShape poly = (PolygonShape) f.getShape();
                for(int j = 0; j < poly.getVertexCount() - 1; j++) {
                    poly.getVertex(j, v0);
                    poly.getVertex(j + 1, v1);
                    renderer.line(v0.add(bodyPos), v1.add(bodyPos));
                }
                Pools.free(v0);
                Pools.free(v1);
                break;
            }
            case Chain: {
                Vector2 v0 = Pools.obtain(Vector2.class).set(Vector2.Zero);
                Vector2 v1 = Pools.obtain(Vector2.class).set(Vector2.Zero);
                ChainShape chain = (ChainShape) f.getShape();
                for(int j = 0; j < chain.getVertexCount() - 1; j++) {
                    chain.getVertex(j, v0);
                    chain.getVertex(j + 1, v1);
                    renderer.line(v0.add(bodyPos), v1.add(bodyPos));
                }
                Pools.free(v0);
                Pools.free(v1);
                break;
            }
            }
        }
    }
}
