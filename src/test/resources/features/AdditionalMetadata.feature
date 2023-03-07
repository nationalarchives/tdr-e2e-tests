Feature: Additional Metadata Pages

  Scenario: Additional metadata page is accessed by a standard user
    Given A logged in standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing upload of 2 files
    And the logged in user navigates to the additional metadata page
    Then the user will be on a page with the title "Descriptive & closure metadata"

  Scenario: File selection page is accessed by a standard user for descriptive metadata
    Given A logged in standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing upload of 2 files
    And the logged in user navigates to the files selection page for descriptive metadata
    Then the user will be on a page with the caption "Descriptive metadata"

  Scenario: File selection page is accessed by a standard user for closure metadata
    Given A logged in standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing upload of 2 files
    And the logged in user navigates to the files selection page for closure metadata
    Then the user will be on a page with the caption "Closure metadata"

  Scenario: Descriptive metadata form is accessed by a standard user
    Given A logged in standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing upload of 2 files
    And the logged in user navigates to the add metadata page for descriptive metadata
    Then the user will be on a page with the caption "Descriptive metadata"
    And the user will be on a page with the title "Add or edit metadata"

  Scenario: Closure metadata form is accessed by a standard user
    Given A logged in standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing upload of 2 files
    And the logged in user navigates to the confirm closure status page for closure metadata
    Then the user will be on a page with the title "Confirm closure status"
    When the user confirms that the closure status has been approved by the advisory council
    And the user clicks the Continue button
    Then the user will be on a page with the title "Add or edit metadata"

  Scenario: Descriptive metadata form is partially completed by a standard user
    Given A logged in standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing upload of 2 files
    And the logged in user navigates to the add metadata page for descriptive metadata
    Then the user will be on a page with the caption "Descriptive metadata"
    When the user clicks the Save and Review button
    Then the user will see a form error message "Enter the date of the record for this record"

  Scenario: Descriptive metadata form is fully completed by a standard user
    Given A logged in standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing upload of 2 files
    And the logged in user navigates to the add metadata page for descriptive metadata
    Then the user will be on a page with the caption "Descriptive metadata"
    And the user will be on a page with the title "Add or edit metadata"
    When the user enters some description for the description field
    And the user enters 07/03/2023 for the date of the record field
    When the user clicks the Save and Review button
    Then the user will be on a page with the title "Review saved changes"

  Scenario: Closure metadata form is fully completed by a standard user
    Given A logged in standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing upload of 2 files
    And the logged in user navigates to the add metadata page for closure metadata
    Then the user will be on a page with the caption "Closure metadata"
    And the user will be on a page with the title "Add or edit metadata"
    When the user enters 01/01/2023 for the FOI decision asserted field
    And the user enters 07/03/2023 for the closure start date field
    And the user enters 5 for the closure period field
    And the user selects "27(1)" for the FOI exemption code field
    When the user clicks the Save and Review button
    Then the user will be on a page with the title "Review saved changes"
