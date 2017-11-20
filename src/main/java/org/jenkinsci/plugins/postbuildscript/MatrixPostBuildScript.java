package org.jenkinsci.plugins.postbuildscript;

import hudson.Extension;
import hudson.Launcher;
import hudson.matrix.MatrixAggregatable;
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
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;

import static org.jenkinsci.plugins.postbuildscript.ExecuteOn.BOTH;

public class MatrixPostBuildScript extends PostBuildScript implements MatrixAggregatable {

    private ExecuteOn executeOn;

    @DataBoundConstructor
    public MatrixPostBuildScript(Collection<ScriptFile> genericScriptFiles, Collection<ScriptFile> groovyScriptFiles, Collection<Script> groovyScripts, Collection<PostBuildStep> buildSteps, boolean markBuildUnstable, ExecuteOn executeOn) {
        super(genericScriptFiles, groovyScriptFiles, groovyScripts, buildSteps, markBuildUnstable);

        this.executeOn = executeOn;
    }

    @Override
    public hudson.matrix.MatrixAggregator createAggregator(
        MatrixBuild matrixBuild,
        Launcher launcher,
        BuildListener buildListener
    ) {
        ProcessorFactory processorFactory = createProcessorFactory();
        return new ConfigurableMatrixAggregator(
            matrixBuild,
            launcher,
            buildListener,
            processorFactory,
            executeOn
        );
    }

    @Override
    public boolean perform(
        AbstractBuild<?, ?> build,
        Launcher launcher,
        BuildListener listener
    ) throws InterruptedException, IOException {

        Processor processor = createProcessor(build, launcher, listener);

        if (executeOn.axes()) {
            return processor.process();
        }

        return true;
    }

    public ExecuteOn getExecuteOn() {
        return executeOn;
    }

    public Object readResolve() {
        super.readResolve();
        if (executeOn == null) {
            executeOn = BOTH;
        }
        return this;
    }

    @Extension(optional = true)
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Nonnull
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

        public boolean isMatrixProject(Object job) {
            return job instanceof MatrixProject;
        }

    }


}
