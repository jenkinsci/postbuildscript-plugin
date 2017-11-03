package org.jenkinsci.plugins.postbuildscript;

import java.io.File;
import java.io.IOException;
import java.util.List;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.matrix.MatrixAggregatable;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.tasks.BuildStep;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.ListBoxModel;
import org.jenkinsci.plugins.postbuildscript.service.ScriptExecutor;
import org.kohsuke.stapler.DataBoundConstructor;

import static org.jenkinsci.plugins.postbuildscript.ExecuteOn.BOTH;


/**
 * @author Gregory Boissinot
 */
public class PostBuildScript extends Notifier implements MatrixAggregatable {

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    private final List<GenericScript> genericScriptFileList;
    private final List<GroovyScriptFile> groovyScriptFileList;
    private final List<GroovyScriptContent> groovyScriptContentList;
    private final List<PostBuildStep> buildSteps;

    private final boolean markBuildUnstable;
    private ExecuteOn executeOn;

    @DataBoundConstructor
    public PostBuildScript(List<GenericScript> genericScriptFile,
        List<GroovyScriptFile> groovyScriptFile,
        List<GroovyScriptContent> groovyScriptContent,
        boolean markBuildUnstable,
        ExecuteOn executeOn,
        List<PostBuildStep> buildStep) {
        genericScriptFileList = genericScriptFile;
        groovyScriptFileList = groovyScriptFile;
        groovyScriptContentList = groovyScriptContent;
        buildSteps = buildStep;
        this.markBuildUnstable = markBuildUnstable;
        this.executeOn = executeOn;
    }

    @Override
    public MatrixAggregator createAggregator(MatrixBuild build, Launcher launcher, BuildListener listener) {
        return new MatrixAggregator(build, launcher, listener) {

            @Override
            public boolean endBuild() throws InterruptedException, IOException {
                return !executeOn.matrix() || _perform(build, launcher, listener);
            }
        };
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) throws InterruptedException, IOException {
        Job<?, ?> job = build.getProject();

        boolean axe = isMatrixAxe(job);
        if (axe && executeOn.axes()) {     // matrix axe, and set to execute on axes' nodes
            return _perform(build, launcher, listener);
        }

        return axe || _perform(build, launcher, listener);
    }

    private boolean _perform(AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) {

        log(listener, "Executing post build scripts.");

        ScriptExecutor executor = new ScriptExecutor(
            new PostBuildScriptLog(listener),
            listener
        );

        try {

            return processScripts(executor, build, launcher, listener);

        } catch (PostBuildScriptException pse) {
            log(listener, "[Error] - Problems occured: %s", pse.getMessage());
            build.setResult(Result.FAILURE);
            return false;
        }
    }

