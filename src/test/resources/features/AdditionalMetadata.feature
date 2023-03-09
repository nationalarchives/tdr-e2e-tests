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

  Scenario: Closure metadata form is partially completed by a standard user
    Given A logged in standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing upload of 2 files
    And the logged in user navigates to the add metadata page for closure metadata
    Then the user will be on a page with the caption "Closure metadata"
    When the user enters 01/01/2023 for the FOI decision asserted field
    And the user clicks the Save and Review button
    Then the user will see a form error message "Enter the closure start date for this record"

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

  Scenario: Descriptive metadata is deleted by a standard user
    Given A logged in standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing upload of 2 files
    And an existing completed descriptive form
    And the logged in user navigates to the view metadata page for descriptive metadata
    Then the user will be on a page with the caption "Descriptive metadata"
    And the user will be on a page with the title "View existing metadata"
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
    Then the user will be on a page with the caption "Descriptive metadata"
    And existing metadata should contain 2 values
    And existing metadata should contain the metadata Name with value path0
    And existing metadata should contain the metadata Language with value English

  Scenario: Closure metadata is deleted by a standard user
    Given A logged in standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing upload of 2 files
    And an existing completed closure form
    And the logged in user navigates to the view metadata page for closure metadata
    Then the user will be on a page with the caption "Closure metadata"
    And the user will be on a page with the title "View metadata"
    And existing metadata should contain 8 values
    And existing metadata should contain the metadata Name with value path0
    And existing metadata should contain the metadata FOI decision asserted with value 01/01/2000
    And existing metadata should contain the metadata Closure Start Date with value 01/01/2000
    And existing metadata should contain the metadata Closure status with value Open
    And existing metadata should contain the metadata Closure Period with value 5 years
    And existing metadata should contain the metadata FOI exemption code with value 27(1)
    And existing metadata should contain the metadata Is the title sensitive for the public? with value No, this title can be made public
    And existing metadata should contain the metadata Is the description sensitive for the public? with value No, this description can be made public
    When the user clicks the Delete metadata link
    Then the user will be on a page with the large heading "Delete closure metadata"
    When the user clicks the Delete and return to files button
    Then the user will be on a page with the title "Choose a file"
    When the logged in user navigates to the view metadata page for closure metadata
    Then the user will be on a page with the caption "Closure metadata"
    And existing metadata should contain 4 values
    And existing metadata should contain the metadata Name with value path0
    And existing metadata should contain the metadata Closure status with value Open
    And existing metadata should contain the metadata Is the title sensitive for the public? with value No, this title can be made public
    And existing metadata should contain the metadata Is the description sensitive for the public? with value No, this description can be made public