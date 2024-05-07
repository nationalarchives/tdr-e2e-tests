Feature: File Checks Page

  Scenario: A user will see the file checks in progress page if the checks are incomplete
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing transfer agreement part 1
    And an existing transfer agreement part 2
    And an existing upload of 5 files
    And 1 of the antivirus scans for the standard transfer have finished
    When the user is logged in on the file checks page
    Then the user will be on a page with the title "Checking your records"
    And the file checks completed banner should not be visible
    And the file checks continue button should be disabled

  Scenario: A user will see the file checks complete page if the checks are complete
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing transfer agreement part 1
    And an existing transfer agreement part 2
    And an existing upload of 10 files
    And 2 of the FFID scans for the standard transfer have finished
    And 5 of the checksum scans for the standard transfer have finished
    And 8 of the antivirus scans for the standard transfer have finished
    When the user is logged in on the file checks page
    Then the user will be on a page with the title "Checking your records"
    And the user waits for the checks to complete
    Then the file checks completed banner should be visible
    And the file checks continue button should be enabled

  Scenario: A user will see the file checks complete notification if the checks are complete and they visit the record checks page
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing transfer agreement part 1
    And an existing transfer agreement part 2
    And the file checks are complete
    When the user is logged in on the file checks page
    And the user will be on a page with the title "Checking your records"
    Then the user will see the message "Your upload and checks have been completed."
    When the user clicks on the Continue button
    Then the user will be on a page with the title "Results of your checks"

  Scenario: Consignment file checks page is accessed by a logged out user
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing transfer agreement part 1
    And an existing transfer agreement part 2
    And an existing upload of 5 files
    And the logged out user attempts to access the file checks page
    Then the logged out user should be on the login page

  Scenario: A judgment user should see the judgments file checks progress page
    Given A logged in judgment user
    And an existing judgment consignment for transferring body MOCK1
    And an existing transfer agreement part 1
    And an existing transfer agreement part 2
    And an existing upload of 1 files
    And 1 of the antivirus scans for the judgment transfer have finished
    When the logged in user navigates to the file checks page
    Then the user will be on a page with the title "Checking your upload"

  Scenario: A judgment user will see the transfer complete page if the checks are complete
    Given A logged in judgment user
    And an existing judgment consignment for transferring body MOCK1
    And an existing transfer agreement part 1
    And an existing transfer agreement part 2
    And an existing upload of 1 files
    When the logged in user navigates to the file checks page
    And 1 of the FFID scans for the judgment transfer have finished
    And 1 of the checksum scans for the judgment transfer have finished
    And 1 of the antivirus scans for the judgment transfer have finished
    Then the user will be on a page with the title "Checking your upload"
    Then the user will be on a page with a panel titled "Transfer complete"
    And the judgment transfer export will be complete
