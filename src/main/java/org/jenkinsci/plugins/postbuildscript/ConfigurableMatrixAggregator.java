package org.jenkinsci.plugins.postbuildscript;

import hudson.Launcher;
import hudson.matrix.MatrixBuild;
import hudson.model.BuildListener;
import org.jenkinsci.plugins.postbuildscript.model.Configuration;

import java.io.IOException;

public class ConfigurableMatrixAggregator extends hudson.matrix.MatrixAggregator {

    private final Processor processor;

    private final ExecuteOn executeOn;

    public ConfigurableMatrixAggregator(
        MatrixBuild build,
        Launcher launcher,
        BuildListener listener,
        Configuration config,
        ExecuteOn executeOn
    ) {
        super(build, launcher, listener);
        processor = ProcessorFactory.create(build, launcher, listener, config);
        this.executeOn = executeOn;
    }

    @Override
    public boolean endBuild() throws InterruptedException, IOException {
        return !executeOn.matrix() || processor.process();
    }
}
