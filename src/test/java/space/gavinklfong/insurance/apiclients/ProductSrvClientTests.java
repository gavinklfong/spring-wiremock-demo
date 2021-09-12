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
import space.gavinklfong.insurance.quotation.apiclients.ProductSrvClient;
import space.gavinklfong.insurance.quotation.apiclients.ProductSrvClientImpl;
import space.gavinklfong.insurance.quotation.apiclients.models.Product;

import java.io.IOException;
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
public class ProductSrvClientTests {

    static final private String PRODUCT_CODE = "CAR001-01";
    private final int HIGH_RISK_AGE = 70;
    private final double HIGH_RISK_AGE_ADJ_RATE = 1.5;
    private final double POST_CODE_DISCOUNT_RATE = 0.3;
    private final long LISTED_PRICE = 1500L;
    private final String[] PRODUCT_POST_CODE = {"SW20", "SM1", "E12" };

    public static WireMockServer wireMockRule = new WireMockServer(options().dynamicPort());

    @Autowired
    private ProductSrvClient productSrvClient;

    ObjectMapper objectMapper = new ObjectMapper();

    private Faker faker = new Faker();

    @TestConfiguration
    static class WebClientTestConfiguration {
        @Bean
        ProductSrvClient productSrvClient(@Value("${app.productSrvUrl}") String url) {
            return new ProductSrvClientImpl(url);
        }
    }

    public ProductSrvClientTests() {
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
        registry.add("app.productSrvUrl", wireMockRule::baseUrl);
    }

    @Test
    void givenProductExists_whenRetrieveProduct_thenSuccess() throws IOException {

        // Given
        Product product = Product.builder()
                .productCode(PRODUCT_CODE)
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
                .customerAgeThreshold(HIGH_RISK_AGE)
                .customerAgeThresholdAdjustmentRate(HIGH_RISK_AGE_ADJ_RATE)
                .discountPostCode(PRODUCT_POST_CODE)
                .postCodeDiscountRate(POST_CODE_DISCOUNT_RATE)
                .listedPrice(LISTED_PRICE)
                .build();

        wireMockRule.stubFor(get(urlEqualTo("/products/" + PRODUCT_CODE))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(product)))
        );

        // When
        List<Product> products = productSrvClient.getProducts(PRODUCT_CODE);

        // Then
        wireMockRule.verify(
                getRequestedFor(urlEqualTo("/products/" + PRODUCT_CODE))
        );
        assertThat(products, is(notNullValue()));
        assertThat(products.size(), is(1));
        assertThat(products.get(0).getProductCode(), is(PRODUCT_CODE));
    }

    void givenProductNotExists_whenRetrieveProduct_thenFail() throws IOException {

        // Given
        wireMockRule.stubFor(get(urlEqualTo("/products/" + PRODUCT_CODE))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.NOT_FOUND_404))
        );

        // When
        List<Product> products = productSrvClient.getProducts(PRODUCT_CODE);

        // Then
        wireMockRule.verify(
                getRequestedFor(urlEqualTo("/products/" + PRODUCT_CODE))
        );
        assertTrue(products.isEmpty());
    }

}
