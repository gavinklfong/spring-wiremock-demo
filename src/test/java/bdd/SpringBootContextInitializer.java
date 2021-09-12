package bdd;

import bdd.setups.TestContainersSetup;
import bdd.setups.WireMockSetup;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import static bdd.setups.TestContainersSetup.getMongoDBContainerUri;

public class SpringBootContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {

        TestContainersSetup.initTestContainers(configurableApplicationContext.getEnvironment());

        WireMockSetup.setUp();
        WireMockSetup.registerComponent(configurableApplicationContext);

        TestPropertyValues values = TestPropertyValues.of(
                "spring.data.mongodb.uri=" + getMongoDBContainerUri(),
                "app.customerSrvUrl=" + WireMockSetup.getBaseUrl(),
                "app.productSrvUrl=" + WireMockSetup.getBaseUrl()
        );

        values.applyTo(configurableApplicationContext);
    }
}
