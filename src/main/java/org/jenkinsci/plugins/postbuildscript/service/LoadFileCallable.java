package org.jenkinsci.plugins.postbuildscript.service;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import java.io.File;
import java.io.IOException;
import jenkins.MasterToSlaveFileCallable;

public class LoadFileCallable extends MasterToSlaveFileCallable<FilePath> {
    private static final long serialVersionUID = -7079515863020025724L;
    private final String givenPath;
    private final FilePath workspace;

    public LoadFileCallable(String givenPath, FilePath workspace) {
        this.givenPath = givenPath;
        this.workspace = workspace;
    }

    @Override
    public FilePath invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
        File givenFile = new File(givenPath);
        if (givenFile.exists()) {
            return new FilePath(channel, givenFile.getPath());
        }

        FilePath filePath = new FilePath(workspace, givenPath);
        if (filePath.exists()) {
            return filePath;
        }
        return null;
    }
}
