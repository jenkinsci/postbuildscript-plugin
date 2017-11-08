package org.jenkinsci.plugins.postbuildscript.model;

import hudson.tasks.BuildStep;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class PostBuildStep extends PostBuildItem {

    private final List<BuildStep> buildSteps;

    @DataBoundConstructor
    public PostBuildStep(@Nonnull Collection<BuildStep> buildSteps, @Nullable Collection<String> results) {
        super(results);
        this.buildSteps = new ArrayList<>(buildSteps);
    }

    public Iterable<BuildStep> getBuildSteps() {
        return Collections.unmodifiableCollection(buildSteps);
    }

}
