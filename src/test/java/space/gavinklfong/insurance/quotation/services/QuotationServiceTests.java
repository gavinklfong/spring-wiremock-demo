package space.gavinklfong.insurance.quotation.services;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import space.gavinklfong.insurance.quotation.apiclients.CustomerSrvClient;
import space.gavinklfong.insurance.quotation.apiclients.ProductSrvClient;
import space.gavinklfong.insurance.quotation.apiclients.models.Customer;
import space.gavinklfong.insurance.quotation.apiclients.models.Product;
import space.gavinklfong.insurance.quotation.dtos.QuotationReq;
import space.gavinklfong.insurance.quotation.exceptions.RecordNotFoundException;
import space.gavinklfong.insurance.quotation.models.Quotation;
import space.gavinklfong.insurance.quotation.repositories.QuotationRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@Slf4j
@SpringJUnitConfig
@TestPropertySource(properties = {
        "app.quotation.expiryTime=1440"
})
@ContextConfiguration(classes = {QuotationService.class})
@Tag("UnitTest")
class QuotationServiceTests {

    @MockBean
    private QuotationRepository quotationRepo;

    @MockBean
    private CustomerSrvClient customerSrvClient;

    @MockBean
    private ProductSrvClient productSrvClient;

    @Autowired
    private QuotationService quotationService;

    private final Faker faker = new Faker();

    private final String PRODUCT_CODE = "CAR001-01";
    private final long CUSTOMER_ID = 1L;
    private final long LISTED_PRICE = 1500L;

    private final int HIGH_RISK_AGE = 70;
    private final double HIGH_RISK_AGE_ADJ_RATE = 1.5;
    private final double POST_CODE_DISCOUNT_RATE = 0.3;
    private final String[] PRODUCT_POST_CODE = {"SW20", "SM1", "E12" };



    private final Product PRODUCT = Product.builder()
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

    private final List<Product> PRODUCT_LIST = Collections.singletonList(PRODUCT);

    @Test
    void givenCustomerBelowHighRiskAge_whenGenerateQuotation_thenPriceEqualsListedPrice() throws RecordNotFoundException, IOException {

        // Given
        givenCondition(true);

        // When
        QuotationReq req = QuotationReq.builder()
                .customerId(CUSTOMER_ID)
                .postCode("SW11")
                .productCode(PRODUCT_CODE)
                .build();
        Quotation result = quotationService.generateQuotation(req);

        // Then
        assertEquals(LISTED_PRICE, result.getAmount());
        assertTrue(result.getExpiryTime().isAfter(LocalDateTime.now()));
        assertEquals(PRODUCT_CODE, result.getProductCode());
    }

    @Test
    void givenCustomerEqualsOrAboveHighRiskAge_whenGenerateQuotation_thenPriceWithAgeAdjustmentRate() throws RecordNotFoundException, IOException {

        // Given
        givenCondition(false);

        // When
        QuotationReq req = QuotationReq.builder()
                .customerId(CUSTOMER_ID)
                .productCode(PRODUCT_CODE)
                .build();
        Quotation result = quotationService.generateQuotation(req);

        // Then
        assertEquals(LISTED_PRICE * PRODUCT.getCustomerAgeThresholdAdjustmentRate(), result.getAmount());
        assertTrue(result.getExpiryTime().isAfter(LocalDateTime.now()));
        assertEquals(PRODUCT_CODE, result.getProductCode());
    }

    @Test
    void givenCustomerBelowHighRiskAge_whenGenerateQuotationWithDiscountPostCode_thenPriceWithDiscount() throws RecordNotFoundException, IOException {

        // Given
        givenCondition(true);

        // When
        QuotationReq req = QuotationReq.builder()
                .customerId(CUSTOMER_ID)
                .productCode(PRODUCT_CODE)
                .postCode(PRODUCT_POST_CODE[0])
                .build();
        Quotation result = quotationService.generateQuotation(req);

        // Then
        assertEquals(LISTED_PRICE * (1 - PRODUCT.getPostCodeDiscountRate()), result.getAmount());
        assertTrue(result.getExpiryTime().isAfter(LocalDateTime.now()));
        assertEquals(PRODUCT_CODE, result.getProductCode());
    }

    @Test
    void givenCustomerEqualsOrAboveHighRiskAge_whenGenerateQuotationWithDiscountPostCode_thenPriceWithDiscountAndAdjustment() throws RecordNotFoundException, IOException {

        // Given
        givenCondition(false);

        // When
        QuotationReq req = QuotationReq.builder()
                .customerId(CUSTOMER_ID)
                .productCode(PRODUCT_CODE)
                .postCode(PRODUCT_POST_CODE[0])
                .build();
        Quotation result = quotationService.generateQuotation(req);

        // Then
        assertEquals(LISTED_PRICE * (1 - PRODUCT.getPostCodeDiscountRate()) * PRODUCT.getCustomerAgeThresholdAdjustmentRate(), result.getAmount());
        assertTrue(result.getExpiryTime().isAfter(LocalDateTime.now()));
        assertEquals(PRODUCT_CODE, result.getProductCode());
    }

    private void givenCondition(boolean isCustomerBelowHighRiskAge) throws IOException {
        when(quotationRepo.save(any(Quotation.class))).thenAnswer(invocation -> {
            Quotation quotation = invocation.getArgument(0);
            return quotation.withQuotationCode(UUID.randomUUID().toString());
        });

        Date birthday = isCustomerBelowHighRiskAge? faker.date().birthday(18, 69) : faker.date().birthday(HIGH_RISK_AGE, 99);

        List<Customer> customers = Arrays.asList(
                Customer.builder()
                        .id(CUSTOMER_ID)
                        .dob(birthday
                                .toInstant().atZone(ZoneId.systemDefault())
                                .toLocalDate())
                        .name(faker.name().name())
                        .build()
        );
        when(customerSrvClient.getCustomers(anyLong())).thenReturn(customers);

        when(productSrvClient.getProducts(anyString())).thenReturn(PRODUCT_LIST);
    }
}
