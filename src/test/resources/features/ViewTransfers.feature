Feature: View Transfers

  Scenario: The user's consignment is visible on the View Transfers Page when accessed by a standard user
    Given A logged in standard user
    And an existing standard consignment for transferring body MOCK1
    When the logged in user navigates to the View Transfers page
    Then the user will see a row with a consignment reference that correlates with their consignmentId
    When the user clicks on the Back link
    Then the user should be on the homepage page

  Scenario: Show a link to start a new transfer on the View Transfers Page when a new user visits the page
    Given A logged in standard user
    When the logged in user navigates to the View Transfers page
    When the user clicks the Start a new transfer button
    Then the user should be on the series page

  Scenario: View Transfers page is accessed by a judgment user
    Given A logged in judgment user
    And an existing judgment consignment for transferring body MOCK1
    When the logged in user navigates to the View Transfers page
    Then the user will be on a page with the error message "You are not permitted to see this page"
