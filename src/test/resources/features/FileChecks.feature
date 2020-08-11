Feature: File Checks Page

  Scenario: A user will see antivirus metadata progress bar on the file checks page
    Given A logged out user
    And an existing consignment for transferring body MOCK1 Department
    And an existing transfer agreement
    And the user is logged in on the upload page
    And the user selects directory containing: testfile1
    When the user clicks the continue button
    Then the user will be on a page with the title "Checking your records"
    And the av metadata progress bar should be visible
