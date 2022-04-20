Feature: Help Page

  Scenario: Help page is accessed by a standard user
    Given A logged in standard user
    And the logged in user navigates to the help page
    Then the user will be on a page with the title "User Help Guide"

  Scenario: Help page is accessed by a judgment user
    Given A logged in judgment user
    And the logged in user navigates to the help page
    Then the user will be on a page with the title "Transferring Judgments to The National Archives"
