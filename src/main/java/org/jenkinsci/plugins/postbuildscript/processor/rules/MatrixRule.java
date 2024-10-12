package org.jenkinsci.plugins.postbuildscript.processor.rules;

import org.jenkinsci.plugins.postbuildscript.Messages;
import org.jenkinsci.plugins.postbuildscript.model.ExecuteOn;
import org.jenkinsci.plugins.postbuildscript.model.PostBuildItem;

public class MatrixRule implements ExecutionRule {

    @Override
    public boolean allows(PostBuildItem item, boolean endOfMatrixBuild) {

        ExecuteOn executeOn = item.getExecuteOn();

        // TODO Remove, if there are no 0.18.x installations left
        // needed for migration (JENKINS-53691)
        if (executeOn == null) {
            return true;
        }

        if (endOfMatrixBuild) {
            return executeOn.matrix();
        }

        return executeOn.axes();
    }

    @Override
    public String formatViolationMessage(PostBuildItem item, String scriptName) {
        return Messages.PostBuildScript_OnlyExecuteOn(scriptName, item.getExecuteOn());
    }
}
