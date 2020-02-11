pipeline {
    agent {
        label "master"
    }
    parameters {
        choice(name: "STAGE", choices: ["intg", "staging", "prod"], description: "The stage you are building the front end for")
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
                    account_number = getAccountNumberFromStage()
                    keycloak_user_key = "/${params.STAGE}/keycloak/admin/user"
                    keycloak_password_key = "/${params.STAGE}/keycloak/admin/password"
                    keycloak_user = sh(script: "python /ssm_get_parameter.py ${account_number} ${params.STAGE} ${keycloak_user_key} >keycloak_user.txt", returnStdout: true)
                    keycloak_password = sh(script: "python /ssm_get_parameter.py ${account_number} ${params.STAGE} ${keycloak_password_key} >keycloak_password.txt", returnStdout: true)
                    stash includes: "keycloak_user.txt", name: "keycloak_user"
                    stash includes: "keycloak_password.txt", name: "keycloak_password"
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
                CHROME_DRIVER = "src/chromedriver"
                CHROME_DRIVER_VERSION = "79.0.3945.36"
                TDR_USER_NAME = "${params.TDR_USER}"
                TDR_PASSWORD = "${params.TDR_PASSWORD}"
            }
            stages {
                stage("Install Chrome Driver") {
                    steps {
                        checkout scm
                        sh "wget -q -N http://chromedriver.storage.googleapis.com/${env.CHROME_DRIVER_VERSION}/chromedriver_linux64.zip -P ~/"
                        sh "unzip -q ~/chromedriver_linux64.zip -d ~/"
                        sh "rm ~/chromedriver_linux64.zip"
                        sh "mv -f ~/chromedriver src"
                    }
                }
                stage ("Run Tests") {
                    steps {
                        script {
                            unstash "keycloak_user"
                            unstash "keycloak_password"
                            keycloak_user = sh(script: 'cat keycloak_user.txt', returnStdout: true).trim()
                            keycloak_password = sh(script: 'cat keycloak_password.txt', returnStdout: true).trim()
                            //Hide the output of the test command to stop keycloak credentials appearing in console output
                            sh """
                                set +x
                                sbt test -Dconfig.file=application.${params.STAGE}.conf -Dkeycloak.user=${keycloak_user} -Dkeycloak.password=${keycloak_password}
                            """
                        }
                    }
                }
                stage("Generate HTML report") {
                    steps {
                        cucumber buildstatus: 'UNSTABLE',
                                fileIncludePattern: '**/*.json',
                                trendsLimit: 10,
                                classifications: [
                                        [
                                                'key':'Browser',
                                                'value':'Firefox'
                                        ]
                                ]
                    }
                }
            }
        }
    }
}

def getAccountNumberFromStage() {
    def stageToAccountMap = [
            "intg": env.INTG_ACCOUNT,
            "staging": env.STAGING_ACCOUNT,
            "prod": env.PROD_ACCOUNT
    ]

    return stageToAccountMap.get(params.STAGE)
}