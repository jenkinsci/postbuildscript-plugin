package org.jenkinsci.plugins.postbuildscript;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.tasks.BuildStep;
import org.jenkinsci.plugins.postbuildscript.model.Configuration;
import org.jenkinsci.plugins.postbuildscript.model.PostBuildStep;
import org.jenkinsci.plugins.postbuildscript.model.Script;
import org.jenkinsci.plugins.postbuildscript.model.ScriptFile;
import org.jenkinsci.plugins.postbuildscript.service.ScriptExecutor;

import java.io.IOException;
import java.util.Set;

public class Processor {

    private final AbstractBuild<?, ?> build;
    private final Launcher launcher;
    private final BuildListener listener;
    private final Configuration config;
    private final ScriptExecutor executor;

    public Processor(
        AbstractBuild<?, ?> build,
        Launcher launcher,
        BuildListener listener,
        Configuration config) {
        this.build = build;
        this.launcher = launcher;
        this.listener = listener;
        this.config = config;
        executor = new ScriptExecutor(
            new Logger(listener),
            listener
        );
    }

    private void logError(String message) {
        log(Messages.PostBuildScript_ErrorPrefix(message));
    }

    private void log(String message) {
        listener.getLogger().println(Messages.PostBuildScript_LogPrefix(message));
    }

    private static String getResolvedPath(
        String path,
        AbstractBuild<?, ?> build,
        TaskListener listener
    ) throws PostBuildScriptException {
        if (path == null) {
            return null;
        }

        try {
            String resolvedPath = Util.replaceMacro(path, build.getEnvironment(listener));
            resolvedPath = Util.replaceMacro(resolvedPath, build.getBuildVariables());
            return resolvedPath;
        } catch (IOException | InterruptedException ioe) {
            throw new PostBuildScriptException(ioe);
        }
    }

    private void logSkippingOfExecution(String scriptName, Set<String> targetResult) {
        log(Messages.PostBuildScript_BuildDoesNotHaveAnyOfTheResults(targetResult, scriptName));
    }

    public boolean process() {
        log(Messages.PostBuildScript_ExecutingPostBuildScripts());
        try {
            return processScripts();
        } catch (PostBuildScriptException pse) {
            logError(Messages.PostBuildScript_ProblemOccured(pse.getMessage()));
            build.setResult(Result.FAILURE);
            return false;
        }
    }

    private boolean processScripts() throws PostBuildScriptException {

        //Execute Generic scripts file
        if (!processGenericScriptList()) {
            return setBuildStepsResult();
        }

        //Execute Groovy scripts file
        if (!processGroovyScriptFileList()) {
            return setBuildStepsResult();
        }

        //Execute Groovy scripts content
        if (!processGroovyScriptContentList()) {
            return setBuildStepsResult();
        }

        //Execute Build steps
        return processBuildSteps() || setBuildStepsResult();

    }

    private boolean setBuildStepsResult() {
        if (config.isMarkBuildUnstable()) {
            setUnstableResult();
            return true;
        }
        setFailedResult();
        return false;
    }

    private void setFailedResult() {
        build.setResult(Result.FAILURE);
    }

    private void setUnstableResult() {
        build.setResult(Result.UNSTABLE);
    }

    private boolean processGenericScriptList()
        throws PostBuildScriptException {

        Optional<Result> result = Optional.fromNullable(build.getResult());
        FilePath workspace = build.getWorkspace();
        for (ScriptFile script : config.getGenericScriptFiles()) {
            String filePath = script.getFilePath();
            if (Strings.nullToEmpty(filePath).trim().isEmpty()) {
                logError(Messages.PostBuildScript_NoFilePathProvided(config.genericScriptFileIndexOf(script)));
                continue;
            }

            if (!result.isPresent() || script.shouldBeExecuted(result.get().toString())) {
                String scriptPath = getResolvedPath(filePath, build, listener);
                if (scriptPath != null) {
                    int cmd = executor.executeScriptPathAndGetExitCode(workspace, scriptPath, launcher);
                    if (cmd != 0) {
                        return false;
                    }
                }
            } else {
                logSkippingOfExecution(filePath, script.getResults());
            }


        }
        return true;
    }

    private boolean processGroovyScriptFileList()
        throws PostBuildScriptException {

        Optional<Result> result = Optional.fromNullable(build.getResult());
        for (ScriptFile script : config.getGroovyScriptFiles()) {

            String filePath = script.getFilePath();

            if (Strings.nullToEmpty(filePath).trim().isEmpty()) {
                logError(Messages.PostBuildScript_NoFilePathProvided(config.groovyScriptFileIndexOf(script)));
                continue;
            }

            if (!result.isPresent() || script.shouldBeExecuted(result.get().toString())) {
                String groovyPath = getResolvedPath(script.getFilePath(), build, listener);
                if (groovyPath != null) {
                    if (!executor.performGroovyScriptFile(build, groovyPath)) {
                        return false;
                    }
                }
            } else {
                logSkippingOfExecution(script.getFilePath(), script.getResults());
            }

        }
        return true;
    }

    private boolean processGroovyScriptContentList() {

        Optional<Result> result = Optional.fromNullable(build.getResult());
        for (Script script : config.getGroovyScripts()) {

            if (!result.isPresent() || script.shouldBeExecuted(result.get().toString())) {
                String content = script.getContent();
                if (content != null) {
                    if (!executor.performGroovyScript(build, content)) {
                        return false;
                    }
                }
            } else {
                logSkippingOfExecution(Messages.PostBuildScript_GroovyScript(config.groovyScriptIndexOf(script)),
                    script.getResults()
                );
            }

        }
        return true;
    }

    private boolean processBuildSteps() throws PostBuildScriptException {

        Optional<Result> result = Optional.fromNullable(build.getResult());
        try {
            for (PostBuildStep postBuildStep : config.getBuildSteps()) {

                if (!result.isPresent() || postBuildStep.shouldBeExecuted(result.get().toString())) {
                    for (BuildStep buildStep : postBuildStep.getBuildSteps()) {
                        if (!buildStep.perform(build, launcher, listener)) {
                            return false;
                        }
                    }
                } else {
                    logSkippingOfExecution(
                        Messages.PostBuildScript_BuildStep(config.buildStepIndexOf(postBuildStep)),
                        postBuildStep.getResults()
                    );
                }

            }
            return true;
        } catch (IOException | InterruptedException ioe) {
            throw new PostBuildScriptException(ioe);
        }
    }

}
