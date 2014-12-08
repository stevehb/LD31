package com.sblackwell.ld31;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.badlogic.gdx.math.MathUtils;
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
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.sblackwell.ld31.listeners.ContactSingleton;
import com.sblackwell.ld31.listeners.InputSingleton;
import com.sblackwell.ld31.types.BodyData;
import com.sblackwell.ld31.types.ContactEvent;
import com.sblackwell.ld31.types.EntityType;
import com.sblackwell.ld31.types.FontDrawable;
import com.sblackwell.ld31.types.GameState;
import com.sblackwell.ld31.utils.A;
import com.sblackwell.ld31.utils.L;

public class LD31Game extends ApplicationAdapter {
    private final int FONT_VIEWPORT_WIDTH = 900;
    private final int FONT_VIEWPORT_HEIGHT = 900;
    private final int WORLD_WIDTH = 100;
    private final int WORLD_HEIGHT = 100;
    private final int CIRCLE_SEG_COUNT = 40;

    private final int DROPPER_MIN_X = 2;
    private final int DROPPER_MAX_X = 25;
    private final float DROPPER_DELTA_X = 0.5f;

    private final int PLATFORM_MIN_X = 55;
    private final int PLATFORM_MAX_X = 90;

    private final float TOP_RADIUS = 2.5f;
    private final float BOTTOM_RADIUS = 5.0f;

    private final float BOTTOM_START_Y = WORLD_HEIGHT - 10f - BOTTOM_RADIUS;
    private final float TOP_START_Y = WORLD_HEIGHT - 10f - TOP_RADIUS;

    private final int FALLING_ITEM_COUNT = 10;

    private final String TITLE = "Snowman Ski Jump";
    private final String INSTRUCTIONS_0 = "Build a snowman!";
    private final String INSTRUCTIONS_1 = "left & right to position";
    private final String INSTRUCTIONS_2 = "down to drop";
    private final String INSTRUCTIONS_3 = "any key to start";

    private final Color BACKGROUND = Color.valueOf("073642FF");


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

    private final float SCORE_PLATFORM_PLACEMENT_MAX = 100f;
    private final float SCORE_CONNECTION_ANGLE_MAX = 150f;
    private final float SCORE_TOP_CONNECTED_MAX = 250f;
    private final float SCORE_STICK_HANDS_MAX = 100f;
    private final float SCORE_CHARCOAL_EYES_MAX = 100f;
    private final float SCORE_CARROT_NOSE_MAX = 100f;
    private final float SCORE_TOP_HAT_MAX = 200f;
    private final float SCORE_TOTAL_MAX =
            SCORE_PLATFORM_PLACEMENT_MAX + SCORE_CONNECTION_ANGLE_MAX + SCORE_TOP_CONNECTED_MAX +
            SCORE_STICK_HANDS_MAX + SCORE_CHARCOAL_EYES_MAX + SCORE_CARROT_NOSE_MAX + SCORE_TOP_HAT_MAX;

    private class ScoreKeep {
        public float placementOnPlatform;
        public boolean topConnectedToBottom;
        public float angleOfConnection;
        public boolean hasStickHands;
        public boolean hasCharcoalEyes;
        public boolean hasCarrotNose;
        public boolean hasTopHat;
        public float total;

        @Override
        public String toString() {
            return "ScoreKeep{" +
                    "\n  placementOnPlatform=" + placementOnPlatform +
                    "\n  topConnectedToBottom=" + topConnectedToBottom +
                    "\n  angleOfConnection=" + angleOfConnection +
                    "\n  hasStickHands=" + hasStickHands +
                    "\n  hasCharcoalEyes=" + hasCharcoalEyes +
                    "\n  hasCarrotNose=" + hasCarrotNose +
                    "\n  hasTopHat=" + hasTopHat +
                    "\n  total=" + total +
                    "\n}";
        }
    }

    private Texture fontTexture;
    private BitmapFont font;
    private SpriteBatch batch;
    private ScreenViewport fontViewport, worldViewport;
    private ShapeRenderer shapeRenderer;
    private Box2DDebugRenderer physRenderer;

    private FloatArray vertices1, vertices2;

    private World world;
    private Array<Body> worldBodies;
    private GameState state;
    private ObjectMap<EntityType, Body> namedBodies;
    private Array<FontDrawable> fontDrawables;

    private ScoreKeep scoreKeep;

