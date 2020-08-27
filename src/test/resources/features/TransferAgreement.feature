Feature: Transfer Agreement Page

  Scenario: A logged in user completes the Transfer Agreement form correctly
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And the user is logged in on the Transfer Agreement page
    When the user selects yes to all transfer agreement checks
    And the user confirms that DRO has signed off on the records
    And the user clicks the continue button
    Then the user will be on a page with the title Upload Records

  Scenario: A logged in user submits Transfer Agreement without DRO approval
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And the user is logged in on the Transfer Agreement page
    When the user selects yes to all transfer agreement checks
    And the user does not confirm DRO sign off for the records
    And the user clicks the continue button
    Then the user will see a form error message "DRO must have signed off the appraisal and selection decision for records"

  Scenario: A logged in user submits Transfer Agreement without responding yes to all questions
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And the user is logged in on the Transfer Agreement page
    When the user selects yes for all checks except "The records are all Digital"
    And the user confirms that DRO has signed off on the records
    And the user clicks the continue button
    Then the user will see a form error message "All records must be confirmed as digital before proceeding"