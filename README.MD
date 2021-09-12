# Sample Implementation of Automated Test Using WireMock

## Overview
This repository provides a sample Spring Boot Application for the demonstration of WireMock Usage in Unit Test and Component Test.

It is a insurance product quotation API, it exposes an endpoint ``POST /quotations/generate`` and quotation generation.

![Application Component Diagram](https://github.com/gavinklfong/spring-wiremock-demo/blob/main/blob/WireMock-Quotation_Service.png?raw=true)

## Build

Run this command to build and run all automated tests

`mvn clean install`

If you would like to run the component test only, then run this command

`mvn -Dtest=CucumberTest test`

## Unit Test

Unit test demonstrates the testing on API client with API stub created using WireMock.

![Unit Test Diagram](https://github.com/gavinklfong/spring-wiremock-demo/blob/main/blob/WireMock-Unit_Test.png?raw=true)


## Component Test

Component test is a block box test, it submits requests to the quotation service and verify the outputs. All external API services are mocked using WireMock.

![Component Test Diagram](https://github.com/gavinklfong/spring-wiremock-demo/blob/main/blob/WireMock-Component_Test.png?raw=true)


