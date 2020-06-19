Feature: Upload
  Scenario: The progress bar is hidden before file upload
    Given an existing user
    And an existing consignment for transferring body MOCK1 Department
    And an existing transfer agreement
    And the user is logged in on the upload page
    Then the progress bar should not be visible

  Scenario: The progress bar is shown after file upload
    Given an existing user
    And an existing consignment for transferring body MOCK1 Department
    And an existing transfer agreement
    And the user is logged in on the upload page
    When the user uploads a file
    Then the progress bar should be visible

  Scenario: The records page is shown when the upload is completed
    Given an existing user
    Given an existing consignment for transferring body MOCK1 Department
    And an existing transfer agreement
    And the user is logged in on the upload page
    When the user uploads a file
    Then the page will redirect to the records page after upload is complete
