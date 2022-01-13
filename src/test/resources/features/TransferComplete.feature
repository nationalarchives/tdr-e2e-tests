Feature: Transfer Complete Page

  Scenario: Transfer complete page is accessed by a logged out user
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And the logged out user attempts to access the transfer complete page
    Then the logged out user should be on the login page

  Scenario: A logged in user should see the Transfer complete page
    Given A logged in user
    And an existing standard consignment for transferring body MOCK1
    When the logged in user navigates to the transfer complete page
    Then the user will be on a page with a panel titled "Transfer complete"