    private boolean processScripts(ScriptExecutor executor, AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws PostBuildScriptException {

        //Execute Generic scripts file
        if (genericScriptFileList != null) {
            boolean result = processGenericScriptList(executor, build, launcher, listener);
            if (!result) {
                return setBuildStepsResult(build);
            }
        }

        //Execute Groovy scripts file
        if (groovyScriptFileList != null) {
            boolean result = processGroovyScriptFileList(executor, build, listener);
            if (!result) {
                return setBuildStepsResult(build);
            }
        }

        //Execute Groovy scripts content
        if (groovyScriptContentList != null) {
            boolean result = processGroovyScriptContentList(executor, build, listener);
            if (!result) {
                return setBuildStepsResult(build);
            }
        }

        //Execute Build steps
        if (buildSteps != null) {
            boolean result = processBuildSteps(build, launcher, listener);
            if (!result) {
                return setBuildStepsResult(build);
            }
        }

        return true;
    }

    private boolean processGenericScriptList(ScriptExecutor executor, AbstractBuild<?, ?> build, Launcher launcher, TaskListener listener)
        throws PostBuildScriptException {

        if (genericScriptFileList == null)
            throw new AssertionError();

        Result result = build.getResult();
        FilePath workspace = build.getWorkspace();
        for (GenericScript script : genericScriptFileList) {
            String filePath = script.getFilePath();
            if (filePath == null || filePath.trim().isEmpty()) {
                log(listener,"No filepath provided for script file #%s", genericScriptFileList.indexOf(script));
                continue;
            }

            Result targetResult = script.getTargetResult();
            if (result == null || result.equals(targetResult)) {
                String scriptPath = getResolvedPath(filePath, build, listener);
                if (scriptPath != null) {
                    int cmd = executor.executeScriptPathAndGetExitCode(workspace, scriptPath, launcher);
                    if (cmd != 0) {
                        return false;
                    }
                }
            } else {
                logSkippingOfExecution(listener, filePath, targetResult);
            }


        }
        return true;
    }

    private static void logSkippingOfExecution(TaskListener listener, String scriptName, Result targetResult) {
        log(listener, "Build does not have result \"%s\". Did not execute %s", targetResult, scriptName);
    }

    private static void log(TaskListener listener, String message, Object... args) {
        listener.getLogger().printf("[PostBuildScript] - %s%n", String.format(message, args));
    }

    private boolean processGroovyScriptFileList(ScriptExecutor executor, AbstractBuild<?, ?> build, TaskListener listener)
        throws PostBuildScriptException {

        Result result = build.getResult();
        FilePath workspace = build.getWorkspace();
        for (GroovyScriptFile groovyScript : groovyScriptFileList) {

            String filePath = groovyScript.getFilePath();
            if (filePath == null || filePath.trim().isEmpty()) {
                log(listener,"No filepath provided for script file #%s", groovyScriptFileList.indexOf(groovyScript));
                continue;
            }

            Result targetResult = groovyScript.getTargetResult();
            if (result == null || result.equals(targetResult)) {
                String groovyPath = getResolvedPath(groovyScript.getFilePath(), build, listener);
                if (groovyPath != null) {
                    if (!executor.performGroovyScriptFile(workspace, groovyPath)) {
                        return false;
                    }
                }
            } else {
                logSkippingOfExecution(listener, groovyScript.getFilePath(), targetResult);
            }

        }
        return true;
    }

    private boolean processGroovyScriptContentList(ScriptExecutor executor, AbstractBuild<?, ?> build, TaskListener listener) {

        FilePath workspace = build.getWorkspace();

        Result result = build.getResult();
        for (GroovyScriptContent groovyScript : groovyScriptContentList) {

            Result targetResult = groovyScript.getTargetResult();
            if (result == null || result.equals(targetResult)) {
                String content = groovyScript.getContent();
                if (content != null) {
                    if (!executor.performGroovyScript(workspace, content)) {
                        return false;
                    }
                }
            } else {
                logSkippingOfExecution(listener, "Groovy script #" + groovyScriptContentList.indexOf(groovyScript), targetResult);
            }

        }
        return true;
    }

    private boolean processBuildSteps(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws PostBuildScriptException {

        Result result = build.getResult();

        try {
            for (PostBuildStep bs : buildSteps) {

                Result targetResult = bs.getTargetResult();
                if (result == null || result.equals(targetResult)) {
                    for (BuildStep buildStep : bs.getBuildSteps()) {
                        if (!buildStep.perform(build, launcher, listener)) {
                            return false;
                        }
                    }
                } else {
                    logSkippingOfExecution(listener, "build step #" + buildSteps.indexOf(bs), targetResult);
                }

            }
            return true;
        } catch (IOException ioe) {
            throw new PostBuildScriptException(ioe);
        } catch (InterruptedException ie) {
            throw new PostBuildScriptException(ie);
        }
    }

    @SuppressWarnings("unchecked")
    private String getResolvedPath(String path, AbstractBuild<?, ?> build, TaskListener listener) throws PostBuildScriptException {
        if (path == null) {
            return null;
        }

        String resolvedPath;
        try {
            resolvedPath = Util.replaceMacro(path, build.getEnvironment(listener));
            resolvedPath = Util.replaceMacro(resolvedPath, build.getBuildVariables());
            return resolvedPath;
        } catch (IOException ioe) {
            throw new PostBuildScriptException(ioe);
        } catch (InterruptedException ie) {
            throw new PostBuildScriptException(ie);
        }
    }

    private void setFailedResult(AbstractBuild build) {
        build.setResult(Result.FAILURE);
    }

    private void setUnstableResult(AbstractBuild build) {
        build.setResult(Result.UNSTABLE);
    }

    private boolean setBuildStepsResult(AbstractBuild build) {
        if (isMarkBuildUnstable()) {
            setUnstableResult(build);
            return true;
        }
        setFailedResult(build);
        return false;
    }

    @SuppressWarnings("unused")
    public List<GenericScript> getGenericScriptFileList() {
        return genericScriptFileList;
    }

    @SuppressWarnings("unused")
    public List<GroovyScriptFile> getGroovyScriptFileList() {
        return groovyScriptFileList;
    }

    @SuppressWarnings("unused")
    public List<GroovyScriptContent> getGroovyScriptContentList() {
        return groovyScriptContentList;
    }

    @SuppressWarnings("unused")
    public List<PostBuildStep> getBuildSteps() {
        return buildSteps;
    }

    @SuppressWarnings("unused")
    public boolean isMarkBuildUnstable() {
        return markBuildUnstable;
    }

    @SuppressWarnings("unused")
    public ExecuteOn getExecuteOn() {
        return executeOn;
    }

    @Extension(ordinal = 99)
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
            return PostBuildScript.isMatrixProject(job);
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

    private static boolean isMatrixProject(Object job) {
        return job instanceof MatrixProject;
    }

    private static boolean isMatrixAxe(Job job) {
        return job instanceof MatrixConfiguration;
    }

    @SuppressWarnings({"unused", "deprecation"})
    public Object readResolve() {
        if (executeOn == null)
            executeOn = BOTH;

        return this;
    }

    private boolean isUnix() {
        return File.pathSeparatorChar == ':';
    }
}

