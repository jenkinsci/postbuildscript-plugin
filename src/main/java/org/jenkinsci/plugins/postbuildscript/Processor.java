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
import org.jenkinsci.plugins.postbuildscript.service.CommandExecutor;
import org.jenkinsci.plugins.postbuildscript.service.GroovyScriptExecutor;

import java.io.IOException;
import java.util.Set;

public class Processor {

    private final AbstractBuild<?, ?> build;
    private final Launcher launcher;
    private final BuildListener listener;
    private final Configuration config;
    private final Logger logger;

    public Processor(
        AbstractBuild<?, ?> build,
        Launcher launcher,
        BuildListener listener,
        Configuration config) {
        this.build = build;
        this.launcher = launcher;
        this.listener = listener;
        this.config = config;
        logger = new Logger(listener);
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
        logger.info(Messages.PostBuildScript_BuildDoesNotHaveAnyOfTheResults(targetResult, scriptName));
    }

    public boolean process() {
        logger.info(Messages.PostBuildScript_ExecutingPostBuildScripts());
        try {
            return processScripts();
        } catch (PostBuildScriptException pse) {
            logger.error(Messages.PostBuildScript_ProblemOccured(pse.getMessage()));
            build.setResult(Result.FAILURE);
            return false;
        }
    }

    private boolean processScripts() throws PostBuildScriptException {

        if (!processGenericScriptList()) {
            return setBuildStepsResult();
        }

        if (!processGroovyScriptFileList()) {
            return setBuildStepsResult();
        }

        if (!processGroovyScripts()) {
            return setBuildStepsResult();
        }

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
        CommandExecutor executor = new CommandExecutor(logger, listener, workspace, launcher);
        for (ScriptFile script : config.getGenericScriptFiles()) {
            String filePath = script.getFilePath();
            if (Strings.nullToEmpty(filePath).trim().isEmpty()) {
                logger.error(Messages.PostBuildScript_NoFilePathProvided(config.genericScriptFileIndexOf(script)));
                continue;
            }

            if (!result.isPresent() || script.shouldBeExecuted(result.get().toString())) {
                String scriptPath = getResolvedPath(filePath, build, listener);
                if (scriptPath != null) {
                    int cmd = executor.executeCommand(scriptPath);
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
        GroovyScriptExecutor executor = new GroovyScriptExecutor(logger);
        for (ScriptFile script : config.getGroovyScriptFiles()) {

            String filePath = script.getFilePath();

            if (Strings.nullToEmpty(filePath).trim().isEmpty()) {
                logger.error(Messages.PostBuildScript_NoFilePathProvided(config.groovyScriptFileIndexOf(script)));
                continue;
            }

            if (!result.isPresent() || script.shouldBeExecuted(result.get().toString())) {
                String groovyPath = getResolvedPath(filePath, build, listener);
                if (groovyPath != null) {
                    if (!executor.performGroovyScriptFile(build, groovyPath)) {
                        return false;
                    }
                }
            } else {
                logSkippingOfExecution(filePath, script.getResults());
            }

        }
        return true;
    }

    private boolean processGroovyScripts() {

        Optional<Result> result = Optional.fromNullable(build.getResult());
        GroovyScriptExecutor executor = new GroovyScriptExecutor(logger);
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
