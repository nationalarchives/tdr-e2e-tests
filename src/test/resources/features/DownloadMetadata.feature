Feature: Download Metadata page

  Scenario: Download metadata page contains an image, a link and a continue button
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And the user is logged in on the Download Metadata page
    Then the download metadata page elements are loaded

  Scenario: Download metadata page is accessed by a logged out user
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And the logged out user attempts to access the Download Metadata page
    Then the logged out user should be on the login page

  Scenario: Download metadata page is accessed by a user who did not create the consignment
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And a user who did not create the consignment
    When the user who did not create the consignment is logged in on the Download Metadata page
    Then the user will be on a page with the error message "You are not permitted to see this page"

  Scenario: Download metadata page is accessed by a judgment user
    Given A logged in judgment user
    And an existing judgment consignment for transferring body MOCK1
    When the logged in user navigates to the Download Metadata page
    Then the user should see a general service error "Page not found"

  @wip
  Scenario: Metadata CSV is downloaded by a logged in user
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And the user is logged in on the Download Metadata page
    And the file checks are complete
    And the user has created additional metadata
    And the user clicks the download metadata link
    Then the metadata csv will have the correct columns for 2 files

  Scenario: The transfer adviser should be able to download the metadata excel
    Given A logged in standard user
    And an existing standard consignment for transferring body MOCK1
    When the logged in user navigates to the Download Metadata page
    And the file checks are complete
    And the user has created additional metadata
    And an existing metadata review is in progress
    Then the standard user logs out
    Given A logged in transfer adviser user
    When the logged in user navigates to the metadata review page
    Then the user will be on a page with the label "1. Download and review transfer metadata"
    And the user clicks the download metadata link

  Scenario: The metadata viewer should be able to download the metadata excel
    Given A logged in standard user
    And an existing standard consignment for transferring body MOCK1
    When the logged in user navigates to the Download Metadata page
    And the file checks are complete
    And the user has created additional metadata
    And an existing metadata review is in progress
    Then the standard user logs out
    Given A logged in metadata viewer user
    When the logged in user navigates to the metadata review page
    Then the user will be on a page with the label "1. Download and review transfer metadata"
    And the user clicks the download metadata link
