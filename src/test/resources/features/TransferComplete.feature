@quick
Feature: Transfer Complete Page

  Scenario: Transfer complete page is accessed by a logged out user
    Given A logged out user
    And an existing consignment for transferring body MOCK1
    And the logged out user attempts to access the transfer complete page
    Then the logged out user should be on the login page
