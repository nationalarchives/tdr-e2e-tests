Feature: Before you upload

  Scenario: A logged in user completes the Transfer Agreement form correctly
    Given A logged out judgment user
    And an existing judgment consignment for transferring body MOCK1
    When the user is logged in on the Before Uploading page
    And the user clicks the continue button
    Then the user will be on a page with the title "Upload your records"

