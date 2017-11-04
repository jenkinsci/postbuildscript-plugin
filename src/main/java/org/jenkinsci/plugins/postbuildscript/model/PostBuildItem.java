package org.jenkinsci.plugins.postbuildscript.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PostBuildItem {

    private Set<String> results = new HashSet<>();

    public PostBuildItem(Collection<String> results) {
        if (results != null) {
            this.results.addAll(results);
        }
    }

    public boolean shouldBeExecuted(String result) {
        return results.contains(result);
    }

    public Set<String> getResults() {
        return results;
    }

    public boolean hasResult() {
        return results != null && !results.isEmpty();
    }

    public void addResults(Collection<String> results) {
        if (this.results == null) {
            this.results = new HashSet<>();
        }
        this.results.addAll(results);
    }
}
