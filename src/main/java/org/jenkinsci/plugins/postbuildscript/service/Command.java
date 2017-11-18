package org.jenkinsci.plugins.postbuildscript.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.regex.Pattern;

public class Command {

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    private final String scriptPath;

    private final List<String> parameters;

    public Command(CharSequence command) {
        Deque<String> commandParts = new ArrayDeque<>(Arrays.asList(WHITESPACE_PATTERN.split(command)));
        scriptPath = commandParts.pop();
        parameters = new ArrayList<>(commandParts);
    }

    public String getScriptPath() {
        return scriptPath;
    }

    public List<String> getParameters() {
        return Collections.unmodifiableList(parameters);
    }
}
