Feature: Booking.com Hotel Search Automation

  Scenario: Search and filter hotels in Goa
    Given I open Booking.com homepage
    Then I should see the logo and search box
    When I search for "Goa, India" with check-in in 10 days and check-out in 13 days
    Then I fetch all hotel listings on the first page
    And I print hotel details
    When I apply filters
    Then at least 3 hotels should meet criteria
    When I select the first hotel
    Then I fetch and print room types and prices
    And I scroll to validate reviews or policies section
    And I take a screenshot
