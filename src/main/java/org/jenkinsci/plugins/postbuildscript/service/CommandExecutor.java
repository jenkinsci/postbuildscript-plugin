package org.jenkinsci.plugins.postbuildscript.service;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.tasks.BatchFile;
import hudson.tasks.CommandInterpreter;
import hudson.tasks.Shell;
import org.jenkinsci.plugins.postbuildscript.Messages;
import org.jenkinsci.plugins.postbuildscript.PostBuildScriptException;
import org.jenkinsci.plugins.postbuildscript.logging.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class CommandExecutor {

    private static final Pattern SHEBANG_WITH_SPACES = Pattern.compile("^#!\\s+");

    private final TaskListener listener;

    private final Logger logger;

    private final FilePath workspace;

    private final Launcher launcher;

    public CommandExecutor(Logger logger, TaskListener listener, FilePath workspace, Launcher launcher) {
        this.logger = logger;
        this.listener = listener;
        this.workspace = workspace;
        this.launcher = launcher;
    }

    public int executeCommand(Command command) throws PostBuildScriptException {
        String scriptContent = removeSpaceInFrontOfInterpreter(resolveScriptContent(command));
        try {
            List<String> arguments = buildArguments(command, scriptContent);
            return launcher.launch().cmds(arguments).stdout(listener).pwd(workspace).join();
        } catch (InterruptedException | IOException exception) {
            throw new PostBuildScriptException("Error while executing script", exception);
        }

    }

    private static String removeSpaceInFrontOfInterpreter(String scriptContent) {
        if (scriptContent.startsWith("#!")) {
            return SHEBANG_WITH_SPACES.matcher(scriptContent).replaceFirst("#!");
        }
        return scriptContent;
    }

    private List<String> buildArguments(Command command, String scriptContent)
        throws IOException, InterruptedException {
        CommandInterpreter interpreter = createInterpreter(scriptContent);
        FilePath scriptFile = interpreter.createScriptFile(workspace);
        List<String> args = new ArrayList<>(Arrays.asList(interpreter.buildCommandLine(scriptFile)));
        args.addAll(command.getParameters());
        return args;
    }

    private CommandInterpreter createInterpreter(String scriptContent) {
        if (launcher.isUnix()) {
            return new Shell(scriptContent);
        }
        return new BatchFile(scriptContent);
    }

    private String resolveScriptContent(Command command)
        throws PostBuildScriptException {

        FilePath script = new ScriptFilePath(workspace).resolve(command.getScriptPath());
        logger.info(Messages.PostBuildScript_ExecutingScript(script, command.getParameters()));
        LoadScriptContentCallable callable = new LoadScriptContentCallable();
        Content content = new Content(callable);
        String resolvedContent = content.resolve(script);
        return resolvedContent.trim();

    }

}
