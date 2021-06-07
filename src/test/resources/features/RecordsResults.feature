@slow
Feature: Record results Page

  Scenario: The user logs in to the file checks results page once file checks are complete
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing transfer agreement
    And the records checks are complete
    When the user is logged in on the records results page
    Then the user will be on a page with a panel titled "Checks Complete"

  Scenario: The user will see an error when trying to access file check results for a consignment they don't own
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing transfer agreement
    And a user who did not create the consignment
    When the user who did not create the consignment is logged in on the records results page
    Then the user who did not create the consignment will see the error message "You are not permitted to see this page"

  Scenario: The user will see an error when trying to access file check results before upload has happened
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing transfer agreement
    When the user is logged in on the records results page
    Then the user should see a general service error "Sorry, there is a problem with the service"

  Scenario: The user will see an error when trying to access file check results when there is a checksum mismatch
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing transfer agreement
    And the checksum check has failed
    When the user is logged in on the records results page
    Then the user will see the error summary "One or more files you uploaded have failed our checks"

  Scenario: The user will see an error when trying to access file check results when there is an antivirus failure
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And an existing transfer agreement
    And the antivirus check has failed
    When the user is logged in on the records results page
    Then the user will see the error summary "One or more files you uploaded have failed our checks"
