package org.jenkinsci.plugins.postbuildscript.model;

import hudson.tasks.BuildStep;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

@ExtendWith(MockitoExtension.class)
public class PostBuildStepTest {

    private static final String RESULT = "SUCCESS";

    @Mock
    private BuildStep buildStep;

    @Test
    public void containsBuildSteps() {

        PostBuildStep postBuildStep = new PostBuildStep(
            Collections.singleton(RESULT),
            Collections.singleton(buildStep),
            false
        );

        assertThat(postBuildStep.getBuildSteps(), contains(buildStep));
    }

    // JENKINS-53446
    @Test
    public void allowsEmptyBuildSteps() {

        PostBuildStep postBuildStep = new PostBuildStep(
            Collections.singleton(RESULT),
            null,
            false
        );

        assertThat(postBuildStep.getBuildSteps(), is(Matchers.emptyIterable()));

    }

}
