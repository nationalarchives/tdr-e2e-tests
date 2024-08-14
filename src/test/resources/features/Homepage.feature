Feature: Homepage Page

  Scenario: Homepage page is accessed by a logged out user
    Given A logged out standard user
    And the logged out user attempts to access the homepage page
    Then the logged out user should be on the login page

  Scenario: Homepage page is accessed by a standard user
    Given A logged in standard user
    And the logged in user navigates to the homepage page
    Then the user will be on a page with a heading "Upload your records to start a new transfer"

  Scenario: Homepage page is accessed by a judgment user
    Given A logged in judgment user
    And the logged in user navigates to the homepage page
    Then the user will be on a page with a heading "If this is an update to an existing judgment or decision"

  Scenario: Homepage page is submitted by a judgment user
    Given A logged in judgment user
    And the logged in user navigates to the homepage page
    And the user clicks the continue button
    Then the user should be on the before-uploading page
