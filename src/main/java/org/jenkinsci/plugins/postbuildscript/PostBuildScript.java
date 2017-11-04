package org.jenkinsci.plugins.postbuildscript;

import com.google.common.base.Optional;
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
import hudson.util.ListBoxModel;
import org.jenkinsci.plugins.postbuildscript.model.PostBuildItem;
import org.jenkinsci.plugins.postbuildscript.model.PostBuildStep;
import org.jenkinsci.plugins.postbuildscript.model.Script;
import org.jenkinsci.plugins.postbuildscript.model.ScriptFile;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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

    private static void applyResult(
        Boolean flag,
        Iterable<? extends PostBuildItem> postBuildItems,
        Result result
    ) {
        Optional<Boolean> optional = Optional.fromNullable(flag);
        if (optional.isPresent() && optional.get()) {
            for (PostBuildItem postBuildItem : postBuildItems) {
                if (!postBuildItem.hasResult()) {
                    postBuildItem.setResult(result.toString());
                }
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
        return new MatrixAggregator(
            matrixBuild,
            launcher,
            buildListener,
            config,
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

        Processor processor = ProcessorFactory.create(
            build, launcher, listener, config
        );

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

        if (markBuildUnstable != null) {
            createNewConfigurationIfNotPresent();
            config.setMarkBuildUnstable(markBuildUnstable);
        }

    }

    public List<? extends ScriptFile> getGenericScriptFiles() {
        if (config == null && genericScriptFileList != null) {
            return genericScriptFileList;
        }
        createNewConfigurationIfNotPresent();
        return config.getGenericScriptFiles();
    }

    public List<? extends ScriptFile> getGroovyScriptFiles() {
        if (config == null && groovyScriptFileList != null) {
            return groovyScriptFileList;
        }
        createNewConfigurationIfNotPresent();
        return config.getGroovyScriptFiles();
    }

    public List<? extends Script> getGroovyScripts() {
        if (config == null && groovyScriptContentList != null) {
            return groovyScriptContentList;
        }
        createNewConfigurationIfNotPresent();
        return config.getGroovyScripts();
    }

    public List<PostBuildStep> getBuildSteps() {
        if (config == null && buildSteps != null) {
            List<PostBuildStep> buildSteps = new ArrayList<>(this.buildSteps.size());
            for (BuildStep step : this.buildSteps) {

                String result = Result.SUCCESS.toString();
                if (scriptOnlyIfFailure != null && scriptOnlyIfFailure) {
                    result = Result.FAILURE.toString();
                }

                PostBuildStep postBuildStep = new PostBuildStep(
                    Collections.singletonList(step),
                    result
                );

                buildSteps.add(postBuildStep);

            }
            return buildSteps;
        }
        createNewConfigurationIfNotPresent();
        return config.getBuildSteps();
    }

    private void addBuildSteps() {
        if (buildSteps != null && !buildSteps.isEmpty()) {
            createNewConfigurationIfNotPresent();
            for (BuildStep step : buildSteps) {
                List<BuildStep> steps = Collections.singletonList(step);
                addBuildStep(steps, scriptOnlyIfSuccess, Result.SUCCESS);
                addBuildStep(steps, scriptOnlyIfFailure, Result.FAILURE);
            }
        }
    }

    private void addGroovyScriptContentList() {
        if (groovyScriptContentList != null && !groovyScriptContentList.isEmpty()) {
            createNewConfigurationIfNotPresent();
            config.addGroovyScripts(groovyScriptContentList);
            applyResult(scriptOnlyIfSuccess, groovyScriptContentList, Result.SUCCESS);
            applyResult(scriptOnlyIfFailure, groovyScriptContentList, Result.FAILURE);
        }
    }

    private void addGroovyScriptFileList() {
        if (groovyScriptFileList != null && !groovyScriptFileList.isEmpty()) {
            createNewConfigurationIfNotPresent();
            config.addGroovyScriptFiles(groovyScriptFileList);
            applyResult(scriptOnlyIfSuccess, groovyScriptFileList, Result.SUCCESS);
            applyResult(scriptOnlyIfFailure, groovyScriptFileList, Result.FAILURE);
        }
    }

    private void addGenericScriptFileList() {
        if (genericScriptFileList != null && !genericScriptFileList.isEmpty()) {
            createNewConfigurationIfNotPresent();
            config.addGenericScriptFiles(genericScriptFileList);
            applyResult(scriptOnlyIfSuccess, genericScriptFileList, Result.SUCCESS);
            applyResult(scriptOnlyIfFailure, genericScriptFileList, Result.FAILURE);
        }
    }

    private void createNewConfigurationIfNotPresent() {
        if (config == null) {
            config = new Configuration();
        }
    }

    private void addBuildStep(
        List<BuildStep> steps,
        Boolean flag,
        Result result
    ) {
        Optional<Boolean> optional = Optional.fromNullable(flag);
        if (optional.isPresent() && optional.get()) {
            createNewConfigurationIfNotPresent();
            config.addBuildStep(new PostBuildStep(steps, result.toString()));
        }
    }

    public boolean isMarkBuildUnstable() {
        if (config == null && markBuildUnstable != null) {
            return markBuildUnstable;
        }
        createNewConfigurationIfNotPresent();
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
            return "Execute a set of scripts";
        }

        @Override
        public String getHelpFile() {
            return "/plugin/postbuildscript/help.html";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        public boolean isMatrixProject(Object job) {
            return job instanceof MatrixProject;
        }

        public ListBoxModel doFillResultItems() {
            ListBoxModel items = new ListBoxModel();
            items.add(Result.SUCCESS.toString());
            items.add(Result.UNSTABLE.toString());
            items.add(Result.FAILURE.toString());
            items.add(Result.NOT_BUILT.toString());
            items.add(Result.ABORTED.toString());
            return items;
        }

    }

}

