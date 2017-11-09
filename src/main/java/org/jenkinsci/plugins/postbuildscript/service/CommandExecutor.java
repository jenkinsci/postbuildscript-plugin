package org.jenkinsci.plugins.postbuildscript.service;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.tasks.BatchFile;
import hudson.tasks.CommandInterpreter;
import hudson.tasks.Shell;
import org.jenkinsci.plugins.postbuildscript.Logger;
import org.jenkinsci.plugins.postbuildscript.Messages;
import org.jenkinsci.plugins.postbuildscript.PostBuildScriptException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class CommandExecutor {

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

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

    private static String[] parseParameters(CharSequence command) {
        String[] commandLine = WHITESPACE_PATTERN.split(command);
        String[] parameters = new String[commandLine.length - 1];

        if (commandLine.length > 1) {
            System.arraycopy(commandLine, 1, parameters, 0, parameters.length);
        }
        return parameters;
    }

    public int executeCommand(CharSequence command) throws PostBuildScriptException {

        String[] parameters = parseParameters(command);
        String scriptContent = resolveScriptContent(command, parameters);
        try {
            List<String> arguments = buildArguments(parameters, scriptContent);
            return launcher.launch().cmds(arguments).stdout(listener).pwd(workspace).join();
        } catch (InterruptedException | IOException exception) {
            throw new PostBuildScriptException("Error while executing script", exception);
        }

    }

    private List<String> buildArguments(String[] parameters, String scriptContent)
        throws IOException, InterruptedException {
        CommandInterpreter interpreter = createInterpreter(scriptContent);
        FilePath scriptFile = interpreter.createScriptFile(workspace);
        List<String> args = new ArrayList<>(Arrays.asList(interpreter.buildCommandLine(scriptFile)));
        args.addAll(Arrays.asList(parameters));
        return args;
    }

    private CommandInterpreter createInterpreter(String scriptContent) {
        if (launcher.isUnix()) {
            return new Shell(scriptContent);
        }
        return new BatchFile(scriptContent);
    }

    private String resolveScriptContent(CharSequence command, String[] parameters)
        throws PostBuildScriptException {

        FilePath script = new ScriptFilePath(workspace).resolve(command);
        logger.info(Messages.PostBuildScript_ExecutingScript(script, Arrays.toString(parameters)));
        LoadScriptContentCallable callable = new LoadScriptContentCallable();
        return new Content(logger, callable).resolve(script);

    }

}
