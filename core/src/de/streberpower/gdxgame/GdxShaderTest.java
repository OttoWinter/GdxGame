package de.streberpower.gdxgame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Array;


public class GdxShaderTest extends ApplicationAdapter {


    public PerspectiveCamera camera;
    public CameraInputController cameraController;

    public Shader shader;
    public ModelBatch modelBatch;

    public Environment environment;
    public Model model;
    public Array<ModelInstance> instances = new Array<ModelInstance>();

    @Override
    public void create() {
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0f, 8f, 8f);
        camera.lookAt(0, 0, 0);
        camera.near = 0.05f;
        camera.far = 300f;
        camera.update();

        cameraController = new CameraInputController(camera);
        Gdx.input.setInputProcessor(cameraController);

        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.createSphere(2f, 2f, 2f, 20, 20,
                new Material(), VertexAttributes.Usage.Position |
                        VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        for (int x = -5; x <= 5; x += 2) {
            for (int z = -5; z <= 5; z += 2) {
                ModelInstance instance = new ModelInstance(model, x, 0, z);
                TestShader.TestColorAttribute attrUV = new TestShader.TestColorAttribute(
                        TestShader.TestColorAttribute.DIFFUSE_UV,
                        new Color((x + 5f) / 10f, 1f - (z + 5f) / 10f, 0, 1),
                        new Color(1f - (x + 5f) / 10f, 0, 1f - (z + 5f) / 10f, 1));
                instance.materials.get(0).set(attrUV);
                instances.add(instance);
            }
        }
        shader = new TestShader();
        shader.init();

        modelBatch = new ModelBatch();
    }

    @Override
    public void render() {
        cameraController.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(camera);
        for (ModelInstance instance : instances)
            modelBatch.render(instance, shader);
        modelBatch.end();
    }

    @Override
    public void dispose() {
        shader.dispose();
        model.dispose();
    }
}
