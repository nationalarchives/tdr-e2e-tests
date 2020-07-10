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
    Then the user will be on a page with the title Records

  @upload
  Scenario: A logged in user tries to upload multiple set of files to a consignment
    Given an existing user
    And an existing consignment for transferring body MOCK1 Department
    And an existing transfer agreement
    And the user is logged in on the upload page
    When the user selects a directory
    And the user clicks the continue button
    Then the page will redirect to the records page after upload is complete
#    Then the user goes back to the consignment upload page
#    When the user selects a directory
#    And the user clicks the continue button
#    Then the user should see a user-specific upload error GraphQL error: Upload already occurred for consignment: {consignmentId}
