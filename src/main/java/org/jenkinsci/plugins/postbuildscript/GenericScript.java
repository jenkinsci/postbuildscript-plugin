package org.jenkinsci.plugins.postbuildscript;

import org.jenkinsci.plugins.postbuildscript.model.ScriptFile;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Gregory Boissinot
 *
 * @deprecated Still here for downwards compatibility. Please use {@link ScriptFile} instead
 */
@Deprecated
public class GenericScript extends ScriptFile {

    @DataBoundConstructor
    public GenericScript(String filePath, String result) {
        super(result, filePath);
    }

}
