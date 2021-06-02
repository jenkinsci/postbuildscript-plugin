package org.jenkinsci.plugins.postbuildscript.service;

import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

public class Command {

    private final String scriptPath;

    private final List<String> parameters;

    public Command(String command) {
        Deque<String> commandParts = parseLine(command);
        scriptPath = commandParts.pop();
        parameters = new ArrayList<>(commandParts);
    }

    private static Deque<String> parseLine(String command) {
        if (StringUtils.isBlank(command)) {
            return new ArrayDeque<>(0);
        }
        command = command.trim();
        Deque<String> tokens = new ArrayDeque<>(1);
        StringBuilder currentToken = new StringBuilder(20);
        boolean singleHyphenOpen = false;
        boolean doubleHyphenOpen = false;
        int index = 0;
        while (index < command.length()) {
            char ch = command.charAt(index);
            if (ch == '"' && !singleHyphenOpen) {
                if (doubleHyphenOpen) {
                    addAndReset(tokens, currentToken);
                    doubleHyphenOpen = false;
                } else {
                    if (currentToken.length() > 0) {
                        addAndReset(tokens, currentToken);
                    }
                    doubleHyphenOpen = true;
                }
                index++;
            } else if (ch == '\'' && !doubleHyphenOpen) {
                if (singleHyphenOpen) {
                    addAndReset(tokens, currentToken);
                    singleHyphenOpen = false;
                } else {
                    if (currentToken.length() > 0) {
                        addAndReset(tokens, currentToken);
                    }
                    singleHyphenOpen = true;
                }
                index++;
            } else if (ch == ' ' && !doubleHyphenOpen && !singleHyphenOpen) {
                if (currentToken.length() > 0) {
                    addAndReset(tokens, currentToken);
                }
                index++;
            } else {
                currentToken.append(ch);
                index++;
            }
        }
        if (index == command.length() && doubleHyphenOpen) {
            throw new IllegalArgumentException("Missing closing \" in " + command + " -- " + tokens);
        }
        if (index == command.length() && singleHyphenOpen) {
            throw new IllegalArgumentException("Missing closing ' in " + command  + " -- " + tokens);
        }
        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }
        return tokens;
    }

    private static void addAndReset(Collection<String> tokens, StringBuilder currentToken) {
        tokens.add(currentToken.toString());
        currentToken.setLength(0);
    }

    public String getScriptPath() {
        return scriptPath;
    }

    public List<String> getParameters() {
        return Collections.unmodifiableList(parameters);
    }
}
