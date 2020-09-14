Feature: Records Check Page

  Scenario: User sees the file check progress bars
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing transfer agreement
    And the user is logged in on the upload page
    When the user selects directory containing: testfile1
    And the user clicks the continue button
    Then the user should see the AV Metadata progress bar
    And the user should see the checksum progress bar

  Scenario: User is redirected to results page when the record checks are complete
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing transfer agreement
    And the user is logged in on the upload page
    When the user selects directory containing: testfile1
    And the user clicks the continue button
    Then the user will be on a page with the title Record check results