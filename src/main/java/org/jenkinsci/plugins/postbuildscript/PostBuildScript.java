package org.jenkinsci.plugins.postbuildscript;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
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
import org.jenkinsci.plugins.postbuildscript.model.ScriptType;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * @author Gregory Boissinot
 */
public class PostBuildScript extends Notifier {

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
    ) throws InterruptedException, IOException {

        Processor processor = createProcessor(build, launcher, listener);
        return processor.process();

    }

    Processor createProcessor(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        ProcessorFactory processorFactory = createProcessorFactory();
        return processorFactory.create(build, launcher, listener);
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
        config.addBuildStep(new PostBuildStep(results, steps));
    }

    public boolean isMarkBuildUnstable() {
        return config.isMarkBuildUnstable();
    }

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

        return this;
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

