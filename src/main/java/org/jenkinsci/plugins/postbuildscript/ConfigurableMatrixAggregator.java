package org.jenkinsci.plugins.postbuildscript;

import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixRun;
import hudson.model.BuildListener;
import org.jenkinsci.plugins.postbuildscript.logging.Logger;
import org.jenkinsci.plugins.postbuildscript.processor.Processor;
import org.jenkinsci.plugins.postbuildscript.processor.ProcessorFactory;

import java.io.IOException;

public class ConfigurableMatrixAggregator extends MatrixAggregator {

    private final Processor processor;
    // TODO Added for migration reasons (JENKINS-53691). Remove this, when no installations of 0.18.x are left.
    private final Class<? extends PostBuildScript> initiator;
    private final Logger logger;

    public ConfigurableMatrixAggregator(
        MatrixBuild build,
        Launcher launcher,
        BuildListener listener,
        ProcessorFactory processorFactory,
        Class<? extends PostBuildScript> initiator
    ) {
        super(build, launcher, listener);
        this.initiator = initiator;
        processor = processorFactory.createMatrixProcessor(build, launcher, listener);
        logger = new Logger(listener, build);
    }

    @Override
    public boolean endRun(MatrixRun run) throws InterruptedException, IOException {
        // TODO Added for migration reasons (JENKINS-53691). Remove this, when no installations of 0.18.x are left.
        if (!MatrixPostBuildScript.class.isAssignableFrom(initiator)) {
            logger.warn(Messages.PostBuildScript_DeprecatedUsageOfMatrixOptions());
        }
        logger.debug("endRun", run);
        listener.getLogger().println();
        return super.endRun(run);
    }

    @Override
    public boolean endBuild() {
        // TODO Added for migration reasons (JENKINS-53691). Remove this, when no installations of 0.18.x are left.
        if (!MatrixPostBuildScript.class.isAssignableFrom(initiator)) {
            logger.warn(Messages.PostBuildScript_DeprecatedUsageOfMatrixOptions());
        }
        logger.debug("endBuild");
        return processor.process(true);
    }
}
