library("tdr-jenkinslib")
def repo = "tdr-e2e-tests"

pipeline {
  agent {
    label "built-in"
  }
  parameters {
    choice(name: "STAGE", choices: ["intg", "staging"], description: "TDR environment where end to end tests will run")
    string(name: "DEPLOY_JOB_URL", defaultValue: "Not given", description: "URL of Jenkins deploy job that triggered the end to end tests")
    choice(name: "BROWSER", choices: ["firefox", "chrome"], description: "The browser to run the tests in")
  }
  stages {
    stage("Run git secrets") {
      steps {
        script {
          tdr.runGitSecrets(repo)
        }
      }
    }
    stage("Configure and Run Tests") {
      agent {
        ecs {
          inheritFrom "transfer-frontend"
          taskrole "arn:aws:iam::${env.MANAGEMENT_ACCOUNT}:role/TDRJenkinsNodeS3ExportRole${params.STAGE.capitalize()}"
        }
      }
      environment {
        DRIVER_LOCATION = "src/driver"
        CHROME_DRIVER_VERSION = "83.0.4103.39"
        FIREFOX_DRIVER_VERSION = "v0.30.0"
      }
      stages {
        stage("Install Driver") {
          steps {
            checkout scm
            script {
              if(params.BROWSER == "firefox") {

                  sh "wget -q https://github.com/mozilla/geckodriver/releases/download/${env.FIREFOX_DRIVER_VERSION}/geckodriver-${env.FIREFOX_DRIVER_VERSION}-linux64.tar.gz -P ~/"
                  sh "tar -C ~/ -xzf ~/geckodriver-${env.FIREFOX_DRIVER_VERSION}-linux64.tar.gz"
                  sh "rm -f ~/geckodriver-${env.FIREFOX_DRIVER_VERSION}-linux64.tar.gz"
                  sh "mv ~/geckodriver src/driver"
                }
                if(params.BROWSER == "chrome") {
                  sh "wget -q http://chromedriver.storage.googleapis.com/${env.CHROME_DRIVER_VERSION}/chromedriver_linux64.zip -P ~/"
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
              account_number = tdr.getAccountNumberFromStage(params.STAGE)
              tdr_user_admin_secret = sh(script: "python3 /ssm_get_parameter.py ${account_number} ${params.STAGE} /${params.STAGE}/keycloak/user_admin_client/secret", returnStdout: true).trim()
              tdr_backend_checks_secret = sh(script: "python3 /ssm_get_parameter.py ${account_number} ${params.STAGE} /${params.STAGE}/keycloak/backend_checks_client/secret", returnStdout: true).trim()
              sh """
                set +x
                sbt test -Dconfig.file=./src/test/resources/application.${params.STAGE}.conf -Dkeycloak.user.admin.secret=${tdr_user_admin_secret} -Dkeycloak.backendchecks.secret=${tdr_backend_checks_secret} -Dbrowser=${params.BROWSER}
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
                    'value': params.BROWSER.capitalize()
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
    success {
      script {
        if(getPreviousResultForStage(currentBuild.previousBuild) == "FAILURE") {
          tdr.postToDaTdrSlackChannel(colour: "good",
                                message: " :green_heart: *End to end tests have succeeded after previous failure*\n *TDR Environment*: ${params.STAGE}\n" +
                                                            "  *Deploy Job*: ${DEPLOY_JOB_URL} \n *Cucumber report*: ${BUILD_URL}cucumber-html-reports/overview-features.html"
                  )
        }
      }
    }
  }
}

def getPreviousResultForStage(runWrapper) {
  if(runWrapper.rawBuild.getEnvironment().get("STAGE") == params.STAGE) {
    return runWrapper.getResult()
  } else {
    getPreviousResultForStage(runWrapper.previousBuild)
  }
}
