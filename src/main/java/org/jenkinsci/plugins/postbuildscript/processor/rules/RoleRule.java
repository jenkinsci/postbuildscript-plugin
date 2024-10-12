package org.jenkinsci.plugins.postbuildscript.processor.rules;

import hudson.model.AbstractBuild;
import org.jenkinsci.plugins.postbuildscript.Messages;
import org.jenkinsci.plugins.postbuildscript.model.PostBuildItem;

public class RoleRule implements ExecutionRule {

    private final AbstractBuild<?, ?> build;

    public RoleRule(AbstractBuild<?, ?> build) {
        this.build = build;
    }

    @Override
    public boolean allows(PostBuildItem item, boolean endOfMatrixBuild) {
        boolean runsOnMaster =
                build.getBuiltOnStr() == null || build.getBuiltOnStr().isEmpty();
        if (runsOnMaster) {
            return item.shouldRunOnMaster();
        }
        return item.shouldRunOnSlave();
    }

    @Override
    public String formatViolationMessage(PostBuildItem item, String scriptName) {
        return Messages.PostBuildScript_NodeDoesNotHaveRole(item.getRole(), scriptName);
    }
}
