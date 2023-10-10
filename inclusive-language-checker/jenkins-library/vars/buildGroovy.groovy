import com.global.CommonProcess
import com.global.hooks.BuildGradle
import com.global.hooks.DependencyTrack
import com.global.hooks.GithubPackagesAuthentication
import com.global.hooks.InclusivityCheck
import com.global.hooks.NexusAuthentication
import com.global.hooks.ReportJava
import com.global.hooks.SonarHook
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
    List<Map<String, String>> githubPackagesAuth = config.get('githubPackagesAuth', [])
    String extraDockerBuild = config.get('extraDockerBuild', '')
    String notifySecret = config.get('notifySecret', '')
    String gradleImage = config.get('gradleImage', null)
    String gradleTag = config.get('gradleTag', null)

    String command = gradleWrapper ? './gradlew' : 'gradle'

    def buildHook = new BuildGradle(
        steps: this,
        buildSteps: steps,
        wrapper: gradleWrapper,
        release: nexusRelease,
        extraDockerBuild: extraDockerBuild
    )

     def inclusiveHook = new InclusivityCheck (
        steps: this
    )

    if (gradleImage) {
        buildHook.gradleImage = gradleImage
    }
    if (gradleTag) {
        buildHook.gradleTag = gradleTag
    }

    def reportHook = new ReportJava(
        steps: this,
        styles: styles,
        reports: reports,
        units: units,
        csvReports: csvReports
    )

    def hooks = [buildHook, inclusiveHook, reportHook]

    if (nexus || nexusRelease) {
        def nexusAuth = new NexusAuthentication(
            steps: this
        )

        hooks.add(nexusAuth)
    }

    if (!githubPackagesAuth.isEmpty()) {
        def authGithub = new GithubPackagesAuthentication(
            steps: this,
            packageLocations: githubPackagesAuth)
        hooks.add(authGithub)
    }

    if (scan) {
      def sonarHook = new SonarHook(
        steps: this,
        gradleImage: gradleImage,
        gradleTag: gradleTag,
        wrapper: gradleWrapper
      )
      hooks.add(sonarHook)
    }

    if (dependencyTrack) {
        def dependencyTrackHook = new DependencyTrack(
            steps: this,
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