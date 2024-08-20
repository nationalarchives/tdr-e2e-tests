Feature: Metadata review pages

  Scenario: Metadata review requested by user is approved by DTA reviewer
    Given A logged in standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing upload of 2 files
    And an existing completed closure form
    And the logged in user navigates to the Download Metadata page
    When the user clicks the continue button
    Then the user will be on a page with the large heading "Request a metadata review"
    When the user clicks the Submit Metadata for review button
    Then the user will see the alert Your review is in progress
    Then the standard user logs out
    Given A logged in tna user
    Then the user will be on a page with the title "Welcome to the Transfer Digital Records service"
    And the user clicks the Transfers for review button
    Then the user will be on a page with the title "Metadata Reviews"
    And the tna user clicks view request for consignment
    Then the user will be on a page with the label "2. Set the status of this review"
    And the tna user Approve the metadata
    And the user clicks the Submit button
    Then the tna user logs out
    Given an existing standard user logs in
    When the logged in user navigates to the review-progress page
    Then the user will see the alert You can now complete your transfer


  Scenario: Metadata review requested by user is rejected by DTA reviewer
    Given A logged in standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing upload of 2 files
    And an existing completed closure form
    And the logged in user navigates to the Download Metadata page
    When the user clicks the continue button
    Then the user will be on a page with the large heading "Request a metadata review"
    When the user clicks the Submit Metadata for review button
    Then the user will see the alert Your review is in progress
    Then the standard user logs out
    Given A logged in tna user
    Then the user will be on a page with the title "Welcome to the Transfer Digital Records service"
    And the user clicks the Transfers for review button
    Then the user will be on a page with the title "Metadata Reviews"
    And the tna user clicks view request for consignment
    Then the user will be on a page with the label "2. Set the status of this review"
    And the tna user Reject the metadata
    And the user clicks the Submit button
    Then the tna user logs out
    Given an existing standard user logs in
    When the logged in user navigates to the review-progress page
    Then the user will see the alert We found issues in your metadata
