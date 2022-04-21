Feature: FAQ Page

  Scenario: FAQ page is accessed by a standard user
    Given A logged in standard user
    And the logged in user navigates to the faq page
    Then the user will be on a page with a small heading "How does TDR work?"

  Scenario: FAQ page is accessed by a judgment user
    Given A logged in judgment user
    And the logged in user navigates to the faq page
    Then the user will be on a page with a small heading "How does transferring a judgment record work?"
