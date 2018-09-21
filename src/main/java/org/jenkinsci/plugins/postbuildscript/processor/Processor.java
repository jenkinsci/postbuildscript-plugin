package org.jenkinsci.plugins.postbuildscript.processor;

import com.google.common.base.Strings;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStep;
import org.jenkinsci.plugins.postbuildscript.Logger;
import org.jenkinsci.plugins.postbuildscript.Messages;
import org.jenkinsci.plugins.postbuildscript.PostBuildScriptException;
import org.jenkinsci.plugins.postbuildscript.model.Configuration;
import org.jenkinsci.plugins.postbuildscript.model.PostBuildItem;
import org.jenkinsci.plugins.postbuildscript.model.PostBuildStep;
import org.jenkinsci.plugins.postbuildscript.model.Script;
import org.jenkinsci.plugins.postbuildscript.model.ScriptFile;
import org.jenkinsci.plugins.postbuildscript.model.ScriptType;
import org.jenkinsci.plugins.postbuildscript.processor.rules.ExecutionRule;
import org.jenkinsci.plugins.postbuildscript.service.Command;
import org.jenkinsci.plugins.postbuildscript.service.CommandExecutor;
import org.jenkinsci.plugins.postbuildscript.service.GroovyScriptExecutorFactory;
import org.jenkinsci.plugins.postbuildscript.service.GroovyScriptPreparer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class Processor {

    private final AbstractBuild<?, ?> build;
    private final Launcher launcher;
    private final BuildListener listener;
    private final Configuration config;
    private final Logger logger;
    private final Collection<ExecutionRule> rules = new ArrayList<>();

    public Processor(
        AbstractBuild<?, ?> build,
        Launcher launcher,
        BuildListener listener,
        Configuration config
    ) {
        this.build = build;
        Result result = build.getResult();
        if (result == null) {
            this.launcher = launcher;
        } else {
            this.launcher = launcher.decorateByEnv(
                new EnvVars("BUILD_RESULT", result.toString())); //NON-NLS
        }
        this.listener = listener;
        this.config = config;
        logger = new Logger(listener);
    }

    public void addRule(ExecutionRule rule) {
        rules.add(rule);
    }

    private Command getResolvedCommand(String command) throws PostBuildScriptException {
        if (command == null) {
            return null;
        }

        try {
            String resolvedPath = Util.replaceMacro(command, build.getEnvironment(listener));
            resolvedPath = Util.replaceMacro(resolvedPath, build.getBuildVariables());
            return new Command(resolvedPath);
        } catch (IOException | InterruptedException ioe) {
            throw new PostBuildScriptException(ioe);
        }
    }

    public boolean process() {
        return process(false);
    }

    public boolean process(boolean endOfMatrixBuild) {
        logger.info(Messages.PostBuildScript_ExecutingPostBuildScripts());
        try {
            return processScripts(endOfMatrixBuild);
        } catch (PostBuildScriptException pse) {
            logger.error(Messages.PostBuildScript_ProblemOccured(pse.getMessage()));
            failOrUnstable();
            return false;
        }
    }

    private boolean processScripts(boolean endOfMatrixBuild) throws PostBuildScriptException {
        @SuppressWarnings("NonShortCircuitBooleanExpression")
        boolean everyScriptSuccessful = processScriptFiles(endOfMatrixBuild)
            & processGroovyScripts(endOfMatrixBuild)
            & processBuildSteps(endOfMatrixBuild);
        return everyScriptSuccessful || failOrUnstable();
    }

    private boolean failOrUnstable() {
        if (config.isMarkBuildUnstable()) {
            build.setResult(Result.UNSTABLE);
            return true;
        }
        build.setResult(Result.FAILURE);
        return false;
    }

    private boolean processScriptFiles(boolean endOfMatrixBuild) throws PostBuildScriptException {

        FilePath workspace = build.getWorkspace();
        CommandExecutor commandExecutor = new CommandExecutor(logger, listener, workspace, launcher);
        GroovyScriptPreparer scriptPreparer = createGroovyScriptPreparer();
        boolean everyStepSuccessful = true;
        for (ScriptFile scriptFile : config.getScriptFiles()) {
            String filePath = scriptFile.getFilePath();
            if (Strings.nullToEmpty(filePath).trim().isEmpty()) {
                logger.error(Messages.PostBuildScript_NoFilePathProvided(config.scriptFileIndexOf(scriptFile)));
                continue;
            }

            if (violatesAnyRule(scriptFile, filePath, endOfMatrixBuild)) {
                continue;
            }

            Command command = getResolvedCommand(filePath);
            if (command != null) {

                if (scriptFile.getScriptType() == ScriptType.GENERIC) {
                    int returnCode = commandExecutor.executeCommand(command);
                    if (returnCode != 0) {
                        everyStepSuccessful = false;
                    }
                } else {
                    if (!scriptPreparer.evaluateCommand(scriptFile, command)) {
                        everyStepSuccessful = false;
                    }
                }
            }
        }
        return everyStepSuccessful;
    }

    private boolean processGroovyScripts(boolean endOfMatrixBuild) {

        GroovyScriptPreparer executor = createGroovyScriptPreparer();
        boolean everyStepSuccessful = true;
        for (Script script : config.getGroovyScripts()) {

            String scriptName = Messages.PostBuildScript_GroovyScript(config.groovyScriptIndexOf(script));
            if (violatesAnyRule(script, scriptName, endOfMatrixBuild)) {
                continue;
            }

            String content = script.getContent();
            if (content != null) {
                if (!executor.evaluateScript(script)) {
                    everyStepSuccessful = false;
                }
            }

        }
        return everyStepSuccessful;
    }

    private GroovyScriptPreparer createGroovyScriptPreparer() {
        FilePath workspace = build.getWorkspace();
        GroovyScriptExecutorFactory executorFactory =
            new GroovyScriptExecutorFactory(build, logger);
        return new GroovyScriptPreparer(logger, workspace, executorFactory);
    }

    private boolean processBuildSteps(boolean endOfMatrixBuild) throws PostBuildScriptException {

        try {
            boolean everyStepSuccessful = true;
            for (PostBuildStep postBuildStep : config.getBuildSteps()) {

                String scriptName = Messages.PostBuildScript_BuildStep(
                    config.buildStepIndexOf(postBuildStep));
                if (violatesAnyRule(postBuildStep, scriptName, endOfMatrixBuild)) {
                    continue;
                }

                for (BuildStep buildStep : postBuildStep.getBuildSteps()) {
                    if (!buildStep.perform(build, launcher, listener)) {
                        everyStepSuccessful = false;
                    }
                }
            }
            return everyStepSuccessful;
        } catch (IOException | InterruptedException ioe) {
            throw new PostBuildScriptException(ioe);
        }
    }

    private boolean violatesAnyRule(PostBuildItem item, String scriptName, boolean endOfMatrixBuild) {
        for (ExecutionRule rule : rules) {
            if (!rule.allows(item, endOfMatrixBuild)) {
                logger.info(
                    rule.formatViolationMessage(
                        item,
                        scriptName
                    )
                );
                return true;
            }
        }
        return false;
    }

}
