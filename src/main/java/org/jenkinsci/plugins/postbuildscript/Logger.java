package org.jenkinsci.plugins.postbuildscript;

import hudson.model.TaskListener;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class Logger implements Serializable {

    private final TaskListener listener;

    public Logger(TaskListener listener) {
        this.listener = listener;
    }

    public TaskListener getListener() {
        return listener;
    }

    public void info(String message) {
        listener.getLogger().println("[PostBuildScript] - " + message);
    }

    public void error(String message) {
        listener.getLogger().println("[PostBuildScript] - [ERROR] - " + message);
    }
}
