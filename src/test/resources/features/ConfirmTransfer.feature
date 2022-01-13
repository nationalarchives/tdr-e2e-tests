Feature: Confirm Transfer Page

  Scenario: Consignment confirm transfer page is accessed by a logged out user
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing transfer agreement
    And the logged out user attempts to access the Confirm Transfer page
    Then the logged out user should be on the login page

  Scenario: Consignment confirm transfer page is accessed by a user who did not create the consignment
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing transfer agreement
    And a user who did not create the consignment
    When the user who did not create the consignment is logged in on the Confirm Transfer page
    Then the user who did not create the consignment will see the error message "You are not permitted to see this page"

  Scenario: Consignment confirm transfer page shows the same number of files as were uploaded
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing upload of 3 files
    And the user is logged in on the Confirm Transfer page
    When the user will be on a page with the title "Confirm transfer"
    Then the confirm transfer page shows the user that 3 files have been uploaded

  Scenario: Confirm transfer will show all summary information
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing upload of 4 files
    And the user is logged in on the Confirm Transfer page
    When the user will be on a page with the title "Confirm transfer"
    Then the confirm transfer page shows the user that 4 files have been uploaded
    And the user sees a transfer confirmation with related information

  Scenario: A logged in user submits Final Transfer Confirmation form without confirming anything
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And the user is logged in on the Confirm Transfer page
    When the user clicks the continue button
    Then the user will see all of the Final Transfer Confirmation form's error messages

  Scenario: Submitting the Final Transfer Confirmation form creates a completed export
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing upload of 3 files
    And 3 of the antivirus scans have finished
    And 3 of the FFID scans have finished
    And 3 of the checksum scans have finished
    And the user is logged in on the Confirm Transfer page
    Then the user will be on a page with the title "Confirm transfer"
    When the user confirms all the records are open
    And the user confirms that they are transferring legal ownership of the records to TNA
    And the user clicks the continue button
    Then the user will be on a page with a panel titled "Transfer complete"
    And the standard transfer export will be complete
