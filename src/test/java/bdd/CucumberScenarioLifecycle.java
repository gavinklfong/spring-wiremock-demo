package bdd;

import bdd.setups.WireMockSetup;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;

@Slf4j
@RequiredArgsConstructor
public class CucumberScenarioLifecycle {

    @LocalServerPort
    private int serverPort;

    @Autowired
    private CucumberTestContext testContext;

    @Before
    public void setUp() {
        log.info("SpringBoot port = " + serverPort);
        testContext.setQuotationSrvUrl("http://localhost:" + serverPort);
    }

    @After
    public void cleanUp() {

        WireMockSetup.reset();
        testContext.reset();

        log.info("Test context clean up completed");
    }
}
