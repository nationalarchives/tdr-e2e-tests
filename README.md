# TDR e2e Tests

This repository provides end to end test for the TDR application.

It currently run tests against the Chrome browser only.

## Running Tests Locally

1. Set up TDR frontend application locally (including keycloak and redis): https://github.com/nationalarchives/tdr-transfer-frontend/blob/master/README.md

2. Ensure that the Chrome/Firefox browsers are available locally

3. Depending on which browser you want to run the tests on:
   
   (a) Download and unzip chromedriver locally: https://chromedriver.chromium.org/downloads
   * **NOTE:** Ensure the chromedriver version is compatible with the local Chrome browser version
   (b) Download and unzip geckodriver locally: https://github.com/mozilla/geckodriver/releases
 
4. Clone the TDR End to End Tests project

5. Ensure the following Intellij plugins are installed:

    * Gherkin: https://plugins.jetbrains.com/plugin/9164-gherkin    
    * Scala Cucumber: https://plugins.jetbrains.com/plugin/7460-cucumber-for-scala
    * Java Cucumber: https://plugins.jetbrains.com/plugin/7212-cucumber-for-java

6. Set environment variable:
   ```
   $ export DRIVER_LOCATION=[location of the driver executable downloaded in step 3]
   ```

7. Run the tests
   ```
   $ sbt test -Dconfig.file=application.conf -Dkeycloak.user=[local keycloak admin user] -Dkeycloak.password=[local keycloak admin user password]
   ```
   * `-Dkeycloak.user`: this should be the administrator user name set for the local Keycloak server when setting up the TDR frontend application
   * `-Dkeycloak.password`: this should be the administrator user password set for the local Keycloak server when setting up the TDR frontend application

### "Headless" Chromedriver option

To view the tests running in the Chrome browser locally change the chromedriver option in the StepsUtility.scala to "false":

```
chromeOptions.setHeadless(false)
```

#### Debugging in "Headless" Chromedriver

It is possible to debug in the "headless" mode:
1. Set the follow chrome option in the StepsUtility.scala: `chromeOptions.addArguments("--remote-debugging-port=9222")`  
2. Open a new browser window and go to: `localhost:9222`
3. Run the test and refresh the browser window. Can then view the developer tools/outputs for the headless Chromedriver running the tests

See here for more details: https://developers.google.com/web/updates/2017/04/headless-chrome#frontend.

To retrieve and view the console logs from the headless browser it is possible to retreive them from the webDriver in the test code. For example in a step add the following code:

```
val logEntries: LogEntries = webDriver.manage().logs().get("browser")
```

This will retrieve the logs, they can then be processed/used as required.

## Keycloak Version

The version of Keycloak that the project uses must match the version used by the Keycloak server.

## Running As Jenkins job

To run the tests as a Jenkins job the Jenkins pipeline script carries out the following steps:

1. In the "aws" agent retrieves the TDR environment keycloak administrator user credentials and stashes them.
    * The individual TDR environment keycloak credentials are stored in the environment's parameter store
    * The management account needs access to these credentials to access Keycloak for the TDR environment to create and delete test users
2. In the transfer-frontend agent:
    * Chromedriver or Geckodriver is downloaded to the workspace
    * the Keycloak credentials are unstashed and passed in as system properties when running the tests

* The sbt run test command is hidden to ensure the keycloak credentials do not appear in the Jenkins console output.

## Cucumber Reports

### Running Locally

Cucumber reports for the test run are available when running locally here: [project folder]/target/cucumber/html/index.html

### Jenkins job

The Jenkins cucumber reports plugin (https://plugins.jenkins.io/cucumber-reports/) generates a set of reports after each test run.

The test reports are available via a link "Cucumber reports" on the Jenkins' job home page.