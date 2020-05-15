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
                CHROME_DRIVER = "src/chromedriver"
                CHROME_DRIVER_VERSION = "79.0.3945.36"
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

def getAccountNumberFromStage() {
    def stageToAccountMap = [
            "intg": env.INTG_ACCOUNT,
            "staging": env.STAGING_ACCOUNT,
            "prod": env.PROD_ACCOUNT
    ]

    return stageToAccountMap.get(params.STAGE)
}