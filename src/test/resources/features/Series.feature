
Feature: Series Page

  Scenario: Logged in user selects nothing from dropdown
    Given A logged in user
    When the logged in user navigates to the series page
    And the logged in user selects nothing
    Then the logged in user should stay at the series page

  Scenario: Logged in user selects series from dropdown
    Given A logged in user
    When the logged in user navigates to the series page
    And the logged in user selects the .govuk-select element
    And the logged in user clicks a series
    Then the logged in user should stay at the series page

  Scenario: Logged in user selects 'back' from Series page
    Given A logged in user
    When the logged in user navigates to the series page
    And the logged in user selects the .govuk-back-link element
    Then the logged in user should be at the dashboard page
