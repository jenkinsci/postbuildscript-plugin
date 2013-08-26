package org.jenkinsci.plugins.postbuildscript;

import hudson.Extension;
import hudson.matrix.MatrixProject;
import hudson.maven.MavenModuleSet;
import hudson.model.*;
import hudson.model.listeners.RunListener;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import hudson.util.VersionNumber;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.logging.Logger;

/**
 * @author Gregory Boissinot
 */
@Extension
public class PostBuildScriptListener extends RunListener<Run> implements Serializable {

    private static Logger LOGGER = Logger.getLogger(PostBuildScriptListener.class.getName());

    @Override
    public void onStarted(Run run, TaskListener listener) {
        if (isChangingOrder()) {
            try {
                Job job = run.getParent();
                if (job instanceof MavenModuleSet) {
                    putLastListPostBuildPublisher(MavenModuleSet.class, (MavenModuleSet) job);
                } else if (job instanceof MatrixProject) {
                    putLastListPostBuildPublisher(MatrixProject.class, (MatrixProject) job);
                } else if (Hudson.getInstance().getPlugin("ivy") != null && job instanceof hudson.ivy.IvyModuleSet) {
                    putLastListPostBuildPublisher(hudson.ivy.IvyModuleSet.class, (AbstractProject) job);
                } else if (job instanceof Project) {
                    putLastListPostBuildPublisher(Project.class, (Project) job);
                }
            } catch (PostBuildScriptException pe) {
                LOGGER.severe("[PostBuildScript] - Severe error to start" + pe.getMessage());
                pe.printStackTrace();
            }
        }
    }


    private boolean isChangingOrder() {
        return Hudson.getVersion().isOlderThan(new VersionNumber("1.478"));
    }

    @SuppressWarnings("unchecked")
    private void putLastListPostBuildPublisher(Class<? extends AbstractProject> jobClass, AbstractProject project) throws PostBuildScriptException {
        Field publishersField;
        try {
            publishersField = jobClass.getDeclaredField("publishers");
            publishersField.setAccessible(true);
            DescribableList<Publisher, Descriptor<Publisher>> publishers = (DescribableList<Publisher, Descriptor<Publisher>>) publishersField.get(project);
            if (publishers != null) {
                for (Publisher publisher : publishers) {
                    if (publisher instanceof PostBuildScript) {
                        publishers.remove(publisher.getClass());
                        publishers.add(publisher);
                    }
                }
                publishersField.set(project, publishers);
            }

        } catch (NoSuchFieldException nse) {
            throw new PostBuildScriptException(nse);
        } catch (IllegalAccessException iae) {
            throw new PostBuildScriptException(iae);
        } catch (IOException ioe) {
            throw new PostBuildScriptException(ioe);
        }
    }

}