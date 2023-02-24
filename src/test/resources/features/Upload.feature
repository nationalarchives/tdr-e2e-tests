#Feature will not work with chromedriver in headless mode due to following bug: https://bugs.chromium.org/p/chromedriver/issues/detail?id=2521&q=directory%20upload&colspec=ID%20Status%20Pri%20Owner%20Summary

Feature: Upload
  Scenario: The success message and progress bar are hidden before file selection/upload
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing private beta transfer agreement
    And an existing compliance transfer agreement
    And the user is logged in on the upload page
    Then the user will be on a page with the title "Upload your records"
    Then the upload progress should not be visible
    And the success and removal message container should not be visible

  Scenario: The success message should be displayed when a folder is selected
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing private beta transfer agreement
    And an existing compliance transfer agreement
    And the user is logged in on the upload page
    When the user selects directory containing: testfile1
    Then the success and removal message container should be visible

  Scenario: The progress bar is shown after file upload
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing private beta transfer agreement
    And an existing compliance transfer agreement
    And the user is logged in on the upload page
    When the user selects directory containing: largefile
    And the user clicks the continue button
    Then the user will be on a page with the title "Uploading your records"
    And the upload progress should be visible

  Scenario: The file checks page is shown when the upload is completed
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing private beta transfer agreement
    And an existing compliance transfer agreement
    And the user is logged in on the upload page
    When the user selects directory containing: testfile1
    And the user clicks the continue button
    Then the user will be on a page with the title "Uploading your records"
    And the upload progress should be visible
    Then the user will be on a page with the title "Checking your records"

  Scenario: The upload is complete page is shown when the user navigates back from the file checks page after upload has completed
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing private beta transfer agreement
    And an existing compliance transfer agreement
    And the user is logged in on the upload page
    When the user selects directory containing: testfile1
    And the user clicks the continue button
    Then the user will be on a page with the title "Uploading your records"
    And the upload progress should be visible
    Then the user will be on a page with the title "Checking your records"
    When the user clicks their browser's back button
    Then the user will see the message "Your upload is complete and has been saved. You cannot make amendments to your upload or add additional files."

  Scenario: Consignment upload page is accessed by a logged out user
    Given A logged out standard user
    And an existing standard consignment for transferring body MOCK1
    And an existing private beta transfer agreement
    And an existing compliance transfer agreement
    And the logged out user attempts to access the upload page
    Then the logged out user should be on the login page

  Scenario: The judgment's file checks page is shown when the upload is completed
    Given A logged out judgment user
    And an existing judgment consignment for transferring body MOCK1
    And the user is logged in on the upload page
    When the user selects the file: testdocxfile.docx
    And the success and removal message container should be visible
    And the user clicks the continue button
    Then the user will be on a page with the title "Uploading judgment"
    And the upload progress should be visible
    Then the user will be on a page with the title "Checking your upload"
