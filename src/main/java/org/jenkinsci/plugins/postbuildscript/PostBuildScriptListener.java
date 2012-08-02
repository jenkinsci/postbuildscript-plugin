package org.jenkinsci.plugins.postbuildscript;

import hudson.Extension;
import hudson.matrix.MatrixProject;
import hudson.maven.MavenModuleSet;
import hudson.model.*;
import hudson.model.listeners.RunListener;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * @author Gregory Boissinot
 */
@Extension
public class PostBuildScriptListener extends RunListener<Run> implements Serializable {

    private static Logger LOGGER = Logger.getLogger(PostBuildScriptListener.class.getName());

    @Override
    public void onStarted(Run run, TaskListener listener) {
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


    private void putLastListPostBuildPublisher(Class<? extends AbstractProject> jobClass, AbstractProject project) throws PostBuildScriptException {
        Field publishersField;
        try {
            publishersField = jobClass.getDeclaredField("publishers");
            publishersField.setAccessible(true);
            DescribableList<Publisher, Descriptor<Publisher>> publishers = (DescribableList<Publisher, Descriptor<Publisher>>) publishersField.get(project);
            Iterator<Publisher> it = publishers.iterator();
            while (it.hasNext()) {
                Publisher curPublisher = it.next();
                if (curPublisher instanceof PostBuildScript) {
                    publishers.remove(curPublisher.getClass());
                    publishers.add(curPublisher);
                }
            }
            publishersField.set(project, publishers);

        } catch (NoSuchFieldException nse) {
            throw new PostBuildScriptException(nse);
        } catch (IllegalAccessException iae) {
            throw new PostBuildScriptException(iae);
        } catch (IOException ioe) {
            throw new PostBuildScriptException(ioe);
        }
    }

}