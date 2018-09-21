package org.jenkinsci.plugins.postbuildscript;

import hudson.model.TaskListener;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class Logger implements Serializable {

    private static final long serialVersionUID = 1083402096308867767L;
    private final TaskListener listener;

    public Logger(TaskListener listener) {
        this.listener = listener;
    }

    public TaskListener getListener() {
        return listener;
    }

    public void error(String message) {
        log(Messages.PostBuildScript_ErrorPrefix(message));
    }

    public void warn(String message) {
        log(Messages.PostBuildScript_WarnPrefix(message));
    }

    public void info(String message) {
        log(message);
    }

    private void log(String message) {
        listener.getLogger().println(Messages.PostBuildScript_LogPrefix(message));
    }

}
