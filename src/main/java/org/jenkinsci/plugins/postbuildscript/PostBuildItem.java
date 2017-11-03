package org.jenkinsci.plugins.postbuildscript;

import hudson.model.Result;


public class PostBuildItem {

    private Result result;

    public PostBuildItem(String result) {
        this.result = Result.fromString(result);
    }

    public Result getTargetResult() {
        return result;
    }

    public String getResult() {
        return result.toString();
    }

}
