Feature: File Checks Page

  Scenario: A user will see antivirus metadata progress bar on the file checks page
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing transfer agreement
    And an existing upload
    When the user is logged in on the records page
    Then the user will be on a page with the title "Checking your records"
    And the av metadata progress bar should be visible
    And the av metadata progress bar should have 75% progress

  Scenario: A user will see checksum progress bar on the file checks page
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing transfer agreement
    And an existing upload
    When the user is logged in on the records page
    Then the user will be on a page with the title "Checking your records"
    And the checksum progress bar should be visible
    And the checksum progress bar should have 25% progress

#    This scenario still goes through the upload manually rather than using the helper methods so that the redirect can be tested
  Scenario: User is redirected to results page when the record checks are complete
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing transfer agreement
    And the user is logged in on the upload page
    When the user selects directory containing: testfile1
    And the user clicks the continue button
    Then the user will be on a page with the title "Record check results"

  Scenario: User is redirected to the results page if the checks are complete and they visit the record checks page
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing transfer agreement
    And the records checks are complete
    When the user is logged in on the records page
    Then the user will be on a page with the title "Record check results"
