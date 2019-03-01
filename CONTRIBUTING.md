# Contributing

The plugin was created in 2011 by Gregory Boissinot. It was adopted by Daniel Heid in October 2017. Feel free
to contribute as you like by forking the repository and creating pull requests.

## Continuous Integration

Each commit is built by the official Jenkins CI. The current build status is accessible here:

https://ci.jenkins.io/job/Plugins/job/postbuildscript-plugin

## Reporting issues

Please use the
[official Jenkins Jira project and issue tracking software](https://issues.jenkins-ci.org/issues/?jql=component%20%3D%20postbuildscript-plugin)
to report new bugs or request features.

Please first look through [Jira](https://issues.jenkins-ci.org/issues/?jql=component%20%3D%20postbuildscript-plugin). If a ticket
already exists, please add a comment and try to explain the issue a little more further. If no ticket exists, please open a new one.

In general please _provide example configurations_ (config.xml, Job DSL scripts, ...) and also screenshots are very helpful to reproduce
problems.

Please try to be constructive. If you have a feedback, it's better to write me a direct message. 

## Pull Requests

For bug fixes and enhancements to existing features, first make sure an issue is filed by checking [this Jira filter](https://issues.jenkins-ci.org/issues/?jql=status%20not%20in%20(Resolved%2C%20Closed%2C%20Done)%20AND%20component%20%3D%20postbuildscript-plugin)

After that please create a pull request on GitHub with your change and link to the JIRA issue in the PR, and link to
the PR from the JIRA issue.

## Building and installing a snapshot version

Currently you need to build with Java 8. To let Maven use Java 8, please set your JAVA_HOME environment variable, e.g.
on macOS:

    export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_192.jdk/Contents/Home

Run

	mvn clean package

Then copy the resulting ./target/postbuildscript.hpi file to the $JENKINS_HOME/plugins directory.
Don't forget to restart Jenkins afterwards.

Alternatively use the plugin management console (http://localhost:8080/pluginManager/advanced)
to upload the hpi file. You have to restart Jenkins in order to find the plugin in the installed plugins list.

## Testing

Execute

   mvn verify
   
to ensure every test works. This goal will also run some integration tests that will take some more time.

## Using HPI Maven plugin to test your changes locally

If you want to try running recent development changes, rather than released binaries, you have two options.
You can run directly from the root of the plugin repository:

    mvn hpi:run

Then visit http://localhost:8080/jenkins/ to play with the plugin.

If your IDE supports compile-on-save mode this is especially convenient since each `hpi:run` will pick up compiled
changes without needing to run to `package` phase.
