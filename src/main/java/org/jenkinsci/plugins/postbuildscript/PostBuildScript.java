package org.jenkinsci.plugins.postbuildscript;

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

import javax.annotation.Nonnull;
import java.util.*;


/**
 * @author Gregory Boissinot
 */
public class PostBuildScript extends Notifier
    // MatrixAggregatable is needed for migration of old plugin version configurations (< 1.0.0), see JENKINS-53691
    // TODO Remove, if there are no 0.18.x installations left
    implements MatrixAggregatable {

    private Configuration config = new Configuration();

    @Deprecated
    private List<GenericScript> genericScriptFileList;

    @Deprecated
    private List<GroovyScriptFile> groovyScriptFileList;

    @Deprecated
    private List<GroovyScriptContent> groovyScriptContentList;

    @Deprecated
    private List<BuildStep> buildSteps;

    @Deprecated
    private Boolean scriptOnlyIfSuccess;

    @Deprecated
    private Boolean scriptOnlyIfFailure;

    @Deprecated
    private Boolean markBuildUnstable;

    // TODO Remove, if there are no 0.18.x installations left
    /**
     * needed for migration (JENKINS-53691)
     *
     * @deprecated can now be selected individually for each step
     */
    @Deprecated
    private ExecuteOn executeOn;

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

    private void applyResult(Iterable<? extends PostBuildItem> postBuildItems) {
        Set<String> results = migrateResults();
        for (PostBuildItem postBuildItem : postBuildItems) {
            if (!postBuildItem.hasResult()) {
                postBuildItem.addResults(results);
            }
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

    /**
     * @deprecated needed for migration (JENKINS-53691)
     */
    @Deprecated
    private static org.jenkinsci.plugins.postbuildscript.model.ExecuteOn translate(ExecuteOn executeOn) {
        return org.jenkinsci.plugins.postbuildscript.model.ExecuteOn.valueOf(executeOn.name());
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

    private Set<String> migrateResults() {
        Set<String> results = new HashSet<>();
        if (scriptOnlyIfFailure != null && scriptOnlyIfFailure) {
            results.add(Result.FAILURE.toString());
        }
        if (scriptOnlyIfSuccess != null && scriptOnlyIfSuccess) {
            results.add(Result.SUCCESS.toString());
        }
        if (scriptOnlyIfFailure != null && scriptOnlyIfSuccess != null && !scriptOnlyIfSuccess && !scriptOnlyIfFailure) {
            results.add(Result.SUCCESS.toString());
            results.add(Result.UNSTABLE.toString());
            results.add(Result.FAILURE.toString());
            results.add(Result.NOT_BUILT.toString());
            results.add(Result.ABORTED.toString());
        }
        return results;
    }

    private void migrateBuildSteps() {
        if (buildSteps != null && !buildSteps.isEmpty()) {
            for (BuildStep step : buildSteps) {
                List<BuildStep> steps = Collections.singletonList(step);
                Set<String> results = migrateResults();
                addBuildStep(steps, results);
            }
        }
    }

    private void migrateGroovyScriptContentList() {
        if (groovyScriptContentList != null && !groovyScriptContentList.isEmpty()) {
            config.addGroovyScripts(groovyScriptContentList);
            applyResult(groovyScriptContentList);
        }
    }

    private void migrateScriptFileList(Collection<? extends ScriptFile> scriptFiles, ScriptType scriptType) {
        if (scriptFiles != null && !scriptFiles.isEmpty()) {
            addScriptFiles(scriptFiles, scriptType);
            applyResult(scriptFiles);
        }
    }

    private void addBuildStep(
        List<BuildStep> steps,
        Set<String> results
    ) {
        config.addBuildStep(new PostBuildStep(results, steps, false));
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

    // TODO Remove, if there are no 0.18.x installations left

    public Object readResolve() {
        if (config == null) {
            config = new Configuration();

            migrateScriptFileList(genericScriptFileList, ScriptType.GENERIC);
            migrateScriptFileList(groovyScriptFileList, ScriptType.GROOVY);
            migrateGroovyScriptContentList();
            migrateBuildSteps();

            if (markBuildUnstable != null) {
                config.setMarkBuildUnstable(markBuildUnstable);
            }
        }
        // needed for migration (JENKINS-53691)
        // TODO Remove, if there are no 0.18.x installations left
        if (executeOn != null) {
            applyExecuteOn(getGenericScriptFiles());
            applyExecuteOn(getGroovyScriptFiles());
            applyExecuteOn(getGroovyScripts());
            applyExecuteOn(getBuildSteps());
        }
        return this;
    }

    // TODO Remove, if there are no 0.18.x installations left

    /**
     * @deprecated needed for migration (JENKINS-53691)
     */
    @Deprecated
    private void applyExecuteOn(Iterable<? extends PostBuildItem> items) {
        for (PostBuildItem item : items) {
            item.setExecuteOn(translate(executeOn));
        }
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Nonnull
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

