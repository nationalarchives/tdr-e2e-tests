Feature: Transfer Agreement Page

  Scenario: A logged in user completes the Transfer Agreement form correctly
    Given A logged in user
    And the logged in user navigates to the series page
    And the user clicks the .govuk-select element
    And the logged in user selects the series MOCK1 123
    And the user clicks the continue button
    And the user navigates to the transfer-agreement page
    When the user selects yes to all transfer agreement checks
    And the user clicks the droAppraisalSelection checkbox
    And the user clicks the droSensitivity checkbox
    And the user clicks the continue button
    Then the user should be at the upload page