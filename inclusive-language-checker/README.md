# inclusive-language-checker

What is the Inclusive Language Checker?

The tool works by analysing your text and flagging words and phrases that may be insensitive or biased towards certain groups of people. For example, it may flag the use of gendered language or offensive terms, and suggest alternatives that are more inclusive and respectful.

Using more inclusive language is important because it helps to create a more welcoming and respectful environment for all people, regardless of their background or identity. It can also help to avoid perpetuating harmful stereotypes or biases.

The main goal here to help an organisation achieve it's aims to provide a more inclusive working environment for everyone; this is our mission to help continue that push for inclusivity within our codebase and for the greater good within our workplace. This is why I have called this the “Inclusive Language Tool”.

Running the Woke tool via Docker:

The command we will be using is shown below:

docker run -v $(pwd):/src -w /src getwoke/woke



Daniel.Nana-Klouvi@FVFGH0H0Q05R global-gplan-outdoor % docker run -v $(pwd):/src -w /src getwoke/woke



app-support/app-support.tf:4:2-10: `blackbox` may be insensitive, use `closed-box` instead (error)
  blackbox_target_urls = []
  ^
The "docker run" part of the command tells Docker to run a container based on the image specified after it, in this case "getwoke/woke". The "-v $(pwd):/src" option specifies a volume to mount in the container, which in this case is the current working directory on the host machine. The "-w /src" option sets the working directory inside the container to "/src".

The command "docker run -v $(pwd):/src -w /src getwoke/woke" does the following:

It tells Docker to run a container based on the "getwoke/woke" image.

The "-v $(pwd):/src" option specifies that the current working directory on the host machine should be mounted as a volume inside the container. This means that any files or directories in the current working directory on the host machine will be accessible inside the container. The "/src" part of the option specifies the mount point inside the container.

The "-w /src" option sets the working directory inside the container to "/src", which is the mount point specified in the previous option.

When the container starts up, it will run the command specified in the "getwoke/woke" image. 

So overall, the command is telling Docker to run a container based on the "getwoke/woke" image, and to mount the current working directory on the host machine as a volume inside the container, with the working directory set to "/src" inside the container.

Setting up the Hook within Jenkins:

I then created a Hook called InclusivityCheck written in Groovy. This is a class in a package called com.global.hooks. The class is called InclusivityCheck. It has a method called preBuild which takes a Map object as an argument. The method runs an inclusive language check to scan the commit for non-inclusive language. It does this by running a Docker container with the image getwoke/woke:0.19.0 and executing the command woke -c https://raw.githubusercontent.com/global-shared/inclusivity-check-config/master/config.yaml. The output of this command is then displayed in the console.

package com.global.hooks

class InclusivityCheck {

    def steps

    def preBuild(Map data) {
        String ws = data.ws

        steps.echo("Running Inclusive Language Check to scan the commit for non-inclusive language. Please wait.")

         steps.node {
            steps.ws(ws) {
                steps.stage('InclusivityCheck') {
                    steps.ansiColor("xterm") {
                        steps.docker.withRegistry("https://docker.io", 'docker-hub') {
                            steps.docker.image("getwoke/woke:0.19.0").inside('--entrypoint=\'\'') {
                                steps.sh("woke -c https://raw.githubusercontent.com/global-shared/inclusivity-check-config/master/config.yaml")
                            }
                        }
                    }
                }
            }
        }
    }
}
Creating a Jenkinsfile:

Afterwards, I set up a Jenkinsfile within Shared Testing. This is a Groovy method call to buildGradle with a map as an argument. The map has four key-value pairs:

aws: a map with two key-value pairs, role and account, which are set to "jenkins-devops" and "873744935058" respectively.

images: a map with one key-value pair, "inclusive-language-test": ".", which maps the string "inclusive-language-test" to the current directory ".".

gradleImage: a string "gradle".

gradleTag: a string "7.3-jdk17".

This method call is used to configure the build process for a Gradle project. It sets up AWS credentials, specifies an image to build, and sets the Gradle version to use.



setup

