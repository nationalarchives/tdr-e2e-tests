Feature: View Transfers

  Scenario: The user's consignment is visible on the View Transfers Page when accessed by a standard user
    Given A logged in standard user
    And an existing standard consignment for transferring body MOCK1
    When the logged in user navigates to the View Transfers page
    Then the user will see a row with a consignment reference that correlates with their consignmentId
    When the user clicks on the Back to homepage button
    Then the user should be on the homepage page

#  Uncomment once judgment users can access the view the transfers page
#  Scenario: The user's consignment is visible on the View Transfers Page when accessed by a judgment user
#    Given A logged in judgment user
#    And an existing judgment consignment for transferring body MOCK1
#    When the logged in user navigates to the View Transfers page
#    Then the user will see a row with a consignment reference that correlates with their consignmentId
#    When the user clicks on the Back to homepage button
#    Then the user should be on the homepage page
