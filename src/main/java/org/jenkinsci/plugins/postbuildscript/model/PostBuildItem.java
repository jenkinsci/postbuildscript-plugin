package org.jenkinsci.plugins.postbuildscript.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PostBuildItem {

    private Set<String> results = new HashSet<>();

    public PostBuildItem(@Nullable Collection<String> results) {
        if (results != null) {
            this.results.addAll(results);
        }
    }

    public boolean shouldBeExecuted(@Nullable String result) {
        return results.contains(result);
    }

    public Set<String> getResults() {
        return Collections.unmodifiableSet(results);
    }

    public boolean hasResult() {
        return results != null && !results.isEmpty();
    }

    public void addResults(@Nonnull Collection<String> results) {
        if (this.results == null) { // may happen, if initialized from JSON
            this.results = new HashSet<>();
        }
        this.results.addAll(results);
    }
}
