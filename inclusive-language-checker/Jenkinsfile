setup

buildGradle([
    aws: [role: "jenkins-devops", account: "873744935058"],
    images: ["inclusive-language-test": "."],
    gradleImage: "gradle",
    gradleTag: "7.3-jdk17",
    scan: true,
    build: [
        [
          name: "Build and Test",
          commands: [
           "gradle build"
          ]
        ],
    ],
])