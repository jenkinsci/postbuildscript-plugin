package org.jenkinsci.plugins.postbuildscript.model;

import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.tasks.BuildStep;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class PostBuildStep extends PostBuildItem {

    private List<BuildStep> buildSteps = new ArrayList<>();

    private boolean stopOnFailure;

    @DataBoundConstructor
    public PostBuildStep(@Nullable Collection<String> results, @Nullable Collection<BuildStep> buildSteps, boolean stopOnFailure) {
        super(results);
        this.stopOnFailure = stopOnFailure;
        if (buildSteps != null) {
            this.buildSteps.addAll(buildSteps);
        }
    }

    public Iterable<BuildStep> getBuildSteps() {
        return Collections.unmodifiableCollection(buildSteps);
    }

    public boolean isStopOnFailure() {
        return stopOnFailure;
    }

    public void setStopOnFailure(boolean stopOnFailure) {
        this.stopOnFailure = stopOnFailure;
    }

    @Override
    public Object readResolve() {
        super.readResolve();
        if (buildSteps == null) {
            buildSteps = new ArrayList<>();
        }
        return this;
    }

}
