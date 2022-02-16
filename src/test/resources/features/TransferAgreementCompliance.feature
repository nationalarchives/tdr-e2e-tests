@wip
Feature: Transfer Agreement Page

  Scenario: A logged in user completes the Transfer Agreement form correctly
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing not-compliance transfer agreement
    And the user is logged in on the Transfer Agreement2 page
    When the user confirms all the records are open
    And the user confirms that DRO has signed off on the records
    And the user clicks the continue button
    Then the user will be on a page with the title "Upload your records"

  Scenario: A logged in user submits Transfer Agreement without DRO approval
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing not-compliance transfer agreement
    And the user is logged in on the Transfer Agreement2 page
    And the user confirms all the records are open
    And the user does not confirm DRO sign off for the records
    And the user clicks the continue button
    Then the user will see a form error message "Departmental Records Officer (DRO) must have signed off the appraisal and selection decision for records"
    Then the user will see a summary error message "Departmental Records Officer (DRO) must have signed off the appraisal and selection decision for records"

  Scenario: Consignment transfer agreement page is accessed by a logged out user
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing not-compliance transfer agreement
    And the logged out user attempts to access the Transfer Agreement2 page
    Then the logged out user should be on the login page

  Scenario: Consignment transfer agreement page is accessed by a user who did not create the consignment
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing not-compliance transfer agreement
    And a user who did not create the consignment
    When the user who did not create the consignment is logged in on the Transfer Agreement2 page
    Then the user who did not create the consignment will see the error message "You are not permitted to see this page"
