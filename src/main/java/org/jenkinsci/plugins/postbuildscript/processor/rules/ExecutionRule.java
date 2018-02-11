package org.jenkinsci.plugins.postbuildscript.processor.rules;

import org.jenkinsci.plugins.postbuildscript.model.PostBuildItem;

public interface ExecutionRule {

    boolean allows(PostBuildItem item, boolean endOfMatrixBuild);

    String formatViolationMessage(PostBuildItem item, String scriptName);

}
