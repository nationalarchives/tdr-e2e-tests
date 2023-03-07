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
    And the logged in user navigates to the add metadata page for closure metadata
    Then the user will be on a page with the caption "Closure metadata"
    And the user will be on a page with the title "Add or edit metadata"
