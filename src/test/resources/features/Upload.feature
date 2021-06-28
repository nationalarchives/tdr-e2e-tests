#Feature will not work with chromedriver in headless mode due to following bug: https://bugs.chromium.org/p/chromedriver/issues/detail?id=2521&q=directory%20upload&colspec=ID%20Status%20Pri%20Owner%20Summary

Feature: Upload
  Scenario: The progress bar is hidden before file upload
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing transfer agreement
    And the user is logged in on the upload page
    Then the progress bar should not be visible

  Scenario: The progress bar is shown after file upload
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing transfer agreement
    And the user is logged in on the upload page
    When the user selects directory containing: largefile
    And the user clicks the continue button
    Then the progress bar should be visible

  Scenario: The records page is shown when the upload is completed
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing transfer agreement
    And the user is logged in on the upload page
    When the user selects directory containing: testfile1
    And the user clicks the continue button
    Then the user will be on a page with the title "Checking your records"
@wip
  Scenario: A logged in user tries to upload multiple set of files to a consignment
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing transfer agreement
    And the user is logged in on the upload page
    When the user selects directory containing: testfile1
    And the user clicks the continue button
    Then the user will be on a page with the title "Checking your records"
    When the user clicks their browser's back button
    Then the user should see the error Your upload was interrupted and could not be completed.

  Scenario: Consignment upload page is accessed by a logged out user
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing transfer agreement
    And the logged out user attempts to access the upload page
    Then the logged out user should be on the login page
