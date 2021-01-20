Feature: Transfer Confirmation Page

  Scenario: Transfer confirmation page is accessed by a logged out user
    Given A logged out user
    And the logged out user attempts to access the transfer confirmation page
    Then the logged out user should be on the login page
