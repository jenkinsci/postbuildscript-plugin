package org.jenkinsci.plugins.postbuildscript;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Project;
import hudson.model.Run;
import hudson.model.TaskListener;
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

        Project jenkinsProject = (Project) run.getParent();

        Field publishersField;
        try {
            publishersField = Project.class.getDeclaredField("publishers");
            publishersField.setAccessible(true);
            DescribableList<Publisher, Descriptor<Publisher>> publishers = (DescribableList<Publisher, Descriptor<Publisher>>) publishersField.get(jenkinsProject);
            Iterator<Publisher> it = publishers.iterator();
            while (it.hasNext()) {
                Publisher curPublisher = it.next();
                if (curPublisher instanceof PostBuildScript) {
                    publishers.remove(curPublisher.getClass());
                    publishers.add(curPublisher);
                }
            }
            publishersField.set(jenkinsProject, publishers);

        } catch (NoSuchFieldException nse) {
            LOGGER.severe("[PostBuildScript] - Severe error to start" + nse.getMessage());
            nse.printStackTrace();
        } catch (IllegalAccessException ie) {
            LOGGER.severe("[PostBuildScript] - Severe error to start" + ie.getMessage());
            ie.printStackTrace();
        } catch (IOException ioe) {
            LOGGER.severe("[PostBuildScript] - Severe error to start" + ioe.getMessage());
            ioe.printStackTrace();
        }
    }
}