buildGradle([
    aws: [role: "jenkins-devops", account: "873744935058"],
    images: ["inclusive-language-test": "."],
    gradleImage: "gradle",
    gradleTag: "7.3-jdk17"
])
Modifying buildGradle to accept our new Hook: 

I then modified buildGradle to accept our new InclusivityCheck hook. This is a Groovy script that defines a method called call that takes a map as an argument. The method initializes several variables based on the values in the map, including steps, styles, reports, units, csvReports, scan, gradleWrapper, nexusRelease, sendPreDeploy, sendSuccess, allBranches, dependencyTrack, nexus, extraDockerBuild, notifySecret, gradleImage, and gradleTag.

The method then creates several objects, including buildHook, inclusiveHook, reportHook, and dependencyTrackHook, which are instances of classes BuildGradle, InclusivityCheck, ReportJava, and DependencyTrack, respectively. These objects are added to a list called hooks.

Finally, the method creates an instance of the CommonProcess class and calls its init and run methods, passing in the config map and the hooks list as arguments.

In summary, this script appears to be setting up a build process for a Gradle project, with hooks for various tasks such as inclusivity checks, reporting, and dependency tracking.



import com.global.CommonProcess
import com.global.hooks.BuildGradle
import com.global.hooks.DependencyTrack
import com.global.hooks.ReportJava
import com.global.hooks.NotifyTeams

def call(Map config = [:]) {
    List steps = config.get('build', [])
    List styles = config.get('styles', [])
    Map reports = config.get('reports', [:])
    List units = config.get('units', [])
    Map csvReports = config.get('csvReports', [:])
    Boolean scan = config.get('scan', false)
    Boolean gradleWrapper = config.get('wrapper', false)
    Boolean nexusRelease = config.get('release', false)
    Boolean sendPreDeploy = config.get('notifySendPreDeploy', false)
    Boolean sendSuccess = config.get('notifySendSuccess', false)
    Boolean allBranches = config.get('notifyOnAllBranches', false)
    Boolean dependencyTrack = config.get('dependencyTrack', false)
    Boolean nexus = config.get('nexus', false)
    String extraDockerBuild = config.get('extraDockerBuild', '')
    String notifySecret = config.get('notifySecret', '')
    String gradleImage = config.get('gradleImage', null)
    String gradleTag = config.get('gradleTag', null)

    String command = gradleWrapper ? './gradlew' : 'gradle'

    def buildHook = new BuildGradle(
        steps: this,
        buildSteps: steps,
        scan: scan,
        nexus: nexus,
        wrapper: gradleWrapper,
        release: nexusRelease,
        extraDockerBuild: extraDockerBuild
    )

    if (gradleImage) {
        buildHook.gradleImage = gradleImage
    }
    if (gradleTag) {
        buildHook.gradleTag = gradleTag
    }
    
    def inclusiveHook = new InclusivityCheck (
        steps: this
    )
    
    def reportHook = new ReportJava(
        steps: this,
        styles: styles,
        reports: reports,
        units: units,
        csvReports: csvReports
    )

    def hooks = [buildHook, inclusiveHook, reportHook]

    if (dependencyTrack) {
        def dependencyTrackHook = new DependencyTrack(
            steps: this,
            nexus: nexus,
            command: command
        )

        if (gradleImage) {
            dependencyTrackHook.gradleImage = gradleImage
        }

        if (gradleTag) {
            dependencyTrackHook.gradleTag = gradleTag
        }

        hooks.add(dependencyTrackHook)
    }

    if (!notifySecret.is('')) {
        def notifyHook = new NotifyTeams(
            steps: this,
            secret: notifySecret,
            sendSuccess: sendSuccess,
            sendPreDeploy: sendPreDeploy,
            allBranches: allBranches
        )

        hooks.add(notifyHook)
    }

    def common = new CommonProcess(this)
    common.init(config, hooks)
    common.run()
}

Creating config.yaml:

The final step was configuring the config. This is a configuration file for an inclusive language check. It defines a set of rules for identifying non-inclusive language in code. Each rule has a name, a list of terms to search for, and a list of alternative terms to suggest instead.

