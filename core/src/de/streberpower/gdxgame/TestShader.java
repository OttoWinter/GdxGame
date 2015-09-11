package de.streberpower.gdxgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class TestShader implements Shader {
    ShaderProgram program;
    Camera camera;
    RenderContext context;
    int u_projTrans;
    int u_worldTrans;
    int u_colorU;
    int u_colorV;

    @Override
    public void init() {
        String vert = Gdx.files.internal("test.vertex.glsl").readString();
        String frag = Gdx.files.internal("test.fragment.glsl").readString();
        program = new ShaderProgram(vert, frag);
        if (!program.isCompiled())
            throw new GdxRuntimeException(program.getLog());
        u_projTrans = program.getUniformLocation("u_projTrans");
        u_worldTrans = program.getUniformLocation("u_worldTrans");
        u_colorU = program.getUniformLocation("u_colorU");
        u_colorV = program.getUniformLocation("u_colorV");
    }

    @Override
    public int compareTo(Shader other) {
        return 0;
    }

    @Override
    public boolean canRender(Renderable instance) {
        return instance.material.has(TestColorAttribute.DIFFUSE_UV);
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        this.camera = camera;
        this.context = context;
        program.begin();
        program.setUniformMatrix(u_projTrans, camera.combined);
        context.setDepthTest(GL20.GL_LEQUAL);
        context.setCullFace(GL20.GL_BACK);
    }

    @Override
    public void render(Renderable renderable) {
        program.setUniformMatrix(u_worldTrans, renderable.worldTransform);
        Color colorU = ((TestColorAttribute) renderable.material.get(TestColorAttribute.DIFFUSE_UV)).color1;
        Color colorV = ((TestColorAttribute) renderable.material.get(TestColorAttribute.DIFFUSE_UV)).color2;
        program.setUniformf(u_colorU, colorU.r, colorU.g, colorU.b);
        program.setUniformf(u_colorV, colorV.r, colorV.g, colorV.b);
        renderable.mesh.render(program,
                renderable.primitiveType,
                renderable.meshPartOffset,
                renderable.meshPartSize);
    }

    @Override
    public void end() {
        program.end();
    }

    @Override
    public void dispose() {
        program.dispose();
    }

    public static class TestColorAttribute extends Attribute {
        public final static String DIFFUSE_UV_ALIAS = "diffuseUVColor";
        public final static long DIFFUSE_UV = register(DIFFUSE_UV_ALIAS);

        public final Color color1 = new Color();
        public final Color color2 = new Color();

        public TestColorAttribute(long type, Color c1, Color c2) {
            super(type);
            color1.set(c1);
            color2.set(c2);
        }

        @Override
        public Attribute copy() {
            return new TestColorAttribute(type, color1, color2);
        }

        @Override
        protected boolean equals(Attribute other) {
            if (other == null || !(other instanceof TestColorAttribute))
                return false;

            TestColorAttribute attr = (TestColorAttribute) other;
            return type == other.type && color1.equals(attr.color1) && color2.equals(attr.color2);
        }

        @Override
        public int compareTo(@SuppressWarnings("NullableProblems") Attribute other) {
            if (type != other.type)
                return (int) (type - other.type);
            TestColorAttribute attr = (TestColorAttribute) other;
            return color1.equals(attr.color1)
                    ? attr.color2.toIntBits() - color2.toIntBits()
                    : attr.color1.toIntBits() - color1.toIntBits();
        }
    }
}
