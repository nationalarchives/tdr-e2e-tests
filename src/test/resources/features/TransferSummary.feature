Feature: Transfer Summary Page

  Scenario: Consignment transfer summary page is accessed by a logged out user
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing transfer agreement
    And the logged out user attempts to access the Transfer Summary page
    Then the logged out user should be on the login page

  Scenario: Consignment transfer summary page is accessed by a user who did not create the consignment
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing transfer agreement
    And a user who did not create the consignment
    When the user who did not create the consignment is logged in on the Transfer Summary page
    Then the user who did not create the consignment will see the error message "You are not permitted to see this page"

  Scenario: Consignment transfer summary page shows the same number of files as were uploaded
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing upload of 3 files
    And the user is logged in on the Transfer summary page
    When the user will be on a page with the title "Transfer summary"
    Then the transfer summary page shows the user that 3 files have been uploaded

  Scenario: A logged in user submits Final Transfer Confirmation form without confirming anything
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And the user is logged in on the Transfer Summary page
    When the user clicks the continue button
    Then the user will see all of the Final Transfer Confirmation form's error messages

  Scenario: Submitting the Final Transfer Confirmation form creates a completed export
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing upload of 3 files
    And 3 of the antivirus scans have finished
    And 3 of the FFID scans have finished
    And 3 of the checksum scans have finished
    And the user is logged in on the Transfer Summary page
    Then the user will be on a page with the title "Transfer summary"
    When the user confirms all the records are open
    And the user confirms that they are transferring legal ownership of the records to TNA
    And the user clicks the continue button
    Then the user will be on a page with a panel titled "Transfer Complete"
    And the transfer export will be complete
