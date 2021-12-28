package org.jenkinsci.plugins.postbuildscript;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Launcher;
import hudson.matrix.MatrixAggregatable;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.*;
import org.jenkinsci.plugins.postbuildscript.model.*;
import org.jenkinsci.plugins.postbuildscript.processor.Processor;
import org.jenkinsci.plugins.postbuildscript.processor.ProcessorFactory;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.*;


/**
 * @author Gregory Boissinot
 */
public class PostBuildScript extends Notifier
    // MatrixAggregatable is needed for migration of old plugin version configurations (< 1.0.0), see JENKINS-53691
    // TODO Remove, if there are no 0.18.x installations left
    implements MatrixAggregatable {

    private Configuration config = new Configuration();

    @DataBoundConstructor
    public PostBuildScript(
        Collection<ScriptFile> genericScriptFiles,
        Collection<ScriptFile> groovyScriptFiles,
        Collection<Script> groovyScripts,
        Collection<PostBuildStep> buildSteps,
        boolean markBuildUnstable
    ) {

        addScriptFiles(genericScriptFiles, ScriptType.GENERIC);
        addScriptFiles(groovyScriptFiles, ScriptType.GROOVY);

        if (groovyScripts != null) {
            config.addGroovyScripts(groovyScripts);
        }

        if (buildSteps != null) {
            config.addBuildSteps(buildSteps);
        }

        config.setMarkBuildUnstable(markBuildUnstable);

    }

    void addScriptFiles(Collection<? extends ScriptFile> scriptFiles, ScriptType scriptType) {
        if (scriptFiles != null) {
            for (ScriptFile genericScriptFile : scriptFiles) {
                genericScriptFile.setScriptType(scriptType);
            }
            config.addScriptFiles(scriptFiles);
        }
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(
        AbstractBuild<?, ?> build,
        Launcher launcher,
        BuildListener listener
    ) {
        Processor processor = createProcessor(build, launcher, listener);
        return processor.process();
    }

    ProcessorFactory createProcessorFactory() {
        return new ProcessorFactory(config);
    }

    public List<? extends ScriptFile> getGenericScriptFiles() {
        return config.getScriptFiles(ScriptType.GENERIC);
    }

    public List<? extends ScriptFile> getGroovyScriptFiles() {
        return config.getScriptFiles(ScriptType.GROOVY);
    }

    public List<? extends Script> getGroovyScripts() {
        return config.getGroovyScripts();
    }

    public List<PostBuildStep> getBuildSteps() {
        return config.getBuildSteps();
    }

    private Processor createProcessor(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        ProcessorFactory processorFactory = createProcessorFactory();
        // should be createDefaultProcessor, but createMatrixProcessor is currently needed for migration (JENKINS-53691)
        // TODO Remove, if there are no 0.18.x installations left
        return processorFactory.createMatrixProcessor(build, launcher, listener);
    }

    public boolean isMarkBuildUnstable() {
        return config.isMarkBuildUnstable();
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

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.PostBuildScript_DisplayName();
        }

        @Override
        public String getHelpFile() {
            return "/plugin/postbuildscript/help/postbuildscript.html"; //NON-NLS
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

    }

}

