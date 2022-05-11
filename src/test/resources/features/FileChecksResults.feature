Feature: File Checks results Page

  Scenario: The user will see a success message when they access the file checks results page once file checks are complete
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing private beta transfer agreement
    And an existing compliance transfer agreement
    And the file checks are complete
    And the user is logged in on the file checks results page
    And the user will be on a page with the title "Results of your checks"
    And the user should see a banner titled Success
    When the user clicks on the Continue button
    Then the user will be on a page with the title "Confirm transfer"

  Scenario: The user will see an error when trying to access file check results for a consignment they don't own
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing private beta transfer agreement
    And an existing compliance transfer agreement
    And a user who did not create the consignment
    When the user who did not create the consignment is logged in on the file checks results page
    Then the user who did not create the consignment will see the error message "You are not permitted to see this page"

  Scenario: The user will see an error when trying to access file check results before upload has happened
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing private beta transfer agreement
    And an existing compliance transfer agreement
    When the user is logged in on the file checks results page
    Then the user should see a general service error "Sorry, there is a problem with the service"

  Scenario: The user will see an error when trying to access file check results when there is a checksum mismatch
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing private beta transfer agreement
    And an existing compliance transfer agreement
    And the checksum check has failed
    When the user is logged in on the file checks results page
    Then the user will see the error summary "One or more files you uploaded have failed our checks"

  Scenario: The user will see an error when trying to access file check results when there is an antivirus failure
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing private beta transfer agreement
    And an existing compliance transfer agreement
    And the antivirus check has failed
    When the user is logged in on the file checks results page
    Then the user will see the error summary "One or more files you uploaded have failed our checks"

  Scenario: The user will see an error when there is a FFID password protected failure
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing private beta transfer agreement
    And an existing compliance transfer agreement
    And the FFID "password protected" check has failed
    When the user is logged in on the file checks results page
    Then the user will see the error summary "We cannot accept password protected files. Once removed or replaced, try uploading your folder again."

  Scenario: The user will see an error when there is a FFID zip file failure
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing private beta transfer agreement
    And an existing compliance transfer agreement
    And the FFID "zip file" check has failed
    When the user is logged in on the file checks results page
    Then the user will see the error summary "We cannot accept zip files and similar archival package file formats."

  Scenario: Results page is accessed by a logged out judgment user
    Given A logged out judgment user
    And an existing judgment consignment for transferring body MOCK1
    And the logged out user attempts to access the file checks results page
    Then the logged out user should be on the login page

  Scenario: A judgment user will see an error when trying to access file check results for a judgment they don't own
    Given A logged out judgment user
    And an existing judgment consignment for transferring body MOCK1
    And a user who did not create the consignment
    When the user who did not create the consignment is logged in on the file checks results page
    Then the user who did not create the consignment will see the error message "You are not permitted to see this page"

  Scenario: Submitting the Final Transfer Confirmation creates a completed export for a judgment transfer
    Given A logged out judgment user
    And an existing judgment consignment for transferring body MOCK1
    And an existing upload of 1 files
    And 1 of the antivirus scans for the judgment transfer have finished
    And 1 of the FFID scans for the judgment transfer have finished
    And 1 of the checksum scans for the judgment transfer have finished
    And the user is logged in on the file checks results page
    Then the user will be on a page with the title "Results of checks"
    When the user clicks the continue button
    Then the user will be on a page with a panel titled "Transfer complete"
    And the judgment transfer export will be complete
