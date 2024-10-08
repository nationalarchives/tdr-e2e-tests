#Feature will not work with chromedriver in headless mode due to following bug: https://bugs.chromium.org/p/chromedriver/issues/detail?id=2521&q=directory%20upload&colspec=ID%20Status%20Pri%20Owner%20Summary
Feature: Standard Full user journey
  Scenario: Full transfer standard workflow
    Given A logged out standard user
    When the user navigates to TDR Start Page
    And the user clicks on the Start now button
    And the logged out user enters valid credentials
    And the user clicks the continue button
    Then the user should be on the homepage page
    When the user clicks the continue button
    Then the user should be on a page with series and a consignmentId in the URL
    When the user selects the series MOCK1 123
    And the user clicks the continue button
    Then the user should be on the transfer-agreement page
    When the user selects yes to all transfer agreement part 1 checks
    And the user clicks the Agree and continue button
    Then the user should be on the transfer-agreement-continued page
    When the user selects yes to all transfer agreement part 2 checks
    And the user clicks the Agree and continue button
    Then the user will be on a page with the title "Upload your records"
    When the user selects directory containing: testfile1
    Then the success and removal message container should be visible
    When the user clicks the Start upload button
    Then the user will be on a page with the title "Uploading your records"
    And the upload progress should be visible
    Then the user will be on a page with the title "Checking your records"
    And the file checks completed banner should not be visible
    And the file checks continue button should be disabled
    Then the file checks completed banner should be visible
    And the file checks continue button should be enabled
    When the user clicks the continue button
    Then the user will be on a page with the title "Results of your checks"
    And the user should see a banner titled Success
    When the user clicks on the Next button
    Then the user will be on a page with the fieldset heading "How would you like to enter record metadata?"
    # The additional metadata is tested separately in DescriptiveMetadata.Feature & ClosureMetadata.Feature
    When the user selects the option I don't have any metadata
    And the user clicks the Continue button
    Then the user will be on a page with the title "Download and review metadata"
#    Due to metadata review feature, navigating to confirm transfer page by updating url
#    When the user clicks on the Next button
    When the logged in user navigates to the confirm-transfer page
    Then the user will be on a page with the title "Confirm transfer"
    When the user confirms that they are transferring legal custody of the records to TNA
    And the user clicks the Transfer your records button
    Then the user will be on a page with a panel titled "Transfer complete"
    And the standard transfer export will be complete
