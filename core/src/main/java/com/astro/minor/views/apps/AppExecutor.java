package com.astro.minor.views.apps;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;

public class AppExecutor implements Runnable {
    private List<Application> applications;
    private SpriteBatch batch;

    public AppExecutor(SpriteBatch batch) {
        this.batch = batch;
        applications = new ArrayList<>();
    }

    public void addApplication(Application application) {
        applications.add(application);
    }

    public void removeApplication(Application application) {
        applications.remove(application);
    }

    private void checkClosed() {
        for (Application application : applications) {
            if (application.isClosed()) {
                removeApplication(application);
                application.dispose();
            }
        }
    }

    @Override
    public void run() {
        for (Application application : applications) {
            application.run(batch);
        }
    }

    public void dispose() {
        for (Application application : applications) {
            application.dispose();
        }
        applications.clear();
    }
}
