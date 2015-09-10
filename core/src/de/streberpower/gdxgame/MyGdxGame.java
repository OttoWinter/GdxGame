package de.streberpower.gdxgame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.utils.Array;


public class MyGdxGame extends ApplicationAdapter {

    private static final String INVADERSCENE_MODEL = "invaderscene.g3db";
    private static final String SHIP_TYPE = "ship";
    private static final String BLOCK_TYPE = "block";
    private static final String INVADER_TYPE = "invader";
    private static final String SPACE_TYPE = "space";

    public PerspectiveCamera camera;
    public CameraInputController cameraController;

    public Environment environment;
    public ModelBatch modelBatch;

    public Array<ModelInstance> instances = new Array<ModelInstance>();

    public AssetManager assets;
    public boolean loading;
    public Array<ModelInstance> blocks = new Array<ModelInstance>();
    public Array<ModelInstance> invaders = new Array<ModelInstance>();
    public ModelInstance ship;
    public ModelInstance space;
    //public Model model;
    //public ModelInstance instance;

    @Override
    public void create() {
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
        Gdx.input.setInputProcessor(cameraController);

        //model = modelBuilder.createBox(5f, 5f, 5f, new Material(ColorAttribute.createDiffuse(Color.GREEN)),
        //        VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        //model = loader.loadModel(Gdx.files.internal("ship.obj"));
        //instance = new ModelInstance(model);


        assets = new AssetManager();
        assets.load(INVADERSCENE_MODEL, Model.class);
        loading = true;
    }

    private void doneLoading() {
        Model model = assets.get(INVADERSCENE_MODEL, Model.class);
        for (int i = 0; i < model.nodes.size; i++) {
            String id = model.nodes.get(i).id;
            ModelInstance instance = new ModelInstance(model, id);
            Node node = instance.getNode(id);

            instance.transform.set(node.globalTransform);
            node.translation.set(0, 0, 0);
            node.scale.set(1, 1, 1);
            node.rotation.idt();
            instance.calculateTransforms();

            if (id.equals(SPACE_TYPE)) {
                space = instance;
                continue;
            }

            instances.add(instance);

            if (id.equals(SHIP_TYPE))
                ship = instance;
            else if (id.startsWith(BLOCK_TYPE))
                blocks.add(instance);
            else if (id.startsWith(INVADER_TYPE))
                invaders.add(instance);
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
        modelBatch.render(instances, environment);
        if (space != null)
            modelBatch.render(space);
        modelBatch.end();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        invaders.clear();
        assets.dispose();
    }
}
