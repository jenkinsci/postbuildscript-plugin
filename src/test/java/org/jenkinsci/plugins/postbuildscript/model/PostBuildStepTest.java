package org.jenkinsci.plugins.postbuildscript.model;

import hudson.tasks.BuildStep;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PostBuildStepTest {

    private static final String RESULT = "SUCCESS";

    @Mock
    private BuildStep buildStep;

    @Test
    public void containsBuildSteps() {

        PostBuildStep postBuildStep = new PostBuildStep(
            Collections.singleton(RESULT), Collections.singleton(buildStep));

        assertThat(postBuildStep.getBuildSteps(), contains(buildStep));
    }

    // JENKINS-53446
    @Test
    public void allowsEmptyBuildSteps() {

        PostBuildStep postBuildStep = new PostBuildStep(
            Collections.singleton(RESULT), null);

        assertThat(postBuildStep.getBuildSteps(), is(Matchers.emptyIterable()));

    }

}
