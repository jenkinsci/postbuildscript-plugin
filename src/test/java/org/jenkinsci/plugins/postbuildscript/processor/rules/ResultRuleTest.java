package org.jenkinsci.plugins.postbuildscript.processor.rules;

import hudson.model.AbstractBuild;
import hudson.model.Result;
import org.jenkinsci.plugins.postbuildscript.model.ExecuteOn;
import org.jenkinsci.plugins.postbuildscript.model.PostBuildItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ResultRuleTest {

    @InjectMocks
    private ResultRule resultRule;

    @Mock
    private AbstractBuild<?, ?> build;

    @Mock
    private PostBuildItem item;

    @Test
    public void allowsRunningWhenBuildHasSameResultThanItem() {

        given(build.getResult()).willReturn(Result.SUCCESS);
        given(item.shouldBeExecuted("SUCCESS")).willReturn(true);

        boolean actual = resultRule.allows(item, true);

        assertThat(actual, is(true));

    }

    @Test
    public void deniesRunningWhenBuildHasDifferentResultThanItem() {

        given(build.getResult()).willReturn(Result.SUCCESS);

        boolean actual = resultRule.allows(item, true);

        assertThat(actual, is(false));

    }

    @Test
    public void deniesRunningWhenBuildHasNoResult() {

        boolean actual = resultRule.allows(item, true);

        assertThat(actual, is(false));

    }

    @Test
    public void formatsViolationMessage() {

        given(item.getResults()).willReturn(Collections.singleton("RESULT"));

        String violationMessage = resultRule.formatViolationMessage(item, "scriptName");

        assertThat(violationMessage, containsString("scriptName"));
        assertThat(violationMessage, containsString("RESULT"));

    }

}
