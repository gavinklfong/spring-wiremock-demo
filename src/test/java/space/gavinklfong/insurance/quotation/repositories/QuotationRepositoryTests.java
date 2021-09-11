package space.gavinklfong.insurance.quotation.repositories;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import space.gavinklfong.insurance.quotation.models.Quotation;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
public class QuotationRepositoryTests {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private QuotationRepository quotationRepository;

    private Faker faker = new Faker();

    @AfterEach
    void cleanUp() {
        this.quotationRepository.deleteAll();
    }

    @Test
    void saveQuotation() {
        quotationRepository.save(
                Quotation.builder()
                .quotationCode(UUID.randomUUID().toString())
                .productCode(faker.code().toString())
                .amount(faker.number().randomDouble(2, 1000, 50000))
                .build());

        quotationRepository.save(
                Quotation.builder()
                        .quotationCode(UUID.randomUUID().toString())
                        .productCode(faker.code().toString())
                        .amount(faker.number().randomDouble(2, 1000, 50000))
                        .build());

        quotationRepository.save(
                Quotation.builder()
                        .quotationCode(UUID.randomUUID().toString())
                        .productCode(faker.code().toString())
                        .amount(faker.number().randomDouble(2, 1000, 50000))
                        .build());

        List<Quotation> quotation = quotationRepository.findAll();

        assertEquals(3, quotation.size());
    }
}
