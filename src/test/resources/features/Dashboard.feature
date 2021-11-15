Feature: Dashboard Page

  Scenario: Dashboard page is accessed by a logged out user
    Given A logged out user
    And the logged out user attempts to access the dashboard page
    Then the logged out user should be on the login page

  Scenario: Dashboard page is accessed by a standard user
    Given A logged in standard user
    And the logged in user navigates to the dashboard page
    Then the user will be on a page with a heading "Upload your records to start a new transfer"

  Scenario: Dashboard page is accessed by a judgment user
    Given A logged in judgment user
    And the logged in user navigates to the dashboard page
    Then the user will be on a page with a heading "Upload your judgment to start a new transfer"

  Scenario: Dashboard page is submitted by a judgment user
    Given A logged in judgment user
    And the logged in user navigates to the dashboard page
    And the user clicks the continue button
    Then the user should be on the transfer-agreement page
