package de.streberpower.gdxgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class GameScreen implements Screen {
    public static final int RAINDROP_STARTING_Y = 480;
    public static final int RAINDROP_DROP_COOLDOWN = 1000000000;
    public static final int RAINDROP_DROP_COOLDOWN_DECREASE = 100000000;
    public static final int BUCKET_SIZE = 64;
    public static final int BUCKET_Y = 20;
    private static final int RAINDROP_FALLING_SPEED = 200;
    private static final int RAINDROP_SIZE = 64;
    final MyGdxGame game;
    private OrthographicCamera camera;

    private Texture dropImage;
    private Texture bucketImage;
    private Sound dropSound;
    private Music rainMusic;

    private Rectangle bucket;

    private Vector3 touchPos = new Vector3();

    private Array<Rectangle> raindrops = new Array<Rectangle>();

    private long lastDropTime;
    private int dropCooldown = RAINDROP_DROP_COOLDOWN;

    private FPSLogger fpsLogger;


    public GameScreen(final MyGdxGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        dropImage = new Texture("droplet.png");
        bucketImage = new Texture("bucket.png");

        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

        rainMusic.setLooping(true);

        bucket = new Rectangle();
        bucket.width = BUCKET_SIZE;
        bucket.height = BUCKET_SIZE;
        bucket.x = Gdx.graphics.getWidth() / 2 - bucket.width / 2;
        bucket.y = BUCKET_Y;

        spawnRaindrop();
        fpsLogger = new FPSLogger();
    }

    private void spawnRaindrop() {
        Rectangle raindrop = new Rectangle();
        raindrop.width = RAINDROP_SIZE;
        raindrop.height = RAINDROP_SIZE;
        raindrop.x = MathUtils.random(0, Gdx.graphics.getWidth() - raindrop.width);
        raindrop.y = RAINDROP_STARTING_Y;
        raindrops.add(raindrop);
        lastDropTime = TimeUtils.nanoTime();
    }

    @Override
    public void show() {
        rainMusic.play();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();


        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            bucket.x = touchPos.x - bucket.width / 2;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) bucket.x -= RAINDROP_FALLING_SPEED * Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) bucket.x += RAINDROP_FALLING_SPEED * Gdx.graphics.getDeltaTime();

        if (bucket.x < 0) bucket.x = 0;
        if (bucket.x > Gdx.graphics.getWidth() - bucket.width) bucket.x = Gdx.graphics.getWidth() - bucket.getWidth();

        if (TimeUtils.nanoTime() - lastDropTime > dropCooldown)
            spawnRaindrop();

        Iterator<Rectangle> iter = raindrops.iterator();
        while (iter.hasNext()) {
            Rectangle raindrop = iter.next();
            raindrop.y -= RAINDROP_FALLING_SPEED * Gdx.graphics.getDeltaTime();
            if (raindrop.overlaps(bucket)) {
                dropSound.play();
                iter.remove();
                dropCooldown -= RAINDROP_DROP_COOLDOWN_DECREASE;
            } else if (raindrop.y + raindrop.height < 0) {
                iter.remove();
                dropCooldown -= RAINDROP_DROP_COOLDOWN_DECREASE * 2;
            }
        }
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        game.batch.draw(bucketImage, bucket.x, bucket.y);
        for (Rectangle raindrop : raindrops) {
            game.batch.draw(dropImage, raindrop.x, raindrop.y);
        }
        game.batch.end();
        fpsLogger.log();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        dropImage.dispose();
        bucketImage.dispose();
        dropSound.dispose();
        rainMusic.dispose();
    }
}
