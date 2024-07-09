Feature: File Checks results Page
  Scenario: The user will see a success message when they access the file checks results page once file checks are complete
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing transfer agreement part 1
    And an existing transfer agreement part 2
    And the file checks are complete
    And the user is logged in on the file checks results page
    And the user will be on a page with the title "Results of your checks"
    And the user should see a banner titled Success
    When the user clicks on the Next button
    Then the user will be on a page with the fieldset heading "How would you like to enter record metadata?"

  Scenario: The user will see an error when trying to access file check results for a consignment they don't own
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing transfer agreement part 1
    And an existing transfer agreement part 2
    And a user who did not create the consignment
    When the user who did not create the consignment is logged in on the file checks results page
    Then the user will be on a page with the error message "You are not permitted to see this page"

  Scenario: The user will see an error when trying to access file check results before upload has happened
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing transfer agreement part 1
    And an existing transfer agreement part 2
    When the user is logged in on the file checks results page
    Then the user should see a general service error "Sorry, there is a problem with the service"

  Scenario: The user will see an error when trying to access file check results when there is a checksum mismatch
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing transfer agreement part 1
    And an existing transfer agreement part 2
    And the antivirus check has succeeded
    And the FFID check has succeeded
    And the checksum check has failed
    When the user is logged in on the file checks results page
    Then the user will see the error summary "One or more files you uploaded have failed our checks"

  Scenario: The user will see an error when trying to access file check results when there is an antivirus failure
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing transfer agreement part 1
    And an existing transfer agreement part 2
    And the antivirus check has failed
    And the checksum check has succeeded
    And the FFID check has succeeded
    When the user is logged in on the file checks results page
    Then the user will see the error summary "One or more files you uploaded have failed our checks"

  Scenario: The user will see an error when there is a FFID password protected failure
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing transfer agreement part 1
    And an existing transfer agreement part 2
    And the antivirus check has succeeded
    And the checksum check has succeeded
    And the FFID "password protected" check has failed
    When the user is logged in on the file checks results page
    Then the user will see the error summary "We cannot accept password protected files. Once removed or replaced, try uploading your folder again."

  Scenario: The user will see an error when there is a FFID zip file failure
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing transfer agreement part 1
    And an existing transfer agreement part 2
    And the antivirus check has succeeded
    And the checksum check has succeeded
    And the FFID "zip file" check has failed
    When the user is logged in on the file checks results page
    Then the user will see the error summary "We cannot accept zip files and similar archival package file formats."

  Scenario: File checks result page should not be accessible for judgment users by URL
    Given A logged in judgment user
    When the logged in user navigates to the file checks results page
    Then the user should see a general service error "Page not found"
