package com.global.hooks

class InclusivityCheck {

    def steps

    def preBuild(Map data) {
        String ws = data.ws

        steps.echo("Running Inclusive Language Check to scan the commit for non-inclusive language. Please wait.")

         steps.node {
            steps.ws(ws) {
                steps.stage('Inclusivity Check') {
                    steps.ansiColor("xterm") {
                        steps.docker.withRegistry("https://docker.io", 'docker-hub') {
                            steps.docker.image("getwoke/woke:0.19.0").inside('--entrypoint=\'\'') {
                                steps.sh("woke -c https://raw.githubusercontent.com/global-shared/inclusivity-check-config/master/config.yaml -o sonarqube > inclusive.output.json")
                            }
                        }
                    }
                }
            }
        }
    }
}