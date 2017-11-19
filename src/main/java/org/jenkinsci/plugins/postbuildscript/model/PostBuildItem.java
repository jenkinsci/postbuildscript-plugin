package org.jenkinsci.plugins.postbuildscript.model;

import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PostBuildItem {

    private Set<String> results = new HashSet<>();

    private Role role = Role.BOTH;

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
        return !results.isEmpty();
    }

    public void addResults(@Nonnull Collection<String> results) {
        this.results.addAll(results);
    }

    @DataBoundSetter
    public void setRole(Role role) {
        this.role = role;
    }

    public boolean shouldRunOnMaster() {
        return role == Role.MASTER || role == Role.BOTH;
    }

    public boolean shouldRunOnSlave() {
        return role == Role.SLAVE || role == Role.BOTH;
    }

    public Object readResolve() {
        results = new HashSet<>();
        role = Role.BOTH;
        return this;
    }

}
