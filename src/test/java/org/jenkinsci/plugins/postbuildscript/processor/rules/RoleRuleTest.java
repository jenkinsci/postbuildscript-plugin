package org.jenkinsci.plugins.postbuildscript.processor.rules;

import hudson.model.AbstractBuild;
import org.jenkinsci.plugins.postbuildscript.model.PostBuildItem;
import org.jenkinsci.plugins.postbuildscript.model.Role;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RoleRuleTest {

    @InjectMocks
    private RoleRule roleRule;

    @Mock
    private AbstractBuild<?, ?> build;

    @Mock
    private PostBuildItem item;

    @Test
    public void allowsRunningOnSlaveWhenBuiltStrIsDefined() {

        given(build.getBuiltOnStr()).willReturn("builtOnStr");
        given(item.shouldRunOnSlave()).willReturn(true);

        boolean actual = roleRule.allows(item, true);

        assertThat(actual, is(true));
        verify(item, never()).shouldRunOnMaster();

    }

    @Test
    public void deniesRunningOnMasterWhenBuiltStrIsDefined() {

        given(build.getBuiltOnStr()).willReturn("builtOnStr");
        given(item.shouldRunOnSlave()).willReturn(false);

        boolean actual = roleRule.allows(item, true);

        assertThat(actual, is(false));
        verify(item, never()).shouldRunOnMaster();

    }

    @Test
    public void allowsRunningOnMasterWhenBuiltStrIsUndefined() {

        given(item.shouldRunOnMaster()).willReturn(true);

        boolean actual = roleRule.allows(item, true);

        assertThat(actual, is(true));
        verify(item, never()).shouldRunOnSlave();

    }

    @Test
    public void deniesRunningOnSlaveWhenBuiltStrIsUndefined() {

        given(item.shouldRunOnMaster()).willReturn(false);

        boolean actual = roleRule.allows(item, true);

        assertThat(actual, is(false));
        verify(item, never()).shouldRunOnSlave();

    }

    @Test
    public void formatsViolationMessage() {

        given(item.getRole()).willReturn(Role.MASTER);

        String violationMessage = roleRule.formatViolationMessage(item, "scriptName");

        assertThat(violationMessage, containsString("scriptName"));
        assertThat(violationMessage, containsString("MASTER"));

    }

}
