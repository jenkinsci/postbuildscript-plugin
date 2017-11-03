package org.jenkinsci.plugins.postbuildscript;

import java.util.List;

import hudson.tasks.BuildStep;
import org.kohsuke.stapler.DataBoundConstructor;


public class PostBuildStep extends PostBuildItem {

    private final List<BuildStep> buildSteps;

    @DataBoundConstructor
    public PostBuildStep(List<BuildStep> buildSteps, String result) {
        super(result);
        this.buildSteps = buildSteps;
    }

    public List<BuildStep> getBuildSteps() {
        return buildSteps;
    }


}
