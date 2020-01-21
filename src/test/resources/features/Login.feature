Feature: Login

#  Scenario: Navigate to TDR Home Page as a logged out user
#    Given A logged out user
#    When the logged out user visits url
#    And the logged out user clicks the .govuk-button--start element
#    Then the logged out user should be at the auth page
#    And the logged out user enters valid credentials
#    And the logged out user clicks the [name='login'] element
#    Then the logged in user should be at the dashboard page
#
#  Scenario: Navigate to TDR Home Page as a logged out user with incorrect credentials
#    Given A logged out user
#    When the logged out user visits url
#    And the logged out user clicks the .govuk-button--start element
#    Then the logged out user should be at the auth page
#    And the logged out user enters invalid credentials
#    And the logged out user clicks the [name='login'] element
#    Then the logged out user will remain on the auth page
#    And the user will see an error message

  Scenario:
    Given A logged out user
    When the logged out user visits url
    And the logged out user clicks the .govuk-button--start element
    Then the logged out user should be at the auth page
    And the logged out user enters valid credentials
    And the logged out user clicks the [name='login'] element
    Then the logged in user should be at the dashboard page
    When the logged in user navigates to google
    And the logged in user navigates back to TDR Home Page
    And the logged in user clicks the .govuk-button--start element
    Then the logged in user should be at the dashboard page