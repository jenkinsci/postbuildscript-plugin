package org.jenkinsci.plugins.postbuildscript;

import org.jenkinsci.plugins.postbuildscript.model.Script;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Collection;

/**
 * @author Gregory Boissinot
 * @deprecated Still here for downwards compatibility. Please use {@link Script} instead
 */
@Deprecated
public class GroovyScriptContent extends Script {

    @DataBoundConstructor
    public GroovyScriptContent(String content, Collection<String> results) {
        super(results, content);

    }

}
