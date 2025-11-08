package com.astro.minor.views.apps;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;

public class AppExecutor implements Runnable {
    private List<Application> applications;
    private SpriteBatch batch;
    private List<Application> applicationsToRemove;

    public AppExecutor(SpriteBatch batch) {
        this.batch = batch;
        applications = new ArrayList<>();
        applicationsToRemove = new ArrayList<>();
    }

    public void addApplication(Application application) {
        applications.add(application);
    }

    public void removeApplication(Application application) {
        applications.remove(application);
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
