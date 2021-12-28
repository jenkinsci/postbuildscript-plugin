package org.jenkinsci.plugins.postbuildscript;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import org.jenkinsci.plugins.postbuildscript.model.PostBuildStep;
import org.jenkinsci.plugins.postbuildscript.model.Script;
import org.jenkinsci.plugins.postbuildscript.model.ScriptFile;
import org.jenkinsci.plugins.postbuildscript.processor.Processor;
import org.jenkinsci.plugins.postbuildscript.processor.ProcessorFactory;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Collection;

public class MatrixPostBuildScript extends PostBuildScript {

    @DataBoundConstructor
    public MatrixPostBuildScript(
        Collection<ScriptFile> genericScriptFiles,
        Collection<ScriptFile> groovyScriptFiles,
        Collection<Script> groovyScripts,
        Collection<PostBuildStep> buildSteps,
        boolean markBuildUnstable
    ) {
        super(genericScriptFiles, groovyScriptFiles, groovyScripts, buildSteps, markBuildUnstable);
    }

    @Override
    public MatrixAggregator createAggregator(
        MatrixBuild build,
        Launcher launcher,
        BuildListener listener
    ) {
        ProcessorFactory processorFactory = createProcessorFactory();
        return new ConfigurableMatrixAggregator(
            build,
            launcher,
            listener,
            processorFactory,
            getClass()
        );
    }

    @Override
    public boolean perform(
        AbstractBuild<?, ?> build,
        Launcher launcher,
        BuildListener listener
    ) {
        ProcessorFactory processorFactory = createProcessorFactory();
        Processor processor = processorFactory.createMatrixProcessor(build, launcher, listener);
        return processor.process();
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.MatrixPostBuildScript_DisplayName();
        }

        @Override
        public String getHelpFile() {
            return "/plugin/postbuildscript/help/postbuildscript.html"; //NON-NLS
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return MatrixProject.class.isAssignableFrom(jobType);
        }
    }

}
