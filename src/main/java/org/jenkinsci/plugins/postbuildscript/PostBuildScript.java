package org.jenkinsci.plugins.postbuildscript;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.matrix.*;
import hudson.maven.MavenModuleSet;
import hudson.model.*;
import hudson.tasks.*;
import org.jenkinsci.plugins.postbuildscript.service.ScriptExecutor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gregory Boissinot
 */
public class PostBuildScript extends Notifier implements MatrixAggregatable {

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Deprecated
    private transient List<GenericScript> genericScriptList = new ArrayList<GenericScript>();

    @SuppressWarnings("deprecation")
    private transient List<GroovyScript> groovyScriptList = new ArrayList<GroovyScript>();

    private List<GenericScript> genericScriptFileList = new ArrayList<GenericScript>();
    private List<GroovyScriptFile> groovyScriptFileList = new ArrayList<GroovyScriptFile>();
    private List<GroovyScriptContent> groovyScriptContentList = new ArrayList<GroovyScriptContent>();
    private List<BuildStep> buildSteps;

    private boolean scriptOnlyIfSuccess;
    private boolean scriptOnlyIfFailure;
    private boolean executeOnMatrixNodes;

    @DataBoundConstructor
    public PostBuildScript(List<GenericScript> genericScriptFile,
                           List<GroovyScriptFile> groovyScriptFile,
                           List<GroovyScriptContent> groovyScriptContent,
                           boolean scriptOnlyIfSuccess,
                           boolean scriptOnlyIfFailure,
                           boolean executeOnMatrixNodes,
                           List<BuildStep> buildStep) {
        this.genericScriptFileList = genericScriptFile;
        this.groovyScriptFileList = groovyScriptFile;
        this.groovyScriptContentList = groovyScriptContent;
        this.buildSteps = buildStep;
        this.scriptOnlyIfSuccess = scriptOnlyIfSuccess;
        this.scriptOnlyIfFailure = scriptOnlyIfFailure;
        this.executeOnMatrixNodes = executeOnMatrixNodes;
    }

    public MatrixAggregator createAggregator(MatrixBuild build, Launcher launcher, BuildListener listener) {
        return new MatrixAggregator(build, launcher, listener) {
            @Override
            public boolean endBuild() throws InterruptedException, IOException {
                if (!executeOnMatrixNodes)
                    return _perform(build, launcher, listener);
                else
                    return true;
            }
        };
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) throws InterruptedException, IOException {
        Job job = build.getProject();
        boolean axe = isMatrixAxe(job);
        if (   (axe && executeOnMatrixNodes)     // matrix axe, and set to execute on axes' nodes
            || (!axe)) {                         // neither matrix head nor axe
            return _perform(build, launcher, listener);
        }
        return true;
    }

    private boolean _perform(AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) throws InterruptedException, IOException {

        listener.getLogger().println("[PostBuildScript] - Execution post build scripts.");

        ScriptExecutor executor = new ScriptExecutor(
                new PostBuildScriptLog(listener),
                listener
        );

        try {
            if (scriptOnlyIfSuccess && build.getResult().isWorseThan(Result.SUCCESS)) {
                listener.getLogger().println("[PostBuildScript] Build is not success : do not execute script");
                return true;
            } else if (scriptOnlyIfFailure && build.getResult().isBetterThan(Result.FAILURE)) {
                listener.getLogger().println("[PostBuildScript] Build is not failure : do not execute script");
                return true;
            } else {
                return processScripts(executor, build, launcher, listener);
            }
        } catch (PostBuildScriptException pse) {
            listener.getLogger().println("[PostBuildScript] - [Error] - Problems occurs: " + pse.getMessage());
            build.setResult(Result.FAILURE);
            return false;
        }
    }


    private boolean processScripts(ScriptExecutor executor, AbstractBuild build, Launcher launcher, BuildListener listener) throws PostBuildScriptException {

        //Execute Generic scripts file
        if (genericScriptFileList != null) {
            boolean result = processGenericScriptList(genericScriptFileList, executor, build, launcher, listener);
            if (!result) {
                setFailedResult(build);
                return false;
            }
        }

        //Execute Groovy scripts file
        if (groovyScriptFileList != null) {
            boolean result = processGroovyScriptFileList(groovyScriptFileList, executor, build, listener);
            if (!result) {
                setFailedResult(build);
                return false;
            }
        }

        //Execute Groovy scripts content
        if (groovyScriptContentList != null) {
            boolean result = processGroovyScriptContentList(build.getWorkspace(), groovyScriptContentList, executor);
            if (!result) {
                setFailedResult(build);
                return false;
            }
        }

        //Execute Build steps
        if (buildSteps != null) {
            boolean result = processBuildSteps(buildSteps, build, launcher, listener);
            if (!result) {
                setFailedResult(build);
                return false;
            }
        }

        return true;
    }

