Feature: Login

  Scenario: Navigate to the TDR home page as a logged out user
    Given A logged out user
    When the logged out user navigates to TDR Home Page
    And the user clicks the .govuk-button--start element
    Then the logged out user should be at the auth page

  Scenario: Navigate to the TDR home page as a logged in user
    Given A logged in user
    When the logged out user navigates to TDR Home Page
    And the user clicks the .govuk-button--start element
    Then the user should be at the dashboard page

  Scenario: Log in to TDR with correct credentials
    Given A logged out user
    When the logged out user navigates to TDR Home Page
    And the user clicks the .govuk-button--start element
    And the logged out user enters valid credentials
    And the user clicks the [name='login'] element
    Then the user should be at the dashboard page

  Scenario: Log in to TDR with incorrect credentials
    Given A logged out user
    When the logged out user navigates to TDR Home Page
    And the user clicks the .govuk-button--start element
    And the logged out user enters invalid credentials
    And the user clicks the [name='login'] element
    Then the user will remain on the auth page
    And the user will see the error message Invalid username or password.
