![PostBuildScript](pbs-logo.svg)

# PostBuildScript Jenkins plugin

[![Build Status](https://ci.jenkins.io/job/Plugins/job/postbuildscript-plugin/job/master/badge/icon)](https://ci.jenkins.io/job/Plugins/job/postbuildscript-plugin/job/master/)
[![Jenkins Plugins](https://img.shields.io/jenkins/plugin/v/postbuildscript.svg)](http://updates.jenkins-ci.org/download/plugins/postbuildscript)
[![GitHub contributors](https://img.shields.io/github/contributors/jenkinsci/postbuildscript-plugin.svg)]()

This plugin allows you to run the following actions after a build:

* Batch or shell scripts
* Groovy scripts
* Build steps

You can configure these actions depending on the build status (i.e., only run when build is successful).

With version 1.1.0 scripts can be executed on the master, on slaves or both.

Please refer to the [plugin description](https://wiki.jenkins.io/display/JENKINS/PostBuildScript+Plugin) for further
information.

## Downloads

You'll find the latest HPI files of the plugin here:

http://updates.jenkins-ci.org/download/plugins/postbuildscript

Just put a HPI file into your plugins subdirectory of your Jenkins installation root, e.g. /var/lib/jenkins. Restart
Jenkins afterwards.

## Issue Tracking

Please use the
[official Jenkins Jira project and issue tracking software](https://issues.jenkins-ci.org/issues/?jql=component%20%3D%20postbuildscript-plugin)
to report new bugs or request features.

## Continuous Integration

Each commit is built by the official Jenkins CI. The current build status is accessible here:

https://ci.jenkins.io/job/Plugins/job/postbuildscript-plugin


## Contributing

The plugin was created in 2011 by Gregory Boissinot. It was adopted by Daniel Heid in October 2017. Feel free
to contribute as you like by forking the repository and creating pull requests.

### Reporting issues

Please first look through [Jira](https://issues.jenkins-ci.org/issues/?jql=component%20%3D%20postbuildscript-plugin). If a ticket
already exists, please add a comment and try to explain the issue a little more further. If no ticket exists, please open a new one.

In general please _provide example configurations_ (config.xml, Job DSL scripts, ...) and also screenshots are very helpful to reproduce
problems.

Please try to be constructive. If you have a feedback, it's better to write me a direct message. 

### Pull Requests

For bug fixes and enhancements to existing features, first make sure an issue is filed by checking [this Jira filter](https://issues.jenkins-ci.org/issues/?jql=status%20not%20in%20(Resolved%2C%20Closed%2C%20Done)%20AND%20component%20%3D%20postbuildscript-plugin)

After that please create a pull request on GitHub with your change and link to the JIRA issue in the PR, and link to
the PR from the JIRA issue.

### Building and installing a snapshot version

Run

	mvn clean package

Then copy the resulting ./target/postbuildscript.hpi file to the $JENKINS_HOME/plugins directory.
Don't forget to restart Jenkins afterwards.

Alternatively use the plugin management console (http://localhost:8080/pluginManager/advanced)
to upload the hpi file. You have to restart Jenkins in order to find the plugin in the installed plugins list.

### Using HPI Maven plugin to test your changes locally

If you want to try running recent development changes, rather than released binaries, you have two options.
You can run directly from the root of the plugin repository:

    mvn hpi:run

Then visit http://localhost:8080/jenkins/ to play with the plugin.

If your IDE supports compile-on-save mode this is especially convenient since each `hpi:run` will pick up compiled
changes without needing to run to `package` phase.

## Release Notes

### Version 2.5.0

This version introduces the ability to run Groovy scripts in a sandbox when activated using the checkbox in the configuration view.

Now you can also select for each script whether it should be executed on each axe or on the whole matrix.

* JENKINS-48014 - Allow sandboxing for Groovy scripts
* JENKINS-22489 - Add the ability to specify where each script executes instead of all of the scripts when
added to a matrix job.

### Version 2.4.0

The old option to build only on success or on failure will now be migrated differently: If both is not selected, the
behavior of versions previous to 1.0.0 was to build it independent of the actual build result. Previous versions (after
1.0.0) simply migrate that to do nothing. With version 2.4.0 every build result will be selected in such cases.

* JENKINS-49423 - Upgrade from 0.17 incomplete when scriptOnlyIfSuccess and scriptOnlyIfFailure are both false

### Version 2.3.0

* JENKINS-48931 - PostBuildScript Execute Script Execute Shell Textbox Formatting Broken

### Version 2.2.1

Removing deprecated fields from configuration

Using default values for results and roles on newly created script items.

### Version 2.2.0

Unified script file handling.

There are separate messages now, if a script is not run because the build result or the role does not fit.

### Version 2.1.0

Dan Clayton added the BUILD_RESULT variable to allow handling script actions depending on the build result.

### Version 2.0.0

Refactored the way, how script files are differentiated. This ensures extensibility and helps removing redundant code.

Also removed null pointer exception that occurs on executing a postbuild groovy script on an agent.

### Version 1.1.1

Fixed some bugs that came in with the release of a new major version. Sorry for the inconvenience.

* JENKINS-48197 - Do not initialize fields already loaded from configuration
* JENKINS-48177 - Even marking build as unstable when errors during script execution appear
* JENKINS-48169 - Using better descriptions for role limitations 

### Version 1.1.0

Attention: This is a major refactoring of the plugin. It hasn't been updated for years now. The configuration was changed,
but your existing configuration will be automatically migrated to the new format. I cannot reproduce every configuration, so
please open a bug ticket (and try to be kind, I'm doing this in my free time and don't want to harm anyone).

* JENKINS-11285 - Ability to run postbuild script on a given node

### Version 1.0.0
* JENKINS-30011 - Allow multiple instances of Post Build Scripts as a post build action
* Fix JENKINS-28825 - Confusing error message when leaving script path empty
* Major refactoring, but still trying to be downwards compatible
* Added help files and translations
* JENKINS-24308 - Expost build variable, the AbstractBuild

### Version 0.18
* Fix JENKINS-43637 - Arbitrary code execution vulnerability:
https://github.com/jenkinsci/postbuildscript-plugin/pull/15
* Fix JENKINS-19873 - Batch or a shell script files to execute doesn't run with arguments

### Version 0.17
* Added an option to set the build to unstable instead of failed

### Version 0.16
* Fix JENKINS-16789 - Concurrent builds using postbuildscript plugin will wait for all the instances to finish

### Version 0.15
* Fix JENKINS-19954 - PostBuildScript runs for each matrix configuration rather than after they are all completed when
set to MATRIX

### Version 0.14
* Fix JENKINS-19326 - Groovy script should execute with workspace dir as working directory.
* Fix JENKINS-19072 - No Such Field: 'executeOnMatrixNodes'
* Fix JENKINS-18936 - checkbox is not staying checked
* Fix JENKINS-19033 - execution is always last, rather than where placed in the post-build actions list
* Fix JENKINS-18530 - Postbuild script plugin not respecting order in Post-build Action

### Version 0.13
* Fix JENKINS-11219 - execution is always last, rather than where placed in the post-build actions list

### Version 0.12
* Let user configure script to be executed either on matrix head or axes nodes. Hidden for non-matrix projects

### Version 0.11
* Fix JENKINS-15395 - Add checkbox to run script when build fails (as well as when it passes)

### Version 0.10
* Fix JENKINS-14668 - failure to load without ivy plugin installed

### Version 0.9
* Merge pull request - Make dependency to Ivy plugin optional

### Version 0.8
* Fixed JENKINS-13418 - PostBuildScript plugin does not load its configuration properly
* Change default value for condition to run scripts if the build is not on success.

### Version 0.7
* Fixed a bug where this plugin breaks Promoted Builds Plugin

### Version 0.6
* Added support of Ivy job type

### Version 0.5
* Built for 1.409 (LTS series)
* Added an option to run this build scripts only on success (merge pull request)

### Version 0.4
* Fixed JENKINS-11219 - Add the option to run the script after all the sub elements of a matrix project build have
finished

### Version 0.3
* Added job build steps to post-build actions

### Version 0.2
* Fixed JENKINS-10889 - PostBuildScript plugin fails with Maven2 jobs

### Version 0.1
* Initial Version