    private boolean processGenericScriptList(List<GenericScript> genericScriptFileList, ScriptExecutor executor, AbstractBuild build, Launcher launcher, BuildListener listener) throws PostBuildScriptException {

        assert genericScriptFileList != null;

        FilePath workspace = build.getWorkspace();
        for (GenericScript script : genericScriptFileList) {
            String scriptPath = getResolvedPath(script.getFilePath(), build, listener);
            if (scriptPath != null) {
                int cmd = executor.executeScriptPathAndGetExitCode(workspace, scriptPath, launcher);
                if (cmd != 0) {
                    return false;
                }
            }
        }
        return true;
    }


    private boolean processGroovyScriptFileList(List<GroovyScriptFile> groovyScriptFileList, ScriptExecutor executor, AbstractBuild build, BuildListener listener) throws PostBuildScriptException {

        assert groovyScriptFileList != null;

        FilePath workspace = build.getWorkspace();
        for (GroovyScriptFile groovyScript : groovyScriptFileList) {
            String groovyPath = getResolvedPath(groovyScript.getFilePath(), build, listener);
            if (groovyPath != null) {
                if (!executor.performGroovyScriptFile(workspace, groovyPath)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean processGroovyScriptContentList(FilePath workspace, List<GroovyScriptContent> groovyScriptContentList, ScriptExecutor executor) throws PostBuildScriptException {

        assert groovyScriptContentList != null;

        for (GroovyScriptContent groovyScript : groovyScriptContentList) {
            String content = groovyScript.getContent();
            if (content != null) {
                if (!executor.performGroovyScript(workspace, content)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean processBuildSteps(List<BuildStep> buildSteps, AbstractBuild build, Launcher launcher, BuildListener listener) throws PostBuildScriptException {
        try {
            for (BuildStep bs : buildSteps) {
                if (!bs.perform(build, launcher, listener)) {
                    setFailedResult(build);
                    return false;
                }
            }
            return true;
        } catch (IOException ioe) {
            throw new PostBuildScriptException(ioe);
        } catch (InterruptedException ie) {
            throw new PostBuildScriptException(ie);
        }
    }

    private String getResolvedPath(String path, AbstractBuild build, BuildListener listener) throws PostBuildScriptException {
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

    @Deprecated
    @SuppressWarnings({"unused", "deprecation"})
    public List<GenericScript> getGenericScriptList() {
        return genericScriptList;
    }

    @Deprecated
    @SuppressWarnings({"unused", "deprecation"})
    public List<GroovyScript> getGroovyScriptList() {
        return groovyScriptList;
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
    public List<BuildStep> getBuildSteps() {
        return buildSteps;
    }

    @SuppressWarnings("unused")
    public boolean isScriptOnlyIfSuccess() {
        return scriptOnlyIfSuccess;
    }

    @SuppressWarnings("unused")
    public boolean isScriptOnlyIfFailure() {
        return scriptOnlyIfFailure;
    }

    @SuppressWarnings("unused")
    public boolean isExecuteOnMatrixNodes() {
        return executeOnMatrixNodes;
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
            return Project.class.isAssignableFrom(jobType)
                    || MatrixProject.class.isAssignableFrom(jobType)
                    || MavenModuleSet.class.isAssignableFrom(jobType)
                    || (Hudson.getInstance().getPlugin("ivy") != null && hudson.ivy.IvyModuleSet.class.isAssignableFrom(jobType));
        }

        public boolean isMatrixProject(Object job) {
            return PostBuildScript.isMatrixProject(job);
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
        if (genericScriptList != null) {
            if (genericScriptFileList == null) {
                genericScriptFileList = new ArrayList<GenericScript>();
            }
            if (buildSteps == null) {
                buildSteps = new ArrayList<BuildStep>();
            }
            for (GenericScript script : genericScriptList) {
                if (script.getContent() != null) {
                    if (isUnix()) {
                        buildSteps.add(new Shell(script.getContent()));
                    } else {
                        buildSteps.add(new BatchFile(script.getContent()));
                    }
                } else {
                    genericScriptFileList.add(script);
                }
            }
        }

        if (groovyScriptList != null) {
            if (groovyScriptContentList == null) {
                groovyScriptContentList = new ArrayList<GroovyScriptContent>();
            }
            if (groovyScriptFileList == null) {
                groovyScriptFileList = new ArrayList<GroovyScriptFile>();
            }
            for (GroovyScript groovyScript : groovyScriptList) {
                if (groovyScript.getContent() != null) {
                    groovyScriptContentList.add(new GroovyScriptContent(groovyScript.getContent()));
                }
                if (groovyScript.getFilePath() != null) {
                    groovyScriptFileList.add(new GroovyScriptFile(groovyScript.getFilePath()));
                }
            }
        }

        return this;
    }

    private boolean isUnix() {
        return File.pathSeparatorChar == ':';
    }
}

