package bdd.steps;

import bdd.CucumberTestContext;
import bdd.apiclients.QuotationSrvClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.javafaker.Faker;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import space.gavinklfong.insurance.quotation.apiclients.models.Customer;
import space.gavinklfong.insurance.quotation.apiclients.models.Product;
import space.gavinklfong.insurance.quotation.dtos.QuotationReq;
import space.gavinklfong.insurance.quotation.models.Quotation;
import space.gavinklfong.insurance.quotation.repositories.QuotationRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class QuotationSteps {

    private Faker faker = new Faker();

    @Autowired
    WireMockServer wireMockServer;

    @Autowired
    QuotationRepository quotationRepository;

    @Autowired
    CucumberTestContext testContext;

    ObjectMapper objectMapper = new ObjectMapper();

    static final private long DEFAULT_CUSTOMER_ID = 12L;
    static final private String DEFAULT_PRODUCT_CODE = "CAR001-01";

    public QuotationSteps() {
        super();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Given("a home insurance product with specification:")
    public void a_home_insurance_product_with_specification(DataTable dataTable) throws JsonProcessingException {

        Map<String, String> dataMap = dataTable.asMap(String.class, String.class);

        // Construct product
        Product product = Product.builder()
                .productCode(DEFAULT_PRODUCT_CODE)
                .productClass("Online")
                .productPlan("Home-General")
                .buildingSumInsured(faker.number().randomNumber())
                .contentSumInsured(faker.number().randomNumber())
                .buildsAccidentalDamage("Optional")
                .contentsAccidentalDamage("Optional")
                .maxAlternativeAccommodation(faker.number().randomNumber())
                .matchingItems(faker.bool().bool())
                .maxAlternativeAccommodation(faker.number().randomNumber())
                .maxValuables(faker.number().randomNumber())
                .contentsInGarden(faker.number().randomNumber())
                .theftFromOutbuildings(faker.number().randomNumber())
                .customerAgeThreshold(Integer.parseInt(dataMap.get("highRiskAge")))
                .customerAgeThresholdAdjustmentRate(Double.parseDouble(dataMap.get("highRiskAgeAdjustmentRate")))
                .discountPostCode(dataMap.get("discountPostCodeList").split(","))
                .postCodeDiscountRate(Double.parseDouble(dataMap.get("postCodeDiscountRate")))
                .listedPrice(Long.parseLong(dataMap.get("listPrice")))
                .build();

        // Set up Product API Stub
        wireMockServer.stubFor(get(urlEqualTo(String.format("/products/%s", DEFAULT_PRODUCT_CODE)))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(product)))
        );

        // Save product to context
        testContext.setProduct(product);
    }

    @Given("a customer of {int} years old")
    public void a_customer_of_years_old(Integer age) throws JsonProcessingException {

        // Construct Customer
        Customer customer = Customer.builder()
                .id(DEFAULT_CUSTOMER_ID)
                .name(faker.name().name())
                .dob(LocalDate.now().minusYears(age))
                .build();

        // Set up Customer API Stub
        wireMockServer.stubFor(get(urlEqualTo(String.format("/customers/%s", DEFAULT_CUSTOMER_ID)))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(this.objectMapper.writeValueAsString(customer)))
        );

        // Save customer to context
        testContext.setCustomer(customer);
    }

    @When("submit a quotation request for an address with {string}")
    public void submit_a_quotation_request_for_an_address_with(String postCode) throws IOException {

        // Construct request
        QuotationReq req = QuotationReq.builder()
                .postCode(postCode)
                .customerId(DEFAULT_CUSTOMER_ID)
                .productCode(DEFAULT_PRODUCT_CODE)
                .build();

        // Submit quotation
        QuotationSrvClient quotationSrvClient = new QuotationSrvClient(testContext.getQuotationSrvUrl());
        Quotation quotation = quotationSrvClient.generateQuotation(req);

        // Save the generated quotation into context for the verification in the next step
        testContext.setQuotationReq(req);
        testContext.setQuotation(quotation);
    }

    @Then("a quotation is generated with price equal to {double}")
    public void a_quotation_is_generated_with_price_equal_to(double expectedPrice) {
        Quotation quotation = testContext.getQuotation();
        QuotationReq request = testContext.getQuotationReq();
        assertThat(quotation.getProductCode(), equalTo(request.getProductCode()));
        assertThat(quotation.getAmount(), equalTo(expectedPrice));
    }

    @Then("a quotation is saved in database")
    public void a_quotation_is_saved_in_database() {
        Quotation quotation = testContext.getQuotation();
        Optional<Quotation> quotationFromRepoOptional = quotationRepository.findById(quotation.getQuotationCode());
        assertTrue(quotationFromRepoOptional.isPresent());
        Quotation quotationFromRepo = quotationFromRepoOptional.get();
        assertThat(quotationFromRepo.getQuotationCode(), equalTo(quotation.getQuotationCode()));
        assertThat(quotationFromRepo.getAmount(), equalTo(quotation.getAmount()));
    }

}
