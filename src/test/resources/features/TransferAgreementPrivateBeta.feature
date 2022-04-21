Feature: Transfer Agreement Page

  Scenario: A logged in user completes the Transfer Agreement private beta form correctly
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing series MOCK1
    And the user is logged in on the Transfer Agreement page
    When the user selects yes to all transfer agreement checks
    And the user clicks the continue button
    Then the user will be on a page with the title "Transfer agreement continued"

  Scenario: A logged in user submits Transfer Agreement private beta form without responding yes to all questions
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing series MOCK1
    And the user is logged in on the Transfer Agreement page
    When the user selects yes for all checks except "The records are all English"
    And the user clicks the continue button
    Then the user will see a form error message "All records must be confirmed as English language before proceeding"
    Then the user will see a summary error message "All records must be confirmed as English language before proceeding"

  Scenario: Consignment transfer agreement page is accessed by a logged out user
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And the logged out user attempts to access the Transfer Agreement page
    Then the logged out user should be on the login page

  Scenario: Consignment transfer agreement page is accessed by a user who did not create the consignment
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And a user who did not create the consignment
    When the user who did not create the consignment is logged in on the Transfer Agreement page
    Then the user who did not create the consignment will see the error message "You are not permitted to see this page"
