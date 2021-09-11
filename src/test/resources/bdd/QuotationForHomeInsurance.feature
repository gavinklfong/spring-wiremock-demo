Feature: Generate quotation for home insurance product

  Background:
    Given a home insurance product with specification:
    | attribute                 | value       |
    | highRiskAge               | 70          |
    | highRiskAgeAdjustmentRate | 1.5         |
    | discountPostCodeList      | SW11,N24,E3 |
    | postCodeDiscountRate      | 0.3         |
    | listPrice                 | 1500        |

  Scenario Outline: Generate quotation for combination of customer age and post code
    Given a customer of <age> years old
    When submit a quotation request for an address with '<postCode>'
    Then a quotation is generated with price equal to <expectedPrice>
    And a quotation is saved in database

    Examples:
    | age     | postCode | expectedPrice |
    | 18      | SW13     | 1500          |
    | 18      | SW11     | 1050          |
    | 70      | SW13     | 2250          |
    | 70      | SW11     | 1575          |