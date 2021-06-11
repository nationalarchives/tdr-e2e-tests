Feature: File Checks Page

  Scenario: A user will see the file checks in progress page if the checks are incomplete
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing transfer agreement
    And an existing upload of 5 files
    And 1 of the antivirus scans have finished
    When the user is logged in on the records page
    Then the user will be on a page with the title "Checking records"
    And the file checks completed banner should not be visible
    And the file checks continue button should be disabled

  Scenario: A user will see the file checks complete page if the checks are complete
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
    Then the file checks completed banner should be visible
    And the file checks continue button should be enabled

  Scenario: A user will see the file checks complete notification if the checks are complete and they visit the record checks page
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing transfer agreement
    And the records checks are complete
    When the user is logged in on the records page
    Then the file checks completed banner should be visible
    And the file checks continue button should be enabled

  Scenario: Consignment records page is accessed by a logged out user
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing transfer agreement
    And an existing upload of 5 files
    And the logged out user attempts to access the records page
    Then the logged out user should be on the login page
