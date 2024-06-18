Feature: Descriptive metadata pages

  Scenario: Use CSV upload option to add the additional metadata - full journey
    Given A logged in standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing upload of 2 files
    And the logged in user navigates to the additional metadata entry page
    Then the user will be on a page with the fieldset heading "How would you like to enter record metadata?"
    When the user selects csv for the How would you like to enter record metadata? field
    Then the user clicks the continue button
    And the user selects the file: draft-metadata.csv
    And the user clicks the upload button
    Then the user will be on a page with the title "Checking your metadata"
    And the draft metadata checks completed banner should not be visible
    And the draft metadata checks continue button should be disabled
    And the draft metadata checks completed banner should be visible
    And the draft metadata checks continue button should be enabled
    When the user clicks the continue button
    Then the user will be on a page with the title "Results of your metadata checks"
    And the draft metadata upload status should be "IMPORTED"
    When the user clicks the next button
    Then the user will be on a page with the title "Download and review metadata"
    #And the user clicks the download metadata link
    #And the downloaded metadata csv should be same as draft-metadata.csv

  Scenario: User sees an import error when he uploads the draft metadata csv with invalid values
    Given A logged in standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing upload of 2 files
    And the logged in user navigates to the additional metadata entry page
    Then the user will be on a page with the fieldset heading "How would you like to enter record metadata?"
    When the user selects csv for the How would you like to enter record metadata? field
    Then the user clicks the continue button
    And the user selects the file: invalid-draft-metadata.csv
    And the user clicks the upload button
    Then the user will be on a page with the title "Checking your metadata"
    And the draft metadata checks completed banner should not be visible
    And the draft metadata checks continue button should be disabled
    And the draft metadata checks completed banner should be visible
    And the draft metadata checks continue button should be enabled
    When the user clicks the continue button
    Then the user will be on a page with the title "Results of your metadata checks"
    And the draft metadata upload status should be "ERRORS"

  Scenario: User sees an error when trying to view the draft metadata upload page for a consignment they don't own
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing upload of 2 files
    And a user who did not create the consignment
    When the user who did not create the consignment is logged in on the draft metadata upload page
    Then the user will be on a page with the error message "You are not permitted to see this page"
