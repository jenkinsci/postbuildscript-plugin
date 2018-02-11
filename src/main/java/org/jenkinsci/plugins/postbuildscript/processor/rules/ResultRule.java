package org.jenkinsci.plugins.postbuildscript.processor.rules;

import hudson.model.AbstractBuild;
import hudson.model.Result;
import org.jenkinsci.plugins.postbuildscript.Messages;
import org.jenkinsci.plugins.postbuildscript.model.PostBuildItem;

import java.util.Optional;

public class ResultRule implements ExecutionRule {

    private final AbstractBuild<?, ?> build;

    public ResultRule(AbstractBuild<?, ?> build) {
        this.build = build;
    }

    @Override
    public boolean allows(PostBuildItem item, boolean endOfMatrixBuild) {
        Optional<Result> result = Optional.ofNullable(build.getResult());
        return result.isPresent() && item.shouldBeExecuted(result.get().toString());
    }

    @Override
    public String formatViolationMessage(PostBuildItem item, String scriptName) {
        return Messages.PostBuildScript_BuildDoesNotFit(item.getResults(), scriptName);
    }
}
