package com.sblackwell.ld31.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.sblackwell.ld31.LD31Game;
import com.sblackwell.ld31.utils.L;

public class DesktopLauncher {
	public static void main (String[] arg) {
        L.logWriter = new LogWriterDesktopImpl();

        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 900;
        config.height = 900;
        config.resizable = false;
		new LwjglApplication(new LD31Game(), config);
	}
}
