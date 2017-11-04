package org.jenkinsci.plugins.postbuildscript.model;

public class PostBuildItem {

    private String result;

    public PostBuildItem(String result) {
        this.result = result;
    }

    public boolean shouldBeExecuted(String result) {
        return this.result.equals(result);
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public boolean hasResult() {
        return result != null;
    }
}
