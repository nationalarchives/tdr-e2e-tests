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
