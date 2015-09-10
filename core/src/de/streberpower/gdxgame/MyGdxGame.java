package de.streberpower.gdxgame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Array;


public class MyGdxGame extends ApplicationAdapter {
    public Environment environment;

    public PerspectiveCamera camera;
    public CameraInputController cameraController;

    public ModelBatch modelBatch;

    public AssetManager assets;
    public Array<ModelInstance> instances = new Array<ModelInstance>();
    public boolean loading;
    //public Model model;
    //public ModelInstance instance;

    @Override
    public void create() {
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        modelBatch = new ModelBatch();

        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(1f, 1f, 1f);
        camera.lookAt(0, 0, 0);
        camera.near = 1f;
        camera.far = 1000f;
        camera.update();

        ModelLoader loader = new ObjLoader();
        ModelBuilder modelBuilder = new ModelBuilder();
        //model = modelBuilder.createBox(5f, 5f, 5f, new Material(ColorAttribute.createDiffuse(Color.GREEN)),
        //        VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        //model = loader.loadModel(Gdx.files.internal("ship.obj"));
        //instance = new ModelInstance(model);

        cameraController = new CameraInputController(camera);
        Gdx.input.setInputProcessor(cameraController);

        assets = new AssetManager();
        assets.load("ship.obj", Model.class);
        loading = true;
    }

    private void doneLoading() {
        Model ship = assets.get("ship.obj", Model.class);
        ModelInstance shipInstance = new ModelInstance(ship);
        instances.add(shipInstance);
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
        modelBatch.render(instances, environment);
        modelBatch.end();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        instances.clear();
        assets.dispose();
    }
}
