package org.jenkinsci.plugins.postbuildscript;

import org.jenkinsci.plugins.postbuildscript.model.ScriptFile;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Collection;

/**
 * @author Gregory Boissinot
 * @deprecated Still here for downwards compatibility. Please use {@link ScriptFile} instead
 */
@Deprecated
public class GroovyScriptFile extends ScriptFile {

    @DataBoundConstructor
    public GroovyScriptFile(String filePath, Collection<String> results) {
        super(results, filePath);
    }

}
