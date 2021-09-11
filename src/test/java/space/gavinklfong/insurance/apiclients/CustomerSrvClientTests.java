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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import space.gavinklfong.insurance.quotation.apiclients.CustomerSrvClient;
import space.gavinklfong.insurance.quotation.apiclients.CustomerSrvClientImpl;
import space.gavinklfong.insurance.quotation.apiclients.models.Customer;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.time.ZoneId;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@Slf4j
@SpringJUnitConfig
@Tag("UnitTest")
public class CustomerSrvClientTests {

    static final long DEFAULT_CUSTOMER_ID = 12L;

    public static WireMockServer wireMockRule = new WireMockServer(options().dynamicPort());

    @Autowired
    private CustomerSrvClient customerSrvClient;

    @TestConfiguration
    static class WebClientTestConfiguration {
        @Bean
        CustomerSrvClient customerSrvClient(@Value("${app.customerSrvUrl}") String url){
            return new CustomerSrvClientImpl(url);
        }
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

    private Faker faker = new Faker();

    @Test
    void givenCustomerExists_whenRetrieveCustomer_thenSuccess() throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Given
        wireMockRule.stubFor(get(urlEqualTo("/customers/" + DEFAULT_CUSTOMER_ID))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(objectMapper.writeValueAsString(
                                Customer.builder()
                                        .id(12l)
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
        assertThat(customers, is(notNullValue()));
        assertThat(customers.size(), is(1));
        assertThat(customers.get(0).getId(), is(DEFAULT_CUSTOMER_ID));
    }

    void givenCustomerNotExists_whenRetrieveCustomer_thenFail() throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Given
        wireMockRule.stubFor(get(urlEqualTo("/customers/" + DEFAULT_CUSTOMER_ID))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.NOT_FOUND_404))
        );

        // When
        List<Customer> customers = customerSrvClient.getCustomers(DEFAULT_CUSTOMER_ID);

        // Then
        assertThat(customers, is(notNullValue()));
        assertThat(customers.size(), is(1));
        assertThat(customers.get(0).getId(), is(DEFAULT_CUSTOMER_ID));
    }

}