For example, the first rule is called "whitelist" and searches for the terms "whitelist" and "white-list". It suggests using the alternative term "allowlist" instead.

The file also includes optional severity levels for each rule, which can be set to "warn" or "error" to indicate the severity of the non-inclusive language.

Overall, this configuration file is used to help ensure that code is written in a more inclusive and respectful manner.

rules:
  - name: whitelist
    terms:
      - whitelist
      - white-list
    alternatives:
      - allowlist
    # severity: warn # disabled to show that error is the default

  - name: blacklist
    terms:
      - blacklist
      - black-list
    alternatives:
      - denylist
      - blocklist
      
  - name: master
    terms:
      - master
    alternatives:
      - leader
      - active
      - primary

  - name: slave
    terms:
      - slave
    alternatives:
      - follower
      - secondary
      - standby
      
  - name: grandfathered
    terms:
      - grandfathered
    alternatives:
      - legacy status
      
  - name: man hours
    terms:
      - man hours
    alternatives:
      - person hours
      - engineer hours
      
  - name: sanity check
    terms:
      - sanity check 
    alternatives:
      - quick check
      - confidence check
      
  - name: dummy value 
    terms:
      - dummy value
    alternatives:
      - sample value
      
  - name: man-in-the-middle
    terms:
      - man-in-the-middle
    alternatives:
      - in-path attack
      
   - name: whitespace
    terms:
      - whitespace
    alternatives:
      - empty-space
      
  - name: mob progamming
    terms:
      - mob programming
    alternatives:
      - herd programming
    severity: warning

This guide will now explain what changes you’ll need to make to your build.gradle to report to SonarCloud.

:blue_book: Instructions

Please follow these steps carefully:

Plugin Additions:

In your build.gradle file, you have added several plugins to the plugins block. These plugins provide various functionalities for your Gradle project. Notably, the org.sonarqube plugin is added for SonarCloud integration. It should look similar to this:



sonarqube {
    properties {
        property "sonar.projectKey", "global-shared-test_inclusive-language-checker"
        property "sonar.organization", "global-shared-test-1"
        property "sonar.sourceEncoding", "UTF-8"
        property "sonar.host.url", "https://sonarcloud.io"
        property 'sonar.externalIssuesReportPaths', "inclusive.output.json"
    }
}
Here, you're configuring the properties for SonarCloud analysis:

sonar.projectKey: This is a unique key for your project on SonarCloud. You’d have set it by following this guide.

sonar.organization: This specifies the organisation associated with your project on SonarCloud. You’d have set it by following this guide.

sonar.sourceEncoding: This defines the source code encoding, set to "UTF-8".

sonar.host.url: The URL of your SonarCloud instance, set to "https://sonarcloud.io".

sonar.externalIssuesReportPaths: This points to the path where external issues report JSON is located. This is because the Inclusive Language Checker is meant to generate an external issues report in JSON format. 

To complete the setup, you’ll need to add inclusive.output.json. The sonar.externalIssuesReportPaths property is used to tell SonarCloud where to find this external issues report. In your configuration, you're setting it to "inclusive.output.json". The Inclusive Language Check gets ran by default Jenkins pipelines, and writes the output in the root of your workspace at inclusive.output.json - to include this in your Sonar report, add the line: property 'sonar.externalIssuesReportPaths', "inclusive.output.json"

This means that you expect the Inclusive Language Checker to generate a JSON report file named inclusive.output.json, and this file will be used by SonarCloud to incorporate the issues identified by the Inclusive Language Checker into the overall code analysis. 

Alternatively if this doesn’t seem to work, you can use the special rootProject gradle variable which refers to the root directory: property 'sonar.externalIssuesReportPaths', "$rootProject.projectDir/inclusive.output.json"

For the Inclusive Language Check to take effect, it is important for the project CI/CD to be setup using the common hook buildGradle. 

Alternatively, if the project doesn’t follow the common hook - you can incorporate the InclusivityCheck hook manually into your CI/CD pipeline if the common hook set up isn’t feasible. 

