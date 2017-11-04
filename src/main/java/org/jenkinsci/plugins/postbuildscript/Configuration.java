package org.jenkinsci.plugins.postbuildscript;

import org.jenkinsci.plugins.postbuildscript.model.PostBuildStep;
import org.jenkinsci.plugins.postbuildscript.model.Script;
import org.jenkinsci.plugins.postbuildscript.model.ScriptFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Configuration {

    private final List<ScriptFile> genericScriptFiles = new ArrayList<>();
    private final List<ScriptFile> groovyScriptFiles = new ArrayList<>();
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

    public List<ScriptFile> getGenericScriptFiles() {
        return Collections.unmodifiableList(genericScriptFiles);
    }

    public int genericScriptFileIndexOf(ScriptFile scriptFile) {
        return genericScriptFiles.indexOf(scriptFile);
    }

    public List<Script> getGroovyScripts() {
        return Collections.unmodifiableList(groovyScripts);
    }

    public int groovyScriptIndexOf(Script script) {
        return groovyScripts.indexOf(script);
    }

    public List<ScriptFile> getGroovyScriptFiles() {
        return Collections.unmodifiableList(groovyScriptFiles);
    }

    public int groovyScriptFileIndexOf(ScriptFile scriptFile) {
        return groovyScriptFiles.indexOf(scriptFile);
    }

    public void addGenericScriptFiles(Collection<? extends ScriptFile> genericScriptFiles) {
        this.genericScriptFiles.addAll(genericScriptFiles);
    }

    public void addGroovyScriptFiles(Collection<? extends ScriptFile> groovyScriptFiles) {
        this.groovyScriptFiles.addAll(groovyScriptFiles);
    }

    public void addGroovyScripts(Collection<? extends Script> groovyScripts) {
        this.groovyScripts.addAll(groovyScripts);
    }

    public void addBuildSteps(Collection<? extends PostBuildStep> buildSteps) {
        this.buildSteps.addAll(buildSteps);
    }

    public boolean addBuildStep(PostBuildStep postBuildStep) {
        return buildSteps.add(postBuildStep);
    }

}
