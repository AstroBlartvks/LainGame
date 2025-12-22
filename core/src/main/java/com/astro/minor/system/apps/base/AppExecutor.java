package com.astro.minor.system.apps.base;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;

public class AppExecutor implements Runnable {
    private List<Application> applications;
    private SpriteBatch batch;
    private List<Application> applicationsToRemove;
    private InputMultiplexer inputMultiplexer;
    private Application focusedApplication;
    private FocusHandler focusHandler;

    public AppExecutor(SpriteBatch batch) {
        this.batch = batch;
        applications = new ArrayList<>();
        applicationsToRemove = new ArrayList<>();
        inputMultiplexer = new InputMultiplexer();
        focusedApplication = null;
        focusHandler = new FocusHandler();
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    private class FocusHandler implements InputProcessor {
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            com.badlogic.gdx.math.Vector3 mouse = new com.badlogic.gdx.math.Vector3(screenX, screenY, 0);

            if (!applications.isEmpty()) {
                Application topApplication = applications.get(applications.size() - 1);
                topApplication.getCamera().unproject(mouse);
            }

            for (int i = applications.size() - 1; i >= 0; i--) {
                Application app = applications.get(i);

                int appX = app.getX();
                int appY = app.getY();
                int appW = app.getWidth();
                int appH = app.getHeight();

                if (mouse.x >= appX && mouse.x <= appX + appW &&
                    mouse.y >= appY && mouse.y <= appY + appH + 20) {

                    if (focusedApplication != app) {
                        setFocusedApplication(app);
                        updateInputProcessors();
                    }
                    break;
                }
            }

            return false; // DON'T consume - let other processors handle it
        }

        @Override
        public boolean keyDown(int keycode) { return false; }
        @Override
        public boolean keyUp(int keycode) { return false; }
        @Override
        public boolean keyTyped(char character) { return false; }
        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
        @Override
        public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
        @Override
        public boolean mouseMoved(int screenX, int screenY) { return false; }
        @Override
        public boolean scrolled(float amountX, float amountY) { return false; }
    }

    public void addApplication(Application application) {
        applications.add(application);

        setFocusedApplication(application);

        if (application instanceof com.astro.minor.system.apps.terminal.Console) {
            com.astro.minor.system.apps.terminal.Console console = (com.astro.minor.system.apps.terminal.Console) application;
            updateInputProcessors();
        } else if (application instanceof com.astro.minor.system.apps.editor.MemexEditor) {
            com.astro.minor.system.apps.editor.MemexEditor editor = (com.astro.minor.system.apps.editor.MemexEditor) application;
            updateInputProcessors();
        } else if (application instanceof com.astro.minor.system.apps.browser.Browser) {
            com.astro.minor.system.apps.browser.Browser browser = (com.astro.minor.system.apps.browser.Browser) application;
            updateInputProcessors();
        }
    }

    public void removeApplication(Application application) {
        if (application instanceof com.astro.minor.system.apps.terminal.Console) {
            com.astro.minor.system.apps.terminal.Console console = (com.astro.minor.system.apps.terminal.Console) application;
            inputMultiplexer.removeProcessor(console.getInputProcessor());
        } else if (application instanceof com.astro.minor.system.apps.editor.MemexEditor) {
            com.astro.minor.system.apps.editor.MemexEditor editor = (com.astro.minor.system.apps.editor.MemexEditor) application;
            inputMultiplexer.removeProcessor(editor.getInputProcessor());
        } else if (application instanceof com.astro.minor.system.apps.browser.Browser) {
            com.astro.minor.system.apps.browser.Browser browser = (com.astro.minor.system.apps.browser.Browser) application;
            inputMultiplexer.removeProcessor(browser.getInputProcessor());
        }

        applications.remove(application);

        if (application instanceof com.astro.minor.system.apps.terminal.Console || application instanceof com.astro.minor.system.apps.browser.Browser) {
            updateInputProcessors();
        }
    }

    private void updateInputProcessors() {
        inputMultiplexer.clear();

        inputMultiplexer.addProcessor(focusHandler);

        if (focusedApplication instanceof com.astro.minor.system.apps.editor.MemexEditor) {
            com.astro.minor.system.apps.editor.MemexEditor editor = (com.astro.minor.system.apps.editor.MemexEditor) focusedApplication;
            inputMultiplexer.addProcessor(editor.getInputProcessor());
        } else if (focusedApplication instanceof com.astro.minor.system.apps.browser.Browser) {
            com.astro.minor.system.apps.browser.Browser browser = (com.astro.minor.system.apps.browser.Browser) focusedApplication;
            inputMultiplexer.addProcessor(browser.getInputProcessor());
        } else if (focusedApplication instanceof com.astro.minor.system.apps.terminal.Console) {
            com.astro.minor.system.apps.terminal.Console console = (com.astro.minor.system.apps.terminal.Console) focusedApplication;
            inputMultiplexer.addProcessor(console.getInputProcessor());
        }

        for (Application app : applications) {
            if (app instanceof com.astro.minor.system.apps.terminal.Console && app != focusedApplication) {
                com.astro.minor.system.apps.terminal.Console console = (com.astro.minor.system.apps.terminal.Console) app;
                inputMultiplexer.addProcessor(console.getInputProcessor());
            }
        }
    }

    private void setFocusedApplication(Application app) {
        for (Application application : applications) {
            application.setActiveApplication(application == app);
        }
        focusedApplication = app;
    }

    private void handleApplicationFocus() {
    }

    public int getNewPID() {
        return applications.size() + 1;
    }

    private void checkClosed() {
        if (applications.isEmpty()) { return; }

        for (Application application : applications) {
            if (application.isClosed()) {
                applicationsToRemove.add(application);
            }
        }
    }

    private void processRemovals() {
        if (!applicationsToRemove.isEmpty()) {
            for (Application application : applicationsToRemove) {
                removeApplication(application);
                application.dispose();
            }
            applicationsToRemove.clear();
        }
    }

    public void handleScroll(float amountX, float amountY) {
        if (!applications.isEmpty()) {
            for (Application application : applications) {
                if (application.isActive()) {
                    application.onScroll(amountX, amountY);
                    break;
                }
            }
        }
    }

    @Override
    public void run() {
        checkClosed();

        if (!applications.isEmpty()) {
            List<Application> copy = new ArrayList<>(applications);
            for (Application application : copy) {
                if (!applicationsToRemove.contains(application)) {
                    application.run(batch);
                }
            }
        }

        processRemovals();
    }

    public void dispose() {
        for (Application application : applications) {
            application.dispose();
        }
        applications.clear();
        applicationsToRemove.clear();
    }
}
