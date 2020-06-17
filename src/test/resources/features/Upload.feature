Feature: Upload
  Scenario: The progress bar is hidden before file upload
    Given A logged in user
    And an existing consignment for transferring body MOCK1 Department
    And an existing transfer agreement
    When the logged in user navigates to the upload page
    Then the progress bar should not be visible

  Scenario: The progress bar is shown after file upload
    Given A logged in user
    And an existing consignment for transferring body MOCK1 Department
    And an existing transfer agreement
    And the upload page is loaded
    When the user uploads a file
    Then the progress bar should be visible

  Scenario: The records page is shown when the upload is completed
    Given A logged in user
    And an existing consignment for transferring body MOCK1 Department
    And an existing transfer agreement
    And the upload page is loaded
    When the user uploads a file
    Then the page will redirect to the records page after upload
