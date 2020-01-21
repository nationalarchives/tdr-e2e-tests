Feature: Login

  Scenario: Navigate to TDR Home Page as a logged out user
    Given A logged out user
    When the logged out user visits url
    And the logged out user clicks the .govuk-button--start element
    Then the logged out user should be at the auth page
    And the logged out user enters valid credentials
    And the logged out user clicks the [name='login'] element
    Then the logged in user should be at the dashboard page
  Scenario: Another scenario
    Given A logged out user
    When the logged out user visits url
  Scenario: Another scenario 2
    Given A logged out user
    When the logged out user visits url