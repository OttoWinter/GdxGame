package de.streberpower.gdxgame.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import de.streberpower.gdxgame.PhysicsTest;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        //config.title = "GdxGame";
        //config.width = 480;
        //config.height = 320;
		new LwjglApplication(new PhysicsTest(), config);
	}
}
