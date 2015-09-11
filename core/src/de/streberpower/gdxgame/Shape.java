package de.streberpower.gdxgame;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.collision.Ray;

/**
 * Created by Papas on 11.09.2015.
 */
public interface Shape {
    boolean isVisible(Matrix4 transform, Camera camera);

    float intersects(Matrix4 transform, Ray ray);
}
