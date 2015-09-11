package de.streberpower.gdxgame;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;


public class MyGdxGame extends InputAdapter implements ApplicationListener {

    private static final String INVADER_SCENE_MODEL = "invaderscene.g3db";
    private static final String SHIP_TYPE = "ship";
    private static final String BLOCK_TYPE = "block";
    private static final String INVADER_TYPE = "invader";
    private static final String SPACE_TYPE = "space";

    public PerspectiveCamera camera;
    public CameraInputController cameraController;

    public Environment environment;
    public ModelBatch modelBatch;

    public Array<GameObject> instances = new Array<GameObject>();

    public AssetManager assets;
    public boolean loading;
    public Stage stage;
    public Label label;
    public BitmapFont font;
    public StringBuilder sb;
    public Array<GameObject> blocks = new Array<GameObject>();
    public Array<GameObject> invaders = new Array<GameObject>();
    public GameObject ship;
    public GameObject space;
    protected Shape blockShape;
    protected Shape invaderShape;
    protected Shape shipShape;
    private Vector3 position = new Vector3();
    private int selected = -1, selecting = -1;
    private Material selectionMaterial;
    private Material originalMaterial;
    private BoundingBox bounds = new BoundingBox();

    @Override
    public void create() {
        stage = new Stage();
        font = new BitmapFont();
        label = new Label(" ", new Label.LabelStyle(font, Color.WHITE));
        stage.addActor(label);
        sb = new StringBuilder();
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        modelBatch = new ModelBatch();

        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0f, 7f, 10f);
        camera.lookAt(0, 0, 0);
        camera.near = 1f;
        camera.far = 300f;
        camera.update();

        cameraController = new CameraInputController(camera);
        Gdx.input.setInputProcessor(new InputMultiplexer(this, cameraController));

        //model = modelBuilder.createBox(5f, 5f, 5f, new Material(ColorAttribute.createDiffuse(Color.GREEN)),
        //        VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        //model = loader.loadModel(Gdx.files.internal("ship.obj"));
        //instance = new ModelInstance(model);


        assets = new AssetManager();
        assets.load(INVADER_SCENE_MODEL, Model.class);
        loading = true;

        selectionMaterial = new Material();
        selectionMaterial.set(ColorAttribute.createDiffuse(Color.ORANGE));
        originalMaterial = new Material();
    }

    private void doneLoading() {
        Model model = assets.get(INVADER_SCENE_MODEL, Model.class);
        for (int i = 0; i < model.nodes.size; i++) {
            String id = model.nodes.get(i).id;
            GameObject instance = new GameObject(model, id, true);

            if (id.equals(SPACE_TYPE)) {
                space = instance;
                continue;
            }

            instances.add(instance);

            if (id.equals(SHIP_TYPE)) {
                if (shipShape == null) {
                    instance.calculateBoundingBox(bounds);
                    shipShape = new BaseShape.Sphere(bounds);
                }
                instance.shape = shipShape;
                ship = instance;
            } else if (id.startsWith(BLOCK_TYPE)) {
                if (blockShape == null) {
                    instance.calculateBoundingBox(bounds);
                    blockShape = new BaseShape.Box(bounds);
                }
                instance.shape = blockShape;
                blocks.add(instance);
            } else if (id.startsWith(INVADER_TYPE)) {
                if (invaderShape == null) {
                    instance.calculateBoundingBox(bounds);
                    invaderShape = new BaseShape.Disc(bounds);
                }
                instance.shape = invaderShape;
                invaders.add(instance);
            }
        }

        loading = false;
    }

    @Override
    public void render() {
        if (loading && assets.update())
            doneLoading();
        cameraController.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(camera);
        int visibleCount = 0;
        for (final GameObject instance : instances) {
            if (instance.isVisible(camera)) {
                modelBatch.render(instance, environment);
                visibleCount++;
            }
        }
        if (space != null)
            modelBatch.render(space);
        modelBatch.end();

        sb.setLength(0);
        sb.append(" FPS: ").append(Gdx.graphics.getFramesPerSecond());
        sb.append(" Visible: ").append(visibleCount);
        sb.append(" Selected: ").append(selected);
        label.setText(sb);
        stage.draw();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }


    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        selecting = getObject(screenX, screenY);
        return selecting >= 0;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (selecting < 0)
            return false;
        if (selected == selecting) {
            Ray ray = camera.getPickRay(screenX, screenY);
            final float distance = -ray.origin.y / ray.direction.y;
            position.set(ray.direction).scl(distance).add(ray.origin);
            instances.get(selected).transform.setTranslation(position);
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (selecting >= 0) {
            if (selecting == getObject(screenX, screenY))
                setSelected(selecting);
            selecting = -1;
            return true;
        }
        return false;
    }

    public void setSelected(int value) {
        if (selected == value) return;
        if (selected >= 0) {
            Material mat = instances.get(selected).materials.get(0);
            mat.clear();
            mat.set(originalMaterial);
        }
        selected = value;
        if (selected >= 0) {
            Material mat = instances.get(selected).materials.get(0);
            originalMaterial.clear();
            originalMaterial.set(mat);
            mat.clear();
            mat.set(selectionMaterial);
        }
    }

    public int getObject(int screenX, int screenY) {
        Ray ray = camera.getPickRay(screenX, screenY);
        int result = -1;
        float distance = -1;
        for (int i = 0; i < instances.size; i++) {
            final float dist2 = instances.get(i).intersects(ray);
            if (dist2 >= 0f && (distance < 0f || dist2 <= distance)) {
                result = i;
                distance = dist2;
            }
        }
        return result;
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        invaders.clear();
        assets.dispose();
    }

    public static class GameObject extends ModelInstance {
        private static final BoundingBox bounds = new BoundingBox();
        private final static Vector3 position = new Vector3();
        public final Vector3 center = new Vector3();
        public final Vector3 dimensions = new Vector3();
        public final float radius;
        public de.streberpower.gdxgame.Shape shape;
        public GameObject(Model model, String rootNode, boolean mergeTransform) {
            super(model, rootNode, mergeTransform);
            calculateBoundingBox(bounds);
            bounds.getCenter(center);
            bounds.getDimensions(dimensions);
            radius = dimensions.len() / 2f;
        }

        public boolean isVisible(final Camera camera) {
            //return camera.frustum.sphereInFrustum(transform.getTranslation(position).add(center), radius);
            return shape != null && shape.isVisible(transform, camera);
        }

        public float intersects(Ray ray) {
            /*transform.getTranslation(position).add(center);
            final float len = ray.direction.dot(position.x - ray.origin.x, position.y - ray.origin.y,
                    position.z - ray.origin.z);
            if (len < 0f) return -1f;
            float dist2 = position.dst2(ray.origin.x + ray.direction.x * len, ray.origin.y + ray.direction.y * len,
                    ray.origin.z + ray.direction.z * len);
            return (dist2 <= radius * radius) ? dist2 : -1f;*/
            return shape == null ? -1f : shape.intersects(transform, ray);
        }

    }
}
