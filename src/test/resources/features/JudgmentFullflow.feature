#Feature will not work with chromedriver in headless mode due to following bug: https://bugs.chromium.org/p/chromedriver/issues/detail?id=2521&q=directory%20upload&colspec=ID%20Status%20Pri%20Owner%20Summary

Feature: Judgment Full user journey
  Scenario: Full judgment transfer workflow
    Given A logged out judgment user
    When the user navigates to TDR Start Page
    And the user clicks on the Start now button
    And the logged out user enters valid credentials
    And the user clicks the Sign in button
    Then the user should be on the homepage page
    When the user clicks the Start transfer button
    Then the user should be on a page with before-uploading and a consignmentId in the URL
    When the user clicks on the Continue button
    Then the user will be on a page with the title "Upload document"
    When the user selects the file: testdocxfile.docx
    Then the success and removal message container should be visible
    When the user clicks the Start upload button
    Then the user will be on a page with the title "Uploading document"
    Then the user will be on a page with the title "Checking your document"
    Then the user will be on a page with a panel titled "Transfer complete"
    And the judgment transfer export will be complete
