Feature: Descriptive metadata pages

  Scenario: Descriptive metadata file selection page is accessed by a standard user
    Given A logged in standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing upload of 2 files
    And the logged in user navigates to the files selection page for descriptive metadata
    Then the user will be on the "Descriptive metadata" "Choose a file" page

  Scenario: Descriptive metadata form page is accessed by a standard user
    Given A logged in standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing upload of 2 files
    And the logged in user navigates to the add metadata page for descriptive metadata
    Then the user will be on the "Descriptive metadata" "Add or edit descriptive metadata" page

  Scenario: User sees an error when trying to view the descriptive metadata form page for a consignment they don't own
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing upload of 2 files
    And a user who did not create the consignment
    When the user who did not create the consignment is logged in on the add metadata page for descriptive metadata
    Then the user will be on a page with the error message "You are not permitted to see this page"

  Scenario: Descriptive metadata form page is accessed by a judgment user
    Given A logged in judgment user
    And an existing judgment consignment for transferring body MOCK1
    And an existing upload of 2 files
    When the logged in user navigates to the add metadata page for descriptive metadata
    Then the user will be on a page with the error message "You are not permitted to see this page"

  Scenario: Descriptive metadata form is partially completed by a standard user
    Given A logged in standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing upload of 2 files
    And the logged in user navigates to the add metadata page for descriptive metadata
    Then the user will be on the "Descriptive metadata" "Add or edit descriptive metadata" page
    And the user de-selects "English" for the Language field
    When the user clicks the Save and Review button
    Then the user will see a form error message "Select at least one Language"

  Scenario: Standard user completes all fields on descriptive metadata form
    Given A logged in standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing upload of 2 files
    And the logged in user navigates to the add metadata page for descriptive metadata
    Then the user will be on the "Descriptive metadata" "Add or edit descriptive metadata" page
    When the user enters some description for the description field
    And the user enters 07/03/2023 for the date of the record field
    And the user enters translated title for the translated title field
    And the user enters some reference for the former reference field
    When the user clicks the Save and Review button
    Then the user will be on a page with the title "Review saved changes"

  Scenario: Existing descriptive metadata for a file is deleted by a standard user
    Given A logged in standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing upload of 2 files
    And an existing completed descriptive form
    And the logged in user navigates to the view metadata page for descriptive metadata
    Then the user will be on the "Descriptive metadata" "View Metadata" page
    And existing metadata should contain 4 values
    And existing metadata should contain the metadata Name with value path0
    And existing metadata should contain the metadata Description with value test description
    And existing metadata should contain the metadata Date of the record with value 08/03/2023
    And existing metadata should contain the metadata Language with value English
    When the user clicks the Delete metadata link
    Then the user will be on a page with the large heading "Delete descriptive metadata"
    When the user clicks the Delete and return to files button
    Then the user will be on a page with the title "Choose a file"
    When the logged in user navigates to the view metadata page for descriptive metadata
    Then the user will be on the "Descriptive metadata" "View Metadata" page
    And existing metadata should contain 2 values
    And existing metadata should contain the metadata Name with value path0
    And existing metadata should contain the metadata Language with value English

  Scenario: The user will see an error when trying to view descriptive metadata for a consignment they don't own
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing upload of 2 files
    And an existing completed descriptive form
    And a user who did not create the consignment
    When the user who did not create the consignment is logged in on the view metadata page for descriptive metadata
    Then the user will be on a page with the error message "You are not permitted to see this page"

  Scenario: View descriptive metadata page is accessed by a judgment user
    Given A logged in judgment user
    And an existing judgment consignment for transferring body MOCK1
    And an existing upload of 2 files
    And an existing completed descriptive form
    When the logged in user navigates to the view metadata page for descriptive metadata
    Then the user will be on a page with the error message "You are not permitted to see this page"
