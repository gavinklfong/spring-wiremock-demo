package bdd;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import space.gavinklfong.insurance.quotation.QuotationSrvApplication;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(
        initializers = { SpringBootContextInitializer.class },
        classes = {QuotationSrvApplication.class, CucumberTestContextConfig.class}
)
@ActiveProfiles(profiles={"bdd"})
public class CucumberSpringBootContext {

}
