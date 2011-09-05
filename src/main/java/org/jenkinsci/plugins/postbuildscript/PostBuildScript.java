package org.jenkinsci.plugins.postbuildscript;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.matrix.MatrixProject;
import hudson.maven.MavenModuleSet;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import org.jenkinsci.plugins.postbuildscript.service.ScriptExecutor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gregory Boissinot
 */
public class PostBuildScript extends Notifier {

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    private List<GenericScript> genericScriptList = new ArrayList<GenericScript>();

    private List<GroovyScript> groovyScriptList = new ArrayList<GroovyScript>();

    @DataBoundConstructor
    public PostBuildScript(List<GenericScript> genericScript, List<GroovyScript> groovyScript) {
        this.genericScriptList = genericScript;
        this.groovyScriptList = groovyScript;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) throws InterruptedException, IOException {

        listener.getLogger().println("[PostBuildScript] - Execution post build scripts.");

        final FilePath executionPath = build.getWorkspace();

        ScriptExecutor executor = Guice.createInjector(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(PostBuildScriptLog.class).toInstance(new PostBuildScriptLog(listener));
                        bind(Launcher.class).toInstance(launcher);
                        bind(BuildListener.class).toInstance(listener);
                        bind(FilePath.class).toInstance(executionPath);
                    }
                }
        ).getInstance(ScriptExecutor.class);

        try {

            if (genericScriptList != null) {
                boolean result = processGenericScriptList(executor, build, launcher, listener);
                if (!result) {
                    setFailedResult(build);
                    return false;
                }
            }

            if (groovyScriptList != null) {
                boolean result = processGroovyScriptList(executor, build, listener);
                if (!result) {
                    setFailedResult(build);
                    return false;
                }
            }


        } catch (PostBuildScriptException e) {
            build.setResult(Result.FAILURE);
            return false;
        }

        return true;
    }


    private boolean processGenericScriptList(ScriptExecutor executor, AbstractBuild build, Launcher launcher, BuildListener listener) throws PostBuildScriptException {
        for (GenericScript script : genericScriptList) {

            String scriptContent = script.getContent();
            if (scriptContent != null) {
                int cmd = executor.executeScriptAndGetExitCode(scriptContent, launcher);
                if (cmd != 0) {
                    return false;
                }
            }

            String scriptPath = getResolvedPath(script.getFilePath(), build, listener);
            if (scriptPath != null) {
                int cmd = executor.executeScriptPathAndGetExitCode(scriptPath, launcher);
                if (cmd != 0) {
                    return false;
                }

            }
        }
        return true;
    }


    private boolean processGroovyScriptList(ScriptExecutor executor, AbstractBuild build, BuildListener listener) throws PostBuildScriptException {

        for (GroovyScript groovyScript : groovyScriptList) {

            try {

                String content = groovyScript.getContent();
                if (content != null) {
                    executor.evaluateGroovyScript(content);
                }

                String groovyPath = getResolvedPath(groovyScript.getFilePath(), build, listener);
                if (groovyPath != null) {
                    executor.evaluateGroovyScriptFilePath(groovyPath);
                }

            } catch (PostBuildScriptException e) {
                return false;
            }
        }
        return true;
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

    @SuppressWarnings("unused")
    public List<GenericScript> getGenericScriptList() {
        return genericScriptList;
    }

    @SuppressWarnings("unused")
    public List<GroovyScript> getGroovyScriptList() {
        return groovyScriptList;
    }

    @Extension(ordinal = 99)
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public String getDisplayName() {
            return "[PostBuildScript] - Execute a set of scripts";
        }

        @Override
        public String getHelpFile() {
            return "/plugin/postbuildscript/help.html";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return Project.class.isAssignableFrom(jobType)
                    || MatrixProject.class.isAssignableFrom(jobType)
                    || MavenModuleSet.class.isAssignableFrom(jobType);
        }
    }
}
