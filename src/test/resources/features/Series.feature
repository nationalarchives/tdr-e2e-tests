Feature: Series Page

  Scenario: Logged in user selects nothing from dropdown
    Given A logged in user
    When the logged in user navigates to the series page
    And the user clicks the .govuk-button element
    Then the logged in user should stay at the series page
    And the user will see a form error message This field is required

  Scenario: Logged in user selects a series from the dropdown
    Given A logged in user
    When the logged in user navigates to the series page
    And the user clicks the .govuk-select element
    And the logged in user selects the series MOCK1 123
    And the user clicks the continue button
    Then the user should be at the transfer-agreement page

  Scenario: Logged in user selects 'back' when on Series page
    Given A logged in user
    When the logged in user navigates to the series page
    And the user clicks the .govuk-back-link element
    Then the user should be at the dashboard page
