Feature: File Checks Page

  Scenario: A user will see antivirus metadata progress bar on the file checks page
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing transfer agreement
    And an existing upload of 5 files
    And 1 of the antivirus scans have finished
    When the user is logged in on the records page
    Then the user will be on a page with the title "Checking records"
    And the av metadata progress bar should be visible
    And the av metadata progress bar should have 20% progress

  Scenario: A user will see checksum progress bar on the file checks page
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing transfer agreement
    And an existing upload of 4 files
    And 3 of the checksum scans have finished
    When the user is logged in on the records page
    Then the user will be on a page with the title "Checking records"
    And the checksum progress bar should be visible
    And the checksum progress bar should have 75% progress

  Scenario: A user will see FFID progress bar on the file checks page
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing transfer agreement
    And an existing upload of 8 files
    And 2 of the FFID scans have finished
    When the user is logged in on the records page
    Then the user will be on a page with the title "Checking records"
    And the ffid progress bar should be visible
    And the ffid progress bar should have 25% progress

  Scenario: User is redirected to results page when the record checks are complete
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing transfer agreement
    And an existing upload of 10 files
    And 2 of the FFID scans have finished
    And 5 of the checksum scans have finished
    And 8 of the antivirus scans have finished
    When the user is logged in on the records page
    Then the user will be on a page with the title "Checking records"
    And the user waits for the checks to complete
    Then the user will be on a page with a panel titled "Checks Complete"

  Scenario: User is redirected to the results page if the checks are complete and they visit the record checks page
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing transfer agreement
    And the records checks are complete
    When the user is logged in on the records page
    Then the user will be on a page with a panel titled "Checks Complete"

  Scenario: Consignment records page is accessed by a logged out user
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing transfer agreement
    And an existing upload of 5 files
    And the logged out user attempts to access the records page
    Then the logged out user should be on the login page

  Scenario: The user will see an error when there is a checksum mismatch
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing transfer agreement
    And the checksum check has failed
    When the user is logged in on the records page
    Then the user will see the error summary One or more files you uploaded have failed our checks
