Feature: Sign Out

  Scenario: A logged in user signs out and is redirected to the sign out page
    Given A logged in user
    When the user clicks on the Sign out button
    Then the user should be on the sign-out page

  Scenario: A logged in user logs out and is redirected to the sign in page if they try to access the dashboard
    Given A logged in user
    When the user clicks on the Sign out button
    And the logged out user attempts to access the dashboard page
    Then the logged out user should be on the login page

  Scenario: A logged in user can sign out from the home page
    Given A logged in user
    And the user navigates to TDR Home Page
    When the user clicks on the Sign out button
    Then the user should be on the sign-out page

  Scenario: A logged out user cannot see the sign out link from the home page
    Given A logged out user
    When the user navigates to TDR Home Page
    Then the Sign out button is not displayed on the page
