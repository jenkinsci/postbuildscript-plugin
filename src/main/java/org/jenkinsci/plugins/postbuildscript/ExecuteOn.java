package org.jenkinsci.plugins.postbuildscript;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 * @deprecated Still here for downwards compatibility. Please use {@link org.jenkinsci.plugins.postbuildscript.model.ExecuteOn} instead
 */
@Deprecated
public enum ExecuteOn  {

    MATRIX,
    AXES,
    BOTH

}
