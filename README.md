# TDR e2e Tests

This repository provides end to end test for the TDR application.

It currently run tests against the Chrome browser only.

## Running Tests Locally

1. Set up TDR frontend application locally (including keycloak and redis): https://github.com/nationalarchives/tdr-transfer-frontend/blob/master/README.md

2. Ensure that the Chrome browser is available locally

3. Download and unzip chromedriver locally: https://chromedriver.chromium.org/downloads
   * **NOTE:** Ensure the chromedriver version is compatible with the local Chrome browser version
 
4. Clone the TDR End to End Tests project

5. Ensure the following Intellij plugins are installed:

    * Gherkin: https://plugins.jetbrains.com/plugin/9164-gherkin    
    * Scala Cucumber: https://plugins.jetbrains.com/plugin/7460-cucumber-for-scala
    * Java Cucumber: https://plugins.jetbrains.com/plugin/7212-cucumber-for-java

6. Set environment variable:
   ```
   $ export CHROME_DRIVER=[location of chromedriver]
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

## Keycloak Version

The version of Keycloak that the project uses must match the version used by the Keycloak server.

## Running As Jenkins job

To run the tests as a Jenkins job the Jenkins pipeline script carries out the following steps:

1. In the "aws" agent retrieves the TDR environment keycloak administrator user credentials and stashes them.
    * The individual TDR environment keycloak credentials are stored in the environment's parameter store
    * The management account needs access to these credentials to access Keycloak for the TDR environment to create and delete test users
2. In the transfer-frontend agent:
    * Chromedriver is downloaded to the workspace
    * the Keycloak credentials are unstashed and passed in as system properties when running the tests

* The sbt run test command is hidden to ensure the keycloak credentials do not appear in the Jenkins console output.