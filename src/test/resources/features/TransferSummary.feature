Feature: Transfer Summary Page
  Scenario: Submitting the completed transfer summary page creates a completed export
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing upload of 3 files
    And 3 of the antivirus scans have finished
    And 3 of the FFID scans have finished
    And 3 of the checksum scans have finished
    And the user is logged in on the Transfer Summary page
    Then the user will be on a page with the title "Transfer Summary"
    When the user selects yes to all transfer summary checks
    And the user clicks the continue button
    Then the user will be on a page with the title "Transfer Complete"
    And the transfer export will be complete
