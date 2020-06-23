Feature: Full user journey

  Scenario: Full transfer workflow
    Given A logged out user
    When the user navigates to TDR Home Page
    And the user clicks on the Start now button
    And the logged out user enters valid credentials
    And the user clicks the continue button
    Then the user should be at the dashboard page
    When the logged in user navigates to the series page
    And the user selects the series MOCK1 123
    And the user clicks the continue button
    Then the user should be at the transfer-agreement page
    When the user selects yes to all transfer agreement checks
    And the user confirms that DRO has signed off on the records
    And the user clicks the continue button
    Then the user will be on a page with the title Upload Records
    When the user uploads a file
    Then the page will redirect to the records page after upload is complete
