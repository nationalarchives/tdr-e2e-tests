Feature: Transfer Agreement Continued Page

  Scenario: A logged in user completes the Transfer Agreement compliance form correctly
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an selected series MOCK1
    And an existing private beta transfer agreement
    And the user is logged in on the Transfer Agreement Continued page
    When the user selects yes to all transfer agreement continued checks
    And the user clicks the continue button
    Then the user will be on a page with the title "Upload your records"

  Scenario: A logged in user submits Transfer Agreement compliance form without DRO approval
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an selected series MOCK1
    And an existing private beta transfer agreement
    And the user is logged in on the Transfer Agreement Continued page
    And the user selects yes to all transfer agreement continued checks
    And the user does not confirm DRO sign off for the records
    And the user clicks the continue button
    Then the user will see a form error message "Departmental Records Officer (DRO) must have signed off the appraisal and selection decision for records"
    Then the user will see a summary error message "Departmental Records Officer (DRO) must have signed off the appraisal and selection decision for records"

  Scenario: Consignment transfer agreement page is accessed by a logged out user
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing private beta transfer agreement
    And the logged out user attempts to access the Transfer Agreement Continued page
    Then the logged out user should be on the login page

  Scenario: Consignment transfer agreement page is accessed by a user who did not create the consignment
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing private beta transfer agreement
    And a user who did not create the consignment
    When the user who did not create the consignment is logged in on the Transfer Agreement Continued page
    Then the user who did not create the consignment will see the error message "You are not permitted to see this page"
