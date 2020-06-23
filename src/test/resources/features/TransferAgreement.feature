Feature: Transfer Agreement Page

  Scenario: A logged in user completes the Transfer Agreement form correctly
    Given an existing user
    And an existing consignment for transferring body MOCK1 Department
    And the user is logged in on the Transfer Agreement page
    When the user selects yes to all transfer agreement checks
    And the user confirms that DRO has signed off on the records
    And the user clicks the continue button
    Then the user will be on a page with the title Upload Records

    