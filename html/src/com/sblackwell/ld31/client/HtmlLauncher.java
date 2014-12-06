package com.sblackwell.ld31.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.sblackwell.ld31.LD31Game;
import com.sblackwell.ld31.utils.L;

public class HtmlLauncher extends GwtApplication {

        @Override
        public GwtApplicationConfiguration getConfig () {
            L.logWriter = new LogWriterGwtImpl();
            return new GwtApplicationConfiguration(700, 700);
        }

        @Override
        public ApplicationListener getApplicationListener () {
                return new LD31Game();
        }
}
