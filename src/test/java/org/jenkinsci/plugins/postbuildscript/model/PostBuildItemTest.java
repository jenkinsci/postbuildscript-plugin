package org.jenkinsci.plugins.postbuildscript.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PostBuildItemTest {

    private static final String SUCCESS = "SUCCESS"; // NON-NLS
    private static final String FAILURE = "FAILURE"; // NON-NLS

    private PostBuildItem postBuildItem;

    @BeforeEach
    public void setUp() {
        postBuildItem = new PostBuildItem(Collections.singleton(SUCCESS));
    }

    @Test
    public void doesNotHaveResultOnNullResult() {

        PostBuildItem postBuildItem = new PostBuildItem(null);
        assertThat(postBuildItem.hasResult(), is(false));
    }

    @Test
    public void doesNotHaveResultOnEmptyResults() {

        PostBuildItem postBuildItem = new PostBuildItem(Collections.emptySet());
        assertThat(postBuildItem.hasResult(), is(false));
    }

    @Test
    public void allowsExecutionWhenContainsResults() {

        assertThat(postBuildItem.shouldBeExecuted(SUCCESS), is(true));
    }

    @Test
    public void deniesExecutionWhenDoesNotContainResults() {

        assertThat(postBuildItem.shouldBeExecuted(FAILURE), is(false));
    }

    @Test
    public void deniesExecutionOnNull() {

        assertThat(postBuildItem.shouldBeExecuted(null), is(false));
    }

    @Test
    public void addsResultsWhenInitialized() {

        postBuildItem.addResults(Collections.singleton(FAILURE));

        assertThat(postBuildItem.getResults(), contains(SUCCESS, FAILURE));
    }

    @Test
    public void runsOnBothRolesPerDefault() {

        assertThat(postBuildItem.shouldRunOnMaster(), is(true));
        assertThat(postBuildItem.shouldRunOnSlave(), is(true));
    }

    @Test
    public void setsRole() {

        postBuildItem.setRole(Role.MASTER);
        assertThat(postBuildItem.shouldRunOnMaster(), is(true));
        assertThat(postBuildItem.shouldRunOnSlave(), is(false));
    }
}
