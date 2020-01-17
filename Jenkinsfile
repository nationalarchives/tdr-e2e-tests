pipeline {
    agent {
        label "master"
    }
    parameters {
        choice(name: "STAGE", choices: ["intg", "staging", "prod"], description: "The stage you are building the front end for")
        string(name: "TDR_USER", defaultValue: "[enter test user name]")
        string(name: "TDR_PASSWORD", defaultValue: "[enter test user password]")
    }
    stages {
        stage("Configure and Run Tests") {
            agent {
                ecs {
                    inheritFrom "transfer-frontend"
                }
            }
            environment {
                CHROME_DRIVER = "src/chromedriver"
                CHROME_DRIVER_VERSION = "79.0.3945.36"
                TDR_USER_NAME = "${params.TDR_USER}"
                TDR_PASSWORD = "${params.TDR_PASSWORD}"
            }
            stages {
                stage("Install Chrome Driver") {
                    steps {
                        checkout scm
                        sh "wget -N http://chromedriver.storage.googleapis.com/${env.CHROME_DRIVER_VERSION}/chromedriver_linux64.zip -P ~/"
                        sh "unzip ~/chromedriver_linux64.zip -d ~/"
                        sh "rm ~/chromedriver_linux64.zip"
                        sh "mv -f ~/chromedriver src"
                    }
                }
                stage ("Run Tests") {
                    steps {
                        sh "sbt test -Dconfig.file=application.${params.STAGE}.conf"
                    }
                }
            }
        }
    }
}