Feature: Series Page

  Scenario: Logged in user selects nothing from dropdown
    Given A logged in user
    When the logged in user navigates to the series page
    And the user clicks the .govuk-button element
    Then the logged in user should stay at the series page
    And the user will see a form error message This field is required

  Scenario: Logged in user selects a series from the dropdown
    Given A logged in user
    When the logged in user navigates to the series page
    And the user selects the series MOCK1 123
    And the user clicks the continue button
    Then the user should be at the transfer-agreement page

  Scenario: Logged in user selects 'back' when on Series page
    Given A logged in user
    When the logged in user navigates to the series page
    And the user clicks the .govuk-back-link element
    Then the user should be at the dashboard page

  Scenario: User from MOCK1 Department transferring body sees the correct series choices
   Given A logged in user who is a member of MOCK1 Department transferring body
   When the logged in user navigates to the series page
   Then the user should see the series dropdown values MOCK1 123

  Scenario: User from MOCK2 Department transferring body sees the correct series choices
    Given A logged in user who is a member of MOCK2 Department transferring body
    When the logged in user navigates to the series page
    Then the user should see the series dropdown values MOCK2 234,MOCK2 345

  Scenario: User with no transferring body set sees an error message
    Given A logged in user who is not a member of a transferring body
    When the logged in user navigates to the series page
    Then the user should see a user specific general error Transferring body missing from token for user {userId}

  Scenario: User from MOCK4 Department should see an empty series list
    Given A logged in user who is a member of MOCK4 Department transferring body
    When the logged in user navigates to the series page
    Then the user should see an empty series dropdown
