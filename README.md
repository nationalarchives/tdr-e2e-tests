# TDR e2e Tests

This repository provides end-to-end tests for the TDR application.

## Running Tests Locally

1. Set up TDR frontend application to run the full stack locally: https://github.com/nationalarchives/tdr-transfer-frontend/blob/master/README.md

2. Ensure that the Chrome/Firefox browsers are available locally

3. Depending on which browser you want to run the tests on:
   
   (a) Download and unzip chromedriver locally: https://chromedriver.chromium.org/downloads
   * **NOTE:** Ensure the chromedriver version is compatible with the local Chrome browser version
   (b) Download and unzip geckodriver locally: https://github.com/mozilla/geckodriver/releases
 
4. Clone the TDR End-to-End Tests project

5. Ensure the following Intellij plugins are installed:

    * Gherkin: https://plugins.jetbrains.com/plugin/9164-gherkin    
    * Scala Cucumber: https://plugins.jetbrains.com/plugin/7460-cucumber-for-scala
    * Java Cucumber: https://plugins.jetbrains.com/plugin/7212-cucumber-for-java
    
6. Ensure that you:
      * Are Using AWS credentials that allow access to the TDR environment that tests are being run against
      * Can access the TDR environment that tests are being run against via IP address


### Run the tests from the command line

By default, the tests will run against the TDR integration environment

1. Set environment variables:
    * `$ export DRIVER_LOCATION=[location of the driver executable downloaded in step 3]`
    * `$ export INTG_AWS_ACCOUNT_NUMBER=[aws account number of environment where tests are being run]`
    * If running tests on macOS - Test configuration default is set to the Linux OS location; macOS has the Firefox binary in a different location to Linux. If running the e2e tests on a Mac using Firefox, set the additional the following environment variable:
        * `$ export FIREFOX_BINARY_LOCATION=/Applications/Firefox.app/Contents/MacOS/firefox-bin`
  
2. If you are running the test against your locally running TDR frontend project, set the optional environment variable:
    * `$ export TDR_BASE_URL=http://localhost:9000`

3. Run the tests
    ```
    $ sbt test -Dkeycloak.user.admin.secret=[local tdr user admin client secret] -Dkeycloak.backendchecks.secret=[backend checks for stage that tests are being run against]
    ```

    * `-Dkeycloak.user.admin.secret`: this should be the client secret for the tdr-user-admin client secret set for the local Keycloak server when setting up the TDR frontend application

### Run the tests from Intellij

By default, the tests will run against the TDR integration environment

1. Go to the Feature or Scenario to run
2. Click on the double green arrows in the margin next to the Feature/Scenario
3. Select the "Create Run Configuration" option from the menu. If this option is not there, select "Modify Run Configuration"
4. From the "Create Run Configuration" options select the first option which is the Cucumber Java run option.
5. In the dialog box fill in the following information:
      * **Glue**: `"classpath:steps/"` (**NOTE**: include the double quotes)
      * **VM options**: `-Dkeycloak.user.admin.secret=[local tdr user admin client secret] -Dkeycloak.backendchecks.secret=[backend checks for stage that tests are being run against]`
      * **Environment Variables**: `DRIVER_LOCATION=[location of driver executable downloaded in step 3];INTG_AWS_ACCOUNT_NUMBER=[account number for the integration environment]`
        * Running On macOS **NOTE**: For running on macOS, an additional environment variable needs to be set: `FIREFOX_BINARY_LOCATION=/Applications/Firefox.app/Contents/MacOS/firefox-bin`
        * Running Against Local Frontend Note: If you are running the tests against your locally running TDR frontend project, set the optional environment variable: `TDR_BASE_URL=http://localhost:9000`
      * Leave the default variables for the other options
6. Additionally, if you would like these settings applied for all Features and Scenarios:
    1. On the menu bar, select 'Run'
    2. Select 'Edit Configurations...'
    3. Select 'Cucumber Java'
    4. Select 'Edit Configuration Templates...'
    5. Enter the same dialog box information (mentioned at step 5) in the corresponding boxes

7. Run the Feature or Scenario created.

### Using Tags

Cucumber provides "tagging" configuration to control what features/scenarios are run. This is useful if you want to only run some tests locally.

1. In the `TestRunner` class set the tags option as follows: `tags = Array("@testsIWantToRun")`
2. Select the feature(s) and/or scenario(s) you want to run and annotate them with the tag. 
    * For example:
    ```
    @testsIWantToRun
    Feature: Some feature
    ...
    ```
    * Or:
    ```
    ...
    @testsIWantToRun
    Scenario: Some scenario
    ...
    ```
3. Only those features/scenarios with the tag will run, this will be case whether using the command line, or Intellij

**Note:** If you annotate a feature, all the scenarios within that feature will be run.

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

To retrieve and view the console logs from the headless browser, it is possible to retrieve them from the webDriver in the test code. For example in a step add the following code:

```
val logEntries: LogEntries = webDriver.manage().logs().get("browser")
```

This will retrieve the logs; they can then be processed/used as required.

## Keycloak Version

The version of Keycloak that the project uses, must match the version used by the Keycloak server.

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