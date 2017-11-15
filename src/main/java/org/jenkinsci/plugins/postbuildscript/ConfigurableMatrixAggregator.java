package org.jenkinsci.plugins.postbuildscript;

import hudson.Extension;
import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.model.BuildListener;

import java.io.IOException;

@Extension(optional = true)
public class ConfigurableMatrixAggregator extends MatrixAggregator {

    private final Processor processor;

    private final ExecuteOn executeOn;

    public ConfigurableMatrixAggregator(
        MatrixBuild build,
        Launcher launcher,
        BuildListener listener,
        ProcessorFactory processorFactory,
        ExecuteOn executeOn
    ) {
        super(build, launcher, listener);
        processor = processorFactory.create(build, launcher, listener);
        this.executeOn = executeOn;
    }

    @Override
    public boolean endBuild() throws InterruptedException, IOException {
        return !executeOn.matrix() || processor.process();
    }
}
