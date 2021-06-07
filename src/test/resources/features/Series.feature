@medium
Feature: Series Page

  Scenario: Logged in user selects nothing from dropdown
    Given A logged in user
    When the logged in user navigates to the series page
    And the user clicks the continue button
    Then the user will remain on the series page
    And the user will see a form error message "Select a series reference"
    And the user will see a summary error message "Select a series reference"

  Scenario: Logged in user selects a series from the dropdown
    Given A logged in user
    When the logged in user navigates to the series page
    And the user selects the series MOCK1 123
    And the user clicks the continue button
    Then the user should be on the transfer-agreement page

  Scenario: User from MOCK1 transferring body sees the correct series choices
   Given A logged in user who is a member of MOCK1 transferring body
   When the logged in user navigates to the series page
   Then the user should see the series dropdown values MOCK1 123

  Scenario: User from MOCK2 transferring body sees the correct series choices
    Given A logged in user who is a member of MOCK2 transferring body
    When the logged in user navigates to the series page
    Then the user should see the series dropdown values MOCK2 234,MOCK2 345

  Scenario: User with no transferring body set sees an error message
    Given A logged in user who is not a member of a transferring body
    When the logged in user navigates to the series page
    Then the user should see a general service error "Sorry, there is a problem with the service"

  Scenario: User from MOCK4 should see an empty series list
    Given A logged in user who is a member of MOCK4 transferring body
    When the logged in user navigates to the series page
    Then the user should see an empty series dropdown
