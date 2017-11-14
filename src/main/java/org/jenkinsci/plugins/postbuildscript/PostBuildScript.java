package org.jenkinsci.plugins.postbuildscript;

import hudson.Extension;
import hudson.Launcher;
import hudson.matrix.MatrixAggregatable;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Job;
import hudson.model.Result;
import hudson.tasks.BuildStep;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import org.jenkinsci.plugins.postbuildscript.model.Configuration;
import org.jenkinsci.plugins.postbuildscript.model.PostBuildItem;
import org.jenkinsci.plugins.postbuildscript.model.PostBuildStep;
import org.jenkinsci.plugins.postbuildscript.model.Script;
import org.jenkinsci.plugins.postbuildscript.model.ScriptFile;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.jenkinsci.plugins.postbuildscript.ExecuteOn.BOTH;


/**
 * @author Gregory Boissinot
 */
public class PostBuildScript extends Notifier implements MatrixAggregatable {

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

    private ExecuteOn executeOn;

    @DataBoundConstructor
    public PostBuildScript(
        Collection<ScriptFile> genericScriptFiles,
        Collection<ScriptFile> groovyScriptFiles,
        Collection<Script> groovyScripts,
        Collection<PostBuildStep> buildSteps,
        boolean markBuildUnstable,
        ExecuteOn executeOn
    ) {

        if (genericScriptFiles != null) {
            config.addGenericScriptFiles(genericScriptFiles);
        }

        if (groovyScriptFiles != null) {
            config.addGroovyScriptFiles(groovyScriptFiles);
        }

        if (groovyScripts != null) {
            config.addGroovyScripts(groovyScripts);
        }

        if (buildSteps != null) {
            config.addBuildSteps(buildSteps);
        }

        config.setMarkBuildUnstable(markBuildUnstable);

        this.executeOn = executeOn;

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
    public hudson.matrix.MatrixAggregator createAggregator(
        MatrixBuild matrixBuild,
        Launcher launcher,
        BuildListener buildListener
    ) {
        ProcessorFactory processorFactory = new ProcessorFactory(config);
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

        if (config == null) {
            enrichConfigWithDeprecatedFields();
        }

        ProcessorFactory processorFactory = new ProcessorFactory(config);
        Processor processor = processorFactory.create(build, launcher, listener);

        Job<?, ?> job = build.getProject();
        boolean axe = job instanceof MatrixConfiguration;
        if (axe && executeOn.axes()) {     // matrix axe, and set to execute on axes' nodes
            return processor.process();
        }

        return axe || processor.process();
    }

    private void enrichConfigWithDeprecatedFields() {
        addGenericScriptFileList();
        addGroovyScriptFileList();
        addGroovyScriptContentList();
        addBuildSteps();

        if (markBuildUnstable != null && markBuildUnstable) {
            createConfigurationIfNotPresent();
            config.setMarkBuildUnstable(markBuildUnstable);
        }

    }

    public List<? extends ScriptFile> getGenericScriptFiles() {
        if (config == null && genericScriptFileList != null) {
            applyResult(genericScriptFileList);
            return Collections.unmodifiableList(genericScriptFileList);
        }
        createConfigurationIfNotPresent();
        return config.getGenericScriptFiles();
    }

    public List<? extends ScriptFile> getGroovyScriptFiles() {
        if (config == null && groovyScriptFileList != null) {
            applyResult(groovyScriptFileList);
            return Collections.unmodifiableList(groovyScriptFileList);
        }
        createConfigurationIfNotPresent();
        return config.getGroovyScriptFiles();
    }

    public List<? extends Script> getGroovyScripts() {
        if (config == null && groovyScriptContentList != null) {
            applyResult(groovyScriptContentList);
            return Collections.unmodifiableList(groovyScriptContentList);
        }
        createConfigurationIfNotPresent();
        return config.getGroovyScripts();
    }

    public List<PostBuildStep> getBuildSteps() {
        if (config == null && buildSteps != null) {
            List<PostBuildStep> buildSteps = new ArrayList<>(this.buildSteps.size());
            for (BuildStep step : this.buildSteps) {

                Set<String> results = migrateResults();
                PostBuildStep postBuildStep = new PostBuildStep(
                    Collections.singletonList(step),
                    results
                );

                buildSteps.add(postBuildStep);

            }
            return buildSteps;
        }
        createConfigurationIfNotPresent();
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
        return results;
    }

    private void addBuildSteps() {
        if (buildSteps != null && !buildSteps.isEmpty()) {
            createConfigurationIfNotPresent();
            for (BuildStep step : buildSteps) {
                List<BuildStep> steps = Collections.singletonList(step);
                Set<String> results = migrateResults();
                addBuildStep(steps, results);
            }
        }
    }

    private void addGroovyScriptContentList() {
        if (groovyScriptContentList != null && !groovyScriptContentList.isEmpty()) {
            createConfigurationIfNotPresent();
            config.addGroovyScripts(groovyScriptContentList);
            applyResult(groovyScriptContentList);
        }
    }

    private void addGroovyScriptFileList() {
        if (groovyScriptFileList != null && !groovyScriptFileList.isEmpty()) {
            createConfigurationIfNotPresent();
            config.addGroovyScriptFiles(groovyScriptFileList);
            applyResult(groovyScriptFileList);
        }
    }

    private void addGenericScriptFileList() {
        if (genericScriptFileList != null && !genericScriptFileList.isEmpty()) {
            createConfigurationIfNotPresent();
            config.addGenericScriptFiles(genericScriptFileList);
            applyResult(genericScriptFileList);
        }
    }

    private void createConfigurationIfNotPresent() {
        if (config == null) {
            config = new Configuration();
        }
    }

    private void addBuildStep(
        List<BuildStep> steps,
        Set<String> results
    ) {
        createConfigurationIfNotPresent();
        config.addBuildStep(new PostBuildStep(steps, results));
    }

    public boolean isMarkBuildUnstable() {
        if (config == null && markBuildUnstable != null) {
            return markBuildUnstable;
        }
        createConfigurationIfNotPresent();
        return config.isMarkBuildUnstable();
    }

    public ExecuteOn getExecuteOn() {
        return executeOn;
    }

    public Object readResolve() {
        if (executeOn == null) {
            executeOn = BOTH;
        }
        return this;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

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

        public boolean isMatrixProject(Object job) {
            return job instanceof MatrixProject;
        }

    }

}

