package space.gavinklfong.insurance.apiclients;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.javafaker.Faker;
import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import space.gavinklfong.insurance.quotation.apiclients.CustomerSrvClient;
import space.gavinklfong.insurance.quotation.apiclients.CustomerSrvClientImpl;
import space.gavinklfong.insurance.quotation.apiclients.models.Customer;

import java.io.IOException;
import java.time.ZoneId;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringJUnitConfig
@Tag("UnitTest")
public class CustomerSrvClientTests {

    public static WireMockServer wireMockRule = new WireMockServer(options().dynamicPort());

    static final long DEFAULT_CUSTOMER_ID = 12L;
    private Faker faker = new Faker();
    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CustomerSrvClient customerSrvClient;

    @TestConfiguration
    static class WebClientTestConfiguration {
        @Bean
        CustomerSrvClient customerSrvClient(@Value("${app.customerSrvUrl}") String url) {
            return new CustomerSrvClientImpl(url);
        }
    }

    public CustomerSrvClientTests() {
        super();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @BeforeAll
    public static void beforeAll() {
        wireMockRule.start();
    }

    @AfterAll
    public static void afterAll() {
        wireMockRule.stop();
    }

    @AfterEach
    public void afterEach() {
        wireMockRule.resetAll();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("app.customerSrvUrl", wireMockRule::baseUrl);
    }

    @Test
    void givenCustomerExists_whenRetrieveCustomer_thenSuccess() throws IOException {

        // Given
        wireMockRule.stubFor(get(urlEqualTo("/customers/" + DEFAULT_CUSTOMER_ID))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(
                                Customer.builder()
                                        .id(DEFAULT_CUSTOMER_ID)
                                        .name(faker.name().name())
                                        .dob(faker.date().birthday().toInstant()
                                                .atZone(ZoneId.systemDefault())
                                                .toLocalDate())
                                        .build()
                                )
                        ))
        );

        // When
        List<Customer> customers = customerSrvClient.getCustomers(DEFAULT_CUSTOMER_ID);

        // Then
        wireMockRule.verify(
                getRequestedFor(urlEqualTo("/customers/" + DEFAULT_CUSTOMER_ID))
        );
        assertThat(customers, is(notNullValue()));
        assertThat(customers.size(), is(1));
        assertThat(customers.get(0).getId(), is(DEFAULT_CUSTOMER_ID));
    }

    @Test
    void givenCustomerNotExists_whenRetrieveCustomer_thenFail() throws IOException {

        // Given
        wireMockRule.stubFor(get(urlEqualTo("/customers/" + DEFAULT_CUSTOMER_ID))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.NOT_FOUND_404))
        );

        // When
        List<Customer> customers = customerSrvClient.getCustomers(DEFAULT_CUSTOMER_ID);

        // Then
        wireMockRule.verify(
                getRequestedFor(urlEqualTo("/customers/" + DEFAULT_CUSTOMER_ID))
        );
        assertTrue(customers.isEmpty());
    }

}
