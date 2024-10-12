package org.jenkinsci.plugins.postbuildscript.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Configuration {

    private List<ScriptFile> scriptFiles = new ArrayList<>();
    private final List<Script> groovyScripts = new ArrayList<>();
    private final List<PostBuildStep> buildSteps = new ArrayList<>();
    private boolean markBuildUnstable;

    public List<PostBuildStep> getBuildSteps() {
        return Collections.unmodifiableList(buildSteps);
    }

    public int buildStepIndexOf(PostBuildStep postBuildStep) {
        return buildSteps.indexOf(postBuildStep);
    }

    public boolean isMarkBuildUnstable() {
        return markBuildUnstable;
    }

    public void setMarkBuildUnstable(boolean markBuildUnstable) {
        this.markBuildUnstable = markBuildUnstable;
    }

    public List<ScriptFile> getScriptFiles() {
        return Collections.unmodifiableList(scriptFiles);
    }

    public List<ScriptFile> getScriptFiles(ScriptType scriptType) {
        return scriptFiles.stream()
                .filter(scriptFile -> scriptFile.getScriptType() == scriptType)
                .collect(Collectors.toList());
    }

    public int scriptFileIndexOf(ScriptFile scriptFile) {
        return scriptFiles.indexOf(scriptFile);
    }

    public List<Script> getGroovyScripts() {
        return Collections.unmodifiableList(groovyScripts);
    }

    public int groovyScriptIndexOf(Script script) {
        return groovyScripts.indexOf(script);
    }

    public void addScriptFiles(Collection<? extends ScriptFile> scriptFiles) {
        this.scriptFiles.addAll(scriptFiles);
    }

    public void addGroovyScripts(Collection<? extends Script> groovyScripts) {
        this.groovyScripts.addAll(groovyScripts);
    }

    public void addBuildSteps(Collection<? extends PostBuildStep> buildSteps) {
        this.buildSteps.addAll(buildSteps);
    }

    public void addBuildStep(PostBuildStep postBuildStep) {
        buildSteps.add(postBuildStep);
    }
}
