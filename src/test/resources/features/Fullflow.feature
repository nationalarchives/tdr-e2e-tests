#Feature will not work with chromedriver in headless mode due to following bug: https://bugs.chromium.org/p/chromedriver/issues/detail?id=2521&q=directory%20upload&colspec=ID%20Status%20Pri%20Owner%20Summary

Feature: Full user journey
  Scenario: Full transfer workflow
    Given A logged out user
    When the user navigates to TDR Home Page
    And the user clicks on the Start now button
    And the logged out user enters valid credentials
    And the user clicks the continue button
    Then the user should be on the homepage page
    When the logged in user navigates to the series page
    And the user selects the series MOCK1 123
    And the user clicks the continue button
    Then the user should be on the transfer-agreement page
    When the user selects yes to all transfer agreement checks
    And the user confirms all the records are open
    And the user confirms that DRO has signed off on the records
    And the user clicks the continue button
    Then the user will be on a page with the title "Upload your records"
    When the user selects directory containing: testfile1
    And the user clicks the continue button
    Then the user will be on a page with the title "Checking your records"
    And the file checks completed banner should not be visible
    And the file checks continue button should be disabled
