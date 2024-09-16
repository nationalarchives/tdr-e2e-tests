Feature: Metadata review pages
  Scenario: Metadata review requested by user is approved by transfer advisor
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
    Given A logged in transfer adviser user
    Then the user will be on a page with the title "Welcome to the Transfer Digital Records service"
    And the user clicks the Transfers for review button
    Then the user will be on a page with the title "Metadata Reviews"
    And the transfer adviser user clicks view request for consignment
    Then the user will be on a page with the label "2. Set the status of this review"
    And the transfer adviser user Approve the metadata
    And the user clicks the Submit button
    Then the transfer adviser user logs out
    Given an existing standard user logs in
    When the logged in user navigates to the review-progress page
    Then the user will see the alert You can now complete your transfer

  Scenario: Metadata review requested by user is rejected by transfer advisor
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
    Given A logged in transfer adviser user
    Then the user will be on a page with the title "Welcome to the Transfer Digital Records service"
    And the user clicks the Transfers for review button
    Then the user will be on a page with the title "Metadata Reviews"
    And the transfer adviser user clicks view request for consignment
    Then the user will be on a page with the label "2. Set the status of this review"
    And the transfer adviser user Reject the metadata
    And the user clicks the Submit button
    Then the transfer adviser user logs out
    Given an existing standard user logs in
    When the logged in user navigates to the review-progress page
    Then the user will see the alert We found issues in your metadata

  Scenario: The metadata viewer should not be able to set the status of a metadata review
    Given A logged in standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing upload of 2 files
    And an existing completed closure form
    And an existing metadata review is in progress
    Then the standard user logs out
    Given A logged in metadata viewer user
    Then the user will be on a page with the title "Welcome to the Transfer Digital Records service"
    And the user clicks the Transfers for review button
    Then the user will be on a page with the title "Metadata Reviews"
    And the metadata viewer user clicks view request for consignment
    Then the user will be on a page with the label "1. Download and review transfer metadata"
    And the label "2. Set the status of this review" should not be visible for the metadata viewer user
