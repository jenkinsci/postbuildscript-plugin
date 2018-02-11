package org.jenkinsci.plugins.postbuildscript.processor;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import org.jenkinsci.plugins.postbuildscript.model.Configuration;
import org.jenkinsci.plugins.postbuildscript.processor.rules.MatrixRule;
import org.jenkinsci.plugins.postbuildscript.processor.rules.ResultRule;
import org.jenkinsci.plugins.postbuildscript.processor.rules.RoleRule;

public class ProcessorFactory {

    private final Configuration config;

    public ProcessorFactory(Configuration config) {
        this.config = config;
    }

    public Processor createDefaultProcessor(
        AbstractBuild<?, ?> build,
        Launcher launcher,
        BuildListener listener
    ) {
        Processor processor = new Processor(build, launcher, listener, config);
        processor.addRule(new RoleRule(build));
        processor.addRule(new ResultRule(build));
        return processor;
    }

    public Processor createMatrixProcessor(
        AbstractBuild<?, ?> build,
        Launcher launcher,
        BuildListener listener
    ) {
        Processor processor = createDefaultProcessor(build, launcher, listener);
        processor.addRule(new MatrixRule());
        return processor;
    }

}