    @Override
	public void create () {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        MathUtils.sin(1f);

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

        vertices1 = new FloatArray();
        vertices2 = new FloatArray();

        physRenderer = new Box2DDebugRenderer(true, false, true, true, true, true);
        world = new World(new Vector2(0f, -100f), true);
        world.setContactListener(ContactSingleton.get());
        world.setContactFilter(ContactSingleton.get());
        worldBodies = new Array<Body>();
        namedBodies = new ObjectMap<EntityType, Body>();
        fontDrawables = new Array<FontDrawable>(false, 32, FontDrawable.class);

        scoreKeep = new ScoreKeep();

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
        if(Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            state = GameState.INTRO_2_PLAY;
        }

        world.step(1f/60f, 8, 6);

        // handle dropper movement
        {
            Body dropper = namedBodies.get(EntityType.DROPPER);
            if(dropper != null) {
                Vector2 dropperPos = dropper.getPosition();
                if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                    dropperPos.x -= DROPPER_DELTA_X;
                } else if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                    dropperPos.x += DROPPER_DELTA_X;
                }
                dropperPos.x = MathUtils.clamp(dropperPos.x, DROPPER_MIN_X, DROPPER_MAX_X);
                dropper.setTransform(dropperPos, dropper.getAngle());
            }
        }

        // remake fallen items
        {
            world.getBodies(worldBodies);
            for(int i = 0; i < worldBodies.size; i++) {
                Body b = worldBodies.get(i);
                Vector2 pos = b.getPosition();
                BodyData d = (BodyData) b.getUserData();
                boolean offScreen = pos.x < 0f || pos.y < 0f || pos.x > WORLD_WIDTH;
                boolean isItem = d.type == EntityType.STICK_HANDS || d.type == EntityType.CHARCOAL_EYES || d.type == EntityType.CARROT_NOSE || d.type == EntityType.TOP_HAT;
                if(isItem && offScreen) {
                    Pools.free(d);
                    world.destroyBody(b);
                    int randIdx = MathUtils.random(5, 8);
                    EntityType type = EntityType.array[randIdx];
                    b = createItem(world, type);
                    worldBodies.set(i, b);
                }
            }
        }


        // update world & state
        switch(state) {
        case INTRO: {
            if(InputSingleton.keysPressed > 0) { state = GameState.INTRO_2_PLAY; }
            break;
        }

        case INTRO_2_PLAY: {
            L.l("state=" + state);
            clearPoolArray(fontDrawables);
            world.getBodies(worldBodies);
            clearWorld(world, worldBodies);

            scoreKeep.placementOnPlatform = 0f;
            scoreKeep.topConnectedToBottom = false;
            scoreKeep.angleOfConnection = 0f;
            scoreKeep.hasStickHands = false;
            scoreKeep.hasCharcoalEyes = false;
            scoreKeep.hasCarrotNose = false;
            scoreKeep.hasTopHat = false;
            scoreKeep.total = 0f;

            // create ski slope
            {
                vertices1.clear();
                final float xCenter = 25f;
                for(float x = 0f; x <= 37f; x += 1f) {
                    float y = 0.085f *((x - xCenter) * (x - xCenter)) + 5f;
                    vertices1.add(x);
                    vertices1.add(y);
                }
                vertices1.shrink();
                BodyDef bd = createBodyDef(BodyDef.BodyType.StaticBody, 0f, 0f, 0f, 0f);
                FixtureDef fd = createFixtureDef(Shape.Type.Chain, vertices1, 0f, 0f, 0f, EntityType.SLOPE.groupIdx);
                Body b = createBody(world, bd, fd);
                BodyData data = Pools.obtain(BodyData.class);
                data.outlineColor.set(Color.RED);
                data.type = EntityType.SLOPE;
                b.setUserData(data);
                namedBodies.put(EntityType.SLOPE, b);
            }

            // create platform
            {
                vertices1.clear();
                vertices1.addAll(PLATFORM_MIN_X - 5f, 0f, PLATFORM_MIN_X, 10f, PLATFORM_MAX_X, 10f, PLATFORM_MAX_X + 5f, 0f);
                vertices1.shrink();
                BodyDef bd = createBodyDef(BodyDef.BodyType.StaticBody, 0f, 0f, 0f, 0f);
                FixtureDef fd = createFixtureDef(Shape.Type.Chain, vertices1, 0f, 0f, 0f, EntityType.PLATFORM.groupIdx);
                Body b = createBody(world, bd, fd);
                BodyData data = Pools.obtain(BodyData.class);
                data.outlineColor.set(Color.RED);
                data.type = EntityType.PLATFORM;
                b.setUserData(data);
                namedBodies.put(EntityType.PLATFORM, b);
            }

            // create dropper
            {
                vertices1.clear();
                vertices1.addAll(0f, 0f, 0f, -5f);
                vertices1.shrink();
                FixtureDef fd2 = createFixtureDef(Shape.Type.Chain, vertices1, 0f, 0f, 0f, EntityType.DROPPER.groupIdx);

                vertices1.clear();
                vertices1.addAll(-3f, -7f, 0f, -5f, 3f, -7f);
                vertices1.shrink();
                FixtureDef fd1 = createFixtureDef(Shape.Type.Chain, vertices1, 0f, 0f, 0f, EntityType.DROPPER.groupIdx);

                BodyDef bd = createBodyDef(BodyDef.BodyType.KinematicBody, (DROPPER_MAX_X - DROPPER_MIN_X) / 2f, WORLD_HEIGHT, 10f, 0f);
                Body b = createBody(world, bd, fd1);
                b.createFixture(fd2);
                fd2.shape.dispose();
                BodyData data = Pools.obtain(BodyData.class);
                data.outlineColor.set(Color.OLIVE);
                data.type = EntityType.DROPPER;
                b.setUserData(data);
                namedBodies.put(EntityType.DROPPER, b);
            }

            // create bottom snowball
            {
                Body dropper = namedBodies.get(EntityType.DROPPER);
                vertices1.clear();
                vertices1.add(BOTTOM_RADIUS);
                vertices1.shrink();
                BodyDef bd = createBodyDef(BodyDef.BodyType.DynamicBody, dropper.getPosition().x, BOTTOM_START_Y, 0.13f, 0f);
                bd.gravityScale = 0f;
                FixtureDef fd = createFixtureDef(Shape.Type.Circle, vertices1, 1f, 0f, 0f, EntityType.BOTTOM_BALL.groupIdx);

                Body b = createBody(world, bd, fd);
                BodyData data = Pools.obtain(BodyData.class);
                data.fillColor.set(0.8f, 0.8f, 0.8f, 1f);
                data.outlineColor.set(1f, 1f, 1f, 1f);
                data.type = EntityType.BOTTOM_BALL;
                b.setUserData(data);
                namedBodies.put(EntityType.BOTTOM_BALL, b);
            }

            // create top snowball
            {
                vertices1.clear();
                vertices1.add(TOP_RADIUS);
                vertices1.shrink();
                BodyDef bd = createBodyDef(BodyDef.BodyType.DynamicBody, 0f, -2f * TOP_RADIUS, 0f, 0f);
                bd.gravityScale = 0f;
                FixtureDef fd = createFixtureDef(Shape.Type.Circle, vertices1, 1f, 0f, 0f, EntityType.TOP_BALL.groupIdx);
                Body b = createBody(world, bd, fd);
                BodyData data = Pools.obtain(BodyData.class);
                data.fillColor.set(0.8f, 0.8f, 0.8f, 1f);
                data.outlineColor.set(1f, 1f, 1f, 1f);
                data.type = EntityType.TOP_BALL;
                b.setUserData(data);
                namedBodies.put(EntityType.TOP_BALL, b);
            }

            // create floating accessories
            {
                for(int i = 0; i < FALLING_ITEM_COUNT; i++) {
                    int randIdx = MathUtils.random(5, 8);
                    EntityType type = EntityType.array[randIdx];
                    Body item = createItem(world, type);
                    item.setTransform(item.getPosition().x, MathUtils.random(0f, WORLD_HEIGHT), item.getAngle());
                }
            }

            state = GameState.PLAY_BOTTOM_HELD;
            break;
        }

        case PLAY_BOTTOM_HELD: {
            Body dropper = namedBodies.get(EntityType.DROPPER);
            A.NN(dropper, "dropper != null");
            Vector2 dropperPos = dropper.getPosition();
            Body ball = namedBodies.get(EntityType.BOTTOM_BALL);
            A.NN(ball, "ball != null");

            // move ball with dropper
            ball.setTransform(dropperPos.x, BOTTOM_START_Y, ball.getAngle());

            // release ball if down pressed
            if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                L.l("releasing the ball");
                ball.setGravityScale(1f);
                ball.setAwake(true);
                clearPoolArray(ContactSingleton.get().queue);
                state = GameState.PLAY_BOTTOM_DROPPED;
            }
            break;
        }

        case PLAY_BOTTOM_DROPPED: {
            // process contact events
            Array<ContactEvent> q = ContactSingleton.get().queue;
            for(int i = 0; i < q.size; i++) {
                ContactEvent e = q.get(i);

                // check for contact with falling item
                {
                    Body ball = getBodyOfType(e, EntityType.BOTTOM_BALL);
                    Body stick = getBodyOfType(e, EntityType.STICK_HANDS);
                    if(ball != null && stick != null) {
                        BodyData otherData = (BodyData) stick.getUserData();
                        switch(otherData.type) {
                        case STICK_HANDS:
                            scoreKeep.hasStickHands = true;
                            break;
                        }
                        Pools.free(otherData);
                        world.destroyBody(stick);
                    }
                }

                // check for contact with platform
                {
                    Body platform = getBodyOfType(e, EntityType.PLATFORM);
                    if(platform != null) {
                        Body ball = getBodyOfType(e, EntityType.BOTTOM_BALL);
                        float ballX = ball.getPosition().x;
                        L.l("ball landed at " + ballX);
                        ballX = MathUtils.round(ballX);
                        if(ballX >= PLATFORM_MIN_X && ballX <= PLATFORM_MAX_X) {
                            ball.setLinearVelocity(0f, 0f);
                            ball.setGravityScale(0f);
                            ball.setType(BodyDef.BodyType.StaticBody);
                            scoreKeep.placementOnPlatform = calcPercentFromCenter(ballX, PLATFORM_MIN_X, PLATFORM_MAX_X);
                            L.l("placementOnPlatform=" + scoreKeep.placementOnPlatform);
                        } else {
                            scoreKeep.placementOnPlatform = 0f;
                        }

                        // add display hands
                        if(scoreKeep.hasStickHands) {
                            Body hands = createItem(world, EntityType.DISPLAY_HANDS);
                            hands.setType(BodyDef.BodyType.StaticBody);
                            hands.setTransform(ball.getPosition(), hands.getAngle());
                        }

                        state = GameState.PLAY_BOTTOM_2_TOP;
                        break;
                    }
                }
            }
            clearPoolArray(q);

            // detect if ball falls over side or is inactive
            Body ball = namedBodies.get(EntityType.BOTTOM_BALL);
            if(ball.getPosition().y < 0 || (ball.getPosition().x < PLATFORM_MIN_X && ball.getLinearVelocity().isZero(0.1f))) {
                state = GameState.PLAY_BOTTOM_2_TOP;
            }

            break;
        }

        case PLAY_BOTTOM_2_TOP: {
            L.l("state=" + state);
            Body dropper = namedBodies.get(EntityType.DROPPER);
            A.NN(dropper, "dropper != null");
            Body ball = namedBodies.get(EntityType.TOP_BALL);
            A.NN(ball, "ball != null");
            ball.setTransform(dropper.getPosition().x, TOP_START_Y, ball.getAngle());
            state = GameState.PLAY_TOP_HELD;
            break;
        }

        case PLAY_TOP_HELD: {
            Body dropper = namedBodies.get(EntityType.DROPPER);
            A.NN(dropper, "dropper != null");
            Vector2 dropperPos = dropper.getPosition();
            Body ball = namedBodies.get(EntityType.TOP_BALL);
            A.NN(ball, "ball != null");

            // move ball with dropper
            ball.setTransform(dropperPos.x, TOP_START_Y, ball.getAngle());

            // release ball if down pressed
            if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                L.l("releasing the ball");
                ball.setGravityScale(1f);
                ball.setAwake(true);
                clearPoolArray(ContactSingleton.get().queue);
                state = GameState.PLAY_TOP_DROPPED;
            }
            break;
        }

        case PLAY_TOP_DROPPED: {
            // process contact events
            Array<ContactEvent> q = ContactSingleton.get().queue;
            for(int i = 0; i < q.size; i++) {
                ContactEvent e = q.get(i);

                // check for contact with falling item
                {
                    Body ball = getBodyOfType(e, EntityType.TOP_BALL);
                    Body charcoal = getBodyOfType(e, EntityType.CHARCOAL_EYES);
                    Body carrot = getBodyOfType(e, EntityType.CARROT_NOSE);
                    Body hat = getBodyOfType(e, EntityType.TOP_HAT);
                    Body other = (charcoal != null) ? charcoal : (carrot != null) ? carrot : hat;
                    if(ball != null && other != null) {
                        BodyData otherData = (BodyData) other.getUserData();
                        switch(otherData.type) {
                        case CHARCOAL_EYES:
                            scoreKeep.hasCharcoalEyes = true;
                            break;
                        case CARROT_NOSE:
                            scoreKeep.hasCarrotNose = true;
                            break;
                        case TOP_HAT:
                            scoreKeep.hasTopHat = true;
                            break;
                        }
                        Pools.free(otherData);
                        world.destroyBody(other);
                    }
                }

                // check for contact with platform or bottom ball
                {
                    Body btmBall = getBodyOfType(e, EntityType.BOTTOM_BALL);
                    Body platform = getBodyOfType(e, EntityType.PLATFORM);
                    Body topBall = getBodyOfType(e, EntityType.TOP_BALL);
                    if(topBall != null && (btmBall != null || platform != null)) {
                        if(btmBall != null) {
                            topBall.setLinearVelocity(0f, 0f);
                            topBall.setGravityScale(0f);
                            topBall.setType(BodyDef.BodyType.StaticBody);
                            scoreKeep.topConnectedToBottom = true;
                            float dX = Math.abs(btmBall.getPosition().x - topBall.getPosition().x);
                            float dY = Math.abs(btmBall.getPosition().y - topBall.getPosition().y);
                            float angle = MathUtils.atan2(dY, dX);
                            scoreKeep.angleOfConnection = angle;
                            L.l("top hit btm: angle=" + angle * MathUtils.radiansToDegrees);
                        } else {
                            float ballX = topBall.getPosition().x;
                            L.l("topBall landed at " + ballX);
                            ballX = MathUtils.round(ballX);
                            if(ballX >= PLATFORM_MIN_X && ballX <= PLATFORM_MAX_X) {
                                topBall.setLinearVelocity(0f, 0f);
                                topBall.setGravityScale(0f);
                                topBall.setType(BodyDef.BodyType.StaticBody);
                                scoreKeep.topConnectedToBottom = false;
                                scoreKeep.angleOfConnection = Float.NaN;
                            }
                        }


                        // add display items
                        if(scoreKeep.hasCharcoalEyes) {
                            Body eyes = createItem(world, EntityType.DISPLAY_EYES);
                            eyes.setType(BodyDef.BodyType.StaticBody);
                            eyes.setTransform(topBall.getPosition(), eyes.getAngle());
                        }
                        if(scoreKeep.hasCarrotNose) {
                            Body nose = createItem(world, EntityType.DISPLAY_NOSE);
                            nose.setType(BodyDef.BodyType.StaticBody);
                            nose.setTransform(topBall.getPosition(), nose.getAngle());
                        }
                        if(scoreKeep.hasTopHat) {
                            Body hat = createItem(world, EntityType.DISPLAY_HAT);
                            hat.setType(BodyDef.BodyType.StaticBody);
                            hat.setTransform(topBall.getPosition(), hat.getAngle());
                        }


                        state = GameState.PLAY_2_OUTRO;
                        break;
                    }
                }
            }
            clearPoolArray(q);

            // detect if ball falls over side or is inactive
            Body ball = namedBodies.get(EntityType.TOP_BALL);
            if(ball.getPosition().y < 0 || (ball.getPosition().x < PLATFORM_MIN_X && ball.getLinearVelocity().isZero(0.1f))) {
                state = GameState.PLAY_2_OUTRO;
            }

            break;
        }

        case PLAY_2_OUTRO: {
            L.l("state=" + state);

            // calc scores
            float platformPlacement = SCORE_PLATFORM_PLACEMENT_MAX * scoreKeep.placementOnPlatform;
            float snowballConnected = SCORE_TOP_CONNECTED_MAX * ((scoreKeep.topConnectedToBottom) ? 1f : 0f);
            float snowballAngle = scoreKeep.topConnectedToBottom ? SCORE_CONNECTION_ANGLE_MAX * calcPercentFromCenter(scoreKeep.angleOfConnection, 0, MathUtils.PI) : 0f;
            float stickHands = SCORE_STICK_HANDS_MAX * ((scoreKeep.hasStickHands) ? 1f : 0f);
            float charcoalEyes = SCORE_CHARCOAL_EYES_MAX * ((scoreKeep.hasCharcoalEyes) ? 1f : 0f);
            float carrotNose = SCORE_CARROT_NOSE_MAX * ((scoreKeep.hasCarrotNose) ? 1f : 0f);
            float hat = SCORE_TOP_HAT_MAX * ((scoreKeep.hasTopHat) ? 1f : 0f);
            scoreKeep.total = platformPlacement + snowballConnected + snowballAngle + stickHands + charcoalEyes + carrotNose + hat;

            // set up score diaplays
            String str = "Platform Placement: " + MathUtils.round(platformPlacement) + " / " + (int)SCORE_PLATFORM_PLACEMENT_MAX;
            FontDrawable fd1 = createFontDrawable(font, str, 0.75f, Color.RED, 0.5f, 7f/8f);
            fontDrawables.add(fd1);
            str = "Snowballs Connected: " + MathUtils.round(snowballConnected) + " / " + (int)SCORE_TOP_CONNECTED_MAX;
            FontDrawable fd2 = createFontDrawable(font, str, 0.75f, Color.RED, fd1.pos.x, 6.6f / 8f);
            fontDrawables.add(fd2);
            str = "Snowball Angle: " + MathUtils.round(snowballAngle) + " / " + (int)SCORE_CONNECTION_ANGLE_MAX;
            FontDrawable fd3 = createFontDrawable(font, str, 0.75f, Color.RED, fd1.pos.x, 6.2f / 8f);
            fontDrawables.add(fd3);
            str = "Stick Hands: " + MathUtils.round(stickHands) + " / " + (int)SCORE_STICK_HANDS_MAX;
            FontDrawable fd4 = createFontDrawable(font, str, 0.75f, Color.RED, fd1.pos.x, 5.8f / 8f);
            fontDrawables.add(fd4);
            str = "Charcoal Eyes: " + MathUtils.round(charcoalEyes) + " / " + (int)SCORE_CHARCOAL_EYES_MAX;
            FontDrawable fd5 = createFontDrawable(font, str, 0.75f, Color.RED, fd1.pos.x, 5.4f / 8f);
            fontDrawables.add(fd5);
            str = "Carrot Nose: " + MathUtils.round(carrotNose) + " / " + (int)SCORE_CARROT_NOSE_MAX;
            FontDrawable fd6 = createFontDrawable(font, str, 0.75f, Color.RED, fd1.pos.x, 5f / 8f);
            fontDrawables.add(fd6);
            str = "Top Hat: " + MathUtils.round(hat) + " / " + (int)SCORE_TOP_HAT_MAX;
            FontDrawable fd7 = createFontDrawable(font, str, 0.75f, Color.RED, fd1.pos.x, 4.6f / 8f);
            fontDrawables.add(fd7);
            str = "Total: " + MathUtils.round(scoreKeep.total) + " / " + (int)SCORE_TOTAL_MAX;
            FontDrawable fd8 = createFontDrawable(font, str, 1f, Color.RED, 0.5f, 4f / 8f);
            fontDrawables.add(fd8);
            FontDrawable restart = createFontDrawable(font, "Press R to Restart", 1f, Color.YELLOW, 0.5f, 3f / 8f);
            fontDrawables.add(restart);

            state = GameState.OUTRO;
            break;
        }

        case OUTRO: {
            if(InputSingleton.keysPressed > 0 && !Gdx.input.isKeyPressed(Input.Keys.R)) {
                state = GameState.OUTRO_2_INTRO;
            }
            break;
        }

        case OUTRO_2_INTRO: {
            L.l("state=" + state);
            clearPoolArray(fontDrawables);
            world.getBodies(worldBodies);
            clearWorld(world, worldBodies);

            // make title
            FontDrawable title = createFontDrawable(font, TITLE, 1f, Color.RED, 0.5f, 5f / 6f);
            fontDrawables.add(title);

            // display top
            {
                vertices1.clear();
                vertices1.add(TOP_RADIUS);
                BodyDef circleBodyDef = createBodyDef(BodyDef.BodyType.StaticBody, 50f, 50f + BOTTOM_RADIUS+TOP_RADIUS, 0, 0);
                FixtureDef circleFixtureDef = createFixtureDef(Shape.Type.Circle, vertices1, 0f, 0f, 0f, (short) 0);
                Body top = createBody(world, circleBodyDef, circleFixtureDef);
                BodyData data = Pools.obtain(BodyData.class);
                data.fillColor.set(Color.WHITE);
                data.outlineColor.set(Color.WHITE);
                top.setUserData(data);
                Body eyes = createItem(world, EntityType.DISPLAY_EYES);
                eyes.setType(BodyDef.BodyType.StaticBody);
                eyes.setTransform(top.getPosition(), eyes.getAngle());
                Body nose = createItem(world, EntityType.DISPLAY_NOSE);
                nose.setType(BodyDef.BodyType.StaticBody);
                nose.setTransform(top.getPosition(), nose.getAngle());
                Body hat = createItem(world, EntityType.DISPLAY_HAT);
                hat.setType(BodyDef.BodyType.StaticBody);
                hat.setTransform(top.getPosition(), hat.getAngle());
            }
            // display bottom
            {
                vertices1.clear();
                vertices1.add(BOTTOM_RADIUS);
                BodyDef bd = createBodyDef(BodyDef.BodyType.StaticBody, 50f, 50f, 0, 0);
                FixtureDef fd = createFixtureDef(Shape.Type.Circle, vertices1, 0f, 0f, 0f, (short) 0);
                Body bottom = createBody(world, bd, fd);
                BodyData circleData = Pools.obtain(BodyData.class);
                circleData.fillColor.set(Color.WHITE);
                circleData.outlineColor.set(Color.WHITE);
                bottom.setUserData(circleData);
                Body hands = createItem(world, EntityType.DISPLAY_HANDS);
                hands.setType(BodyDef.BodyType.StaticBody);
                hands.setTransform(bottom.getPosition(), bottom.getAngle());
            }

            // tag line
            FontDrawable inst0 = createFontDrawable(font, INSTRUCTIONS_0, 0.75f, Color.RED, 0.5f, 0.45f);
            fontDrawables.add(inst0);

            // make instructions text
            FontDrawable inst1 = createFontDrawable(font, INSTRUCTIONS_1, 0.75f, Color.RED, 0.5f, 1f / 4f);
            fontDrawables.add(inst1);
            FontDrawable inst2 = createFontDrawable(font, INSTRUCTIONS_2, 0.75f, Color.RED, 0.5f, 1f / 4f);
            inst2.pos.y -= inst1.bounds.height * 1.5f;
            fontDrawables.add(inst2);
            FontDrawable inst3 = createFontDrawable(font, INSTRUCTIONS_3, 0.75f, Color.RED, 0.5f, 1f / 4f);
            inst3.pos.y -= inst1.bounds.height * 3f;
            fontDrawables.add(inst3);

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
        world.getBodies(worldBodies);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
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

    private void clearPoolArray(Array array) {
        Pools.freeAll(array, true);
        array.clear();
    }

    private void clearWorld(World world, Array<Body> worldBodies) {
        for(int i = 0; i < worldBodies.size; i++) {
            Body b = worldBodies.get(i);
            Pools.free(b.getUserData());
            world.destroyBody(b);
            if(namedBodies.containsValue(b, true)) {
                namedBodies.remove(namedBodies.findKey(b, true));
            }
        }
        worldBodies.clear();
    }

    private FontDrawable createFontDrawable(BitmapFont font, String text, float scale, Color tint, float posX, float posY) {
        FontDrawable fd = Pools.obtain(FontDrawable.class);
        fd.text = text;
        fd.scale = scale;
        font.setScale(scale);
        fd.bounds.set(font.getBounds(fd.text));
        //fd.bounds.width *= scale;
        //fd.bounds.height *= scale;
        fd.tint.set(tint);
        if(posX < 0f || posX > 1f) fd.pos.x = posX; else fd.pos.x = (FONT_VIEWPORT_WIDTH - fd.bounds.width) * posX;
        if(posY < 0f || posY > 1f) fd.pos.y = posY; else fd.pos.y = (FONT_VIEWPORT_HEIGHT - fd.bounds.height) * posY;
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

    private FixtureDef createFixtureDef(Shape.Type type, FloatArray verts, float massDensity, float friction, float restitution, short groudIdx) {
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
            float x1 = verts.get(0), y1 = verts.get(1);
            float x2 = verts.get(verts.size - 2), y2 = verts.get(verts.size - 1);
            if(MathUtils.isEqual(x1, x2, 0.1f) && MathUtils.isEqual(y1, y2, 0.1f)) {
                verts.removeIndex(verts.size - 1);
                verts.removeIndex(verts.size - 1);
                verts.shrink();
                cs.createLoop(verts.items);
            } else {
                cs.createChain(verts.items);
            }
            break;
        }
        }
        A.NN(shape, "shape != null");

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = massDensity;
        fixtureDef.friction = friction;
        fixtureDef.restitution = restitution;
        fixtureDef.filter.groupIndex = groudIdx;

        return fixtureDef;
    }

    private Body createBody(World world, BodyDef bodyDef, FixtureDef fixtureDef) {
        Body body = world.createBody(bodyDef);
        body.createFixture(fixtureDef);
        fixtureDef.shape.dispose();
        return body;
    }

    private Body createItem(World world, EntityType type) {
        vertices1.clear();
        vertices2.clear();
        BodyData data = Pools.obtain(BodyData.class);
        data.type = type;
        Shape.Type shapeType = null;

        switch(type) {
        case STICK_HANDS: {
            shapeType = Shape.Type.Chain;
            vertices1.addAll(-2f, -2f, 0f, 0f, 0f, 2f, 0f, 0f, 2f, 2f, 0f, 0f, 2f, 0f);
            data.outlineColor.set(Color.ORANGE);
            break;
        }
        case CHARCOAL_EYES: {
            shapeType = Shape.Type.Chain;
            vertices1.addAll(0f, 0f, -1f,  1.5f, -2f, 0f, -1f, -1.5f, 0f, 0f);
            vertices2.addAll(0f, 0f,  1f, -1.5f,  2f, 0f,  1f,  1.5f, 0f, 0f);
            //data.fillColor.set(Color.GRAY);
            data.outlineColor.set(Color.GRAY);
            break;
        }
        case CARROT_NOSE: {
            shapeType = Shape.Type.Chain;
            vertices1.addAll(-1.5f, -1.5f, 1.5f, 0.5f, 0.5f, 1.5f, -1.5f, -1.5f);
            //data.fillColor.set(Color.ORANGE);
            data.outlineColor.set(Color.ORANGE);
            break;
        }
        case TOP_HAT: {
            shapeType = Shape.Type.Chain;
            vertices1.addAll(-2f, -1f, 2f, -1f);
            vertices2.addAll(-1f, -1f, -1f, 1.5f, 1f, 1.5f, 1f, -1f);
            //data.fillColor.set();
            data.outlineColor.set(Color.OLIVE);
            break;
        }
        case DISPLAY_HANDS: {
            shapeType = Shape.Type.Chain;
            float rad = BOTTOM_RADIUS;
            vertices1.addAll(-rad, 0, -rad-2f, 2f, -rad-2f, 4f, -rad-2f, 2f, -rad-4f, 4f, -rad-2f, 2f, -rad-4f, 2f);
            vertices2.addAll(rad, 0, rad+2f, 2f, rad+2f, 4f, rad+2f, 2f, rad+4f, 4f, rad+2f, 2f, rad+4f, 2f);
            data.outlineColor.set(Color.ORANGE);
            break;
        }
        case DISPLAY_EYES: {
            shapeType = Shape.Type.Chain;
            vertices1.addAll(0f, 0.75f, -0.5f,  1.5f, -1f, 0.75f, -0.5f, 0f, 0f, 0.75f);
            vertices2.addAll(0f, 0.75f,  0.5f, 0f, 1f, 0.75f, 0.5f, 1.5f, 0f, 0.75f);
            data.outlineColor.set(Color.BLACK);
            break;
        }
        case DISPLAY_NOSE: {
            shapeType = Shape.Type.Chain;
            vertices1.addAll(0f, 0f, -1.5f, -0.75f, 0f, -1.5f, 0f, 0f);
            data.outlineColor.set(Color.ORANGE);
            break;
        }
        case DISPLAY_HAT: {
            shapeType = Shape.Type.Chain;
            float rad = TOP_RADIUS;
            vertices1.addAll(-2f, rad, 2f, rad);
            vertices2.addAll(-1f, rad, -1f, rad+2.5f, 1f, rad+2.5f, 1f, rad);
            data.outlineColor.set(Color.ORANGE);
            break;
        }
        default:
            A.T(false, "item type=" + type);
        }
        vertices1.shrink();
        vertices2.shrink();
        A.NN(shapeType, "shapeType != null");
        A.T(vertices1.size > 0, "vertices1.size > 0");

        float xPos = MathUtils.random(0, WORLD_WIDTH);
        float yPos = WORLD_HEIGHT + 5f;
        BodyDef bd = createBodyDef(BodyDef.BodyType.DynamicBody, xPos, yPos, 0, 0f);
        bd.gravityScale = 0.1f;
        FixtureDef fd1 = createFixtureDef(shapeType, vertices1, 0f, 0f, 0f, type.groupIdx);
        fd1.isSensor = true;
        Body b = createBody(world, bd, fd1);
        if(vertices2.size > 0) {
            FixtureDef fd2 = createFixtureDef(shapeType, vertices2, 0f, 0f, 0f, type.groupIdx);
            fd2.isSensor = true;
            b.createFixture(fd2);
            fd2.shape.dispose();
        }
        b.setUserData(data);
        return b;
    }

    private void drawBody(ShapeRenderer renderer, Body body) {
        Array<Fixture> fixtures = body.getFixtureList();
        Vector2 bodyPos = body.getPosition();
        BodyData bodyData = (BodyData) body.getUserData();
        switch(renderer.getCurrentType()) {
        case Line:
            renderer.setColor(bodyData.outlineColor);
            break;
        case Filled:
            renderer.setColor(bodyData.fillColor);
            break;
        }
        for(int i = 0; i < fixtures.size; i++) {
            Fixture f = fixtures.get(i);
            switch(f.getType()) {
            case Circle: {
                CircleShape circle = (CircleShape) f.getShape();
                renderer.circle(bodyPos.x, bodyPos.y, circle.getRadius(), CIRCLE_SEG_COUNT);
                break;
            }
            case Edge: {
                if(renderer.getCurrentType() == ShapeRenderer.ShapeType.Filled) break;

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
                if(renderer.getCurrentType() == ShapeRenderer.ShapeType.Filled) break;

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
                if(renderer.getCurrentType() == ShapeRenderer.ShapeType.Filled) break;

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

    private Body getBodyOfType(ContactEvent event, EntityType type) {
        BodyData a = (BodyData) event.a.getUserData();
        if(a != null && a.type == type) { return event.a; }
        BodyData b = (BodyData) event.b.getUserData();
        if(b != null && b.type == type) { return event.b; }
        return null;
    }

    private float calcPercentFromCenter(float val, float min, float max) {
        float width_2 = (max - min) / 2f;
        float centerX = min + width_2;
        float distFromCenter = Math.abs(val - centerX);
        return (1f - (distFromCenter / width_2));
    }
}
