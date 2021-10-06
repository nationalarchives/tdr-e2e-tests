#Feature: Login
#
#  Scenario: Navigate to the TDR home page as a logged out user
#    Given A logged out user
#    When the user navigates to TDR Home Page
#    And the user clicks on the Start now button
#    Then the logged out user should be on the login page
#
#  Scenario: Navigate to the TDR home page as a logged in user
#    Given A logged in user
#    When the user navigates to TDR Home Page
#    And the user clicks on the Start now button
#    Then the user should be on the dashboard page
#
#  Scenario: Log in to TDR with correct credentials
#    Given A logged out user
#    When the user navigates to TDR Home Page
#    And the user clicks on the Start now button
#    And the logged out user enters valid credentials
#    And the user clicks the continue button
#    Then the user should be on the dashboard page
#
#  Scenario: Log in to TDR with incorrect credentials
#    Given A logged out user
#    When the user navigates to TDR Home Page
#    And the user clicks on the Start now button
#    And the logged out user enters invalid credentials
#    And the user clicks the continue button
#    Then the user will remain on the auth page
#    And the user will see the error message Invalid username or password.
#
#  Scenario: Navigate to a page that does not exist as a logged in user
#    Given A logged in user
#    And the user navigates to a page that does not exist
#    Then the user should see a general service error "Page not found"
