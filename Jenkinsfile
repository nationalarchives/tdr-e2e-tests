library("tdr-jenkinslib")

pipeline {
    agent {
        label "master"
    }
    parameters {
        choice(name: "STAGE", choices: ["intg", "staging"], description: "TDR environment where end to end tests will run")
        string(name: "DEPLOY_JOB_URL", defaultValue: "Not given", description: "URL of Jenkins deploy job that triggered the end to end tests")
        choice(name: "BROWSER", choices: ["firefox", "chrome"], description: "The browser to run the tests in")
        string(name: "DRIVER_VERSION", defaultValue: "v0.26.0", description: "The version of the driver")
    }
    stages {
        stage ("Retrieve Keycloak credentials for environment") {
            agent {
                ecs {
                    inheritFrom "aws"
                    taskrole "arn:aws:iam::${env.MANAGEMENT_ACCOUNT}:role/TDRJenkinsNodeReadParamsRole${params.STAGE.capitalize()}"
                }
            }
            steps {
                script {
                    account_number = tdr.getAccountNumberFromStage(params.STAGE)
                    keycloak_user_key = "/${params.STAGE}/keycloak/admin/user"
                    keycloak_password_key = "/${params.STAGE}/keycloak/admin/password"
                    keycloak_user = sh(script: "python3 /ssm_get_parameter.py ${account_number} ${params.STAGE} ${keycloak_user_key}", returnStdout: true).trim()
                    keycloak_password = sh(script: "python3 /ssm_get_parameter.py ${account_number} ${params.STAGE} ${keycloak_password_key}", returnStdout: true).trim()
                }
            }
        }
        stage("Configure and Run Tests") {
            agent {
                ecs {
                    inheritFrom "transfer-frontend"
                }
            }
            environment {
                DRIVER_LOCATION = "src/driver"
            }
            stages {
                stage("Install Driver") {
                    steps {
                        checkout scm
                        script {
                            if(params.BROWSER == "firefox") {
                                    sh "wget -q -N https://github.com/mozilla/geckodriver/releases/download/${params.DRIVER_VERSION}/geckodriver-${params.DRIVER_VERSION}-linux64.tar.gz -P ~/"
                                    sh "tar -C ~/ -xzf ~/geckodriver-${params.DRIVER_VERSION}-linux64.tar.gz"
                                    sh "rm -f ~/geckodriver-${params.DRIVER_VERSION}-linux64.tar.gz"
                                    sh "mv ~/geckodriver src/driver"
                                }
                                if(params.BROWSER == "chrome") {
                                    sh "wget -q -N http://chromedriver.storage.googleapis.com/${params.DRIVER_VERSION}/chromedriver_linux64.zip -P ~/"
                                    sh "unzip -q ~/chromedriver_linux64.zip -d ~/"
                                    sh "rm ~/chromedriver_linux64.zip"
                                    sh "mv -f ~/chromedriver src/driver"
                                }
                        }
                    }
                }
                stage ("Run Tests") {
                    steps {
                        script {
                            //Hide the output of the test command to stop keycloak credentials appearing in console output
                            sh """
                                set +x
                                sbt test -Dconfig.file=application.${params.STAGE}.conf -Dkeycloak.user=${keycloak_user} -Dkeycloak.password=${keycloak_password} -Dbrowser=${params.BROWSER}
                            """
                        }
                    }
                    post {
                        always {
                            cucumber buildStatus: 'UNSTABLE',
                                fileIncludePattern: '**/*.json',
                                trendsLimit: 10,
                                classifications: [
                                    [
                                        'key':'Browser',
                                        'value':'Chrome'
                                    ]
                                ]
                        }
                    }
                }
            }
        }
    }
    post {

        failure {
            script {
                tdr.postToDaTdrSlackChannel(colour: "danger",
                                            message: " :warning: *End to end tests have failed*\n *TDR Environment*: ${params.STAGE}\n" +
                                                     "  *Deploy Job*: ${DEPLOY_JOB_URL} \n *Cucumber report*: ${BUILD_URL}cucumber-html-reports/overview-features.html"
                )
            }
        }
        fixed {
            script {
                tdr.postToDaTdrSlackChannel(colour: "good",
                                            message: " :green_heart: *End to end tests have succeeded after previous failure*\n *TDR Environment*: ${params.STAGE}\n" +
                                                     "  *Deploy Job*: ${DEPLOY_JOB_URL} \n *Cucumber report*: ${BUILD_URL}cucumber-html-reports/overview-features.html"
                )
            }
        }
    }
}