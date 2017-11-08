package org.jenkinsci.plugins.postbuildscript.model;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

public class PostBuildItemTest {

    private static final String SUCCESS = "SUCCESS"; //NON-NLS
    private static final String FAILURE = "FAILURE"; //NON-NLS

    private PostBuildItem postBuildItem;

    @Before
    public void setUp() throws Exception {
        postBuildItem = new PostBuildItem(Collections.singleton(SUCCESS));
    }

    @Test
    public void doesNotHaveResultOnNullResult() throws Exception {

        PostBuildItem postBuildItem = new PostBuildItem(null);
        assertThat(postBuildItem.hasResult(), is(false));

    }

    @Test
    public void doesNotHaveResultOnEmptyResults() throws Exception {

        PostBuildItem postBuildItem = new PostBuildItem(Collections.<String>emptySet());
        assertThat(postBuildItem.hasResult(), is(false));

    }


    @Test
    public void allowsExecutionWhenContainsResults() throws Exception {

        assertThat(postBuildItem.shouldBeExecuted(SUCCESS), is(true));

    }

    @Test
    public void deniesExecutionWhenDoesNotContainResults() throws Exception {

        assertThat(postBuildItem.shouldBeExecuted(FAILURE), is(false));

    }

    @Test
    public void deniesExecutionOnNull() throws Exception {

        assertThat(postBuildItem.shouldBeExecuted(null), is(false));

    }

    @Test
    public void addsResultsWhenInitialized() throws Exception {

        postBuildItem.addResults(Collections.singleton(FAILURE));

        assertThat(postBuildItem.getResults(), contains(SUCCESS, FAILURE));

    }

}
