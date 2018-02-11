package org.jenkinsci.plugins.postbuildscript;

import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixRun;
import hudson.model.BuildListener;
import org.jenkinsci.plugins.postbuildscript.processor.Processor;
import org.jenkinsci.plugins.postbuildscript.processor.ProcessorFactory;

import java.io.IOException;

public class ConfigurableMatrixAggregator extends MatrixAggregator {

    private final Processor processor;

    public ConfigurableMatrixAggregator(
        MatrixBuild build,
        Launcher launcher,
        BuildListener listener,
        ProcessorFactory processorFactory
    ) {
        super(build, launcher, listener);
        processor = processorFactory.createMatrixProcessor(build, launcher, listener);
    }

    @Override
    public boolean endRun(MatrixRun run) throws InterruptedException, IOException {
        listener.getLogger().println();
        return super.endRun(run);
    }

    @Override
    public boolean endBuild() {
        return processor.process(true);
    }
}
