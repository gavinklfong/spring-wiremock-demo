package bdd;

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

    @After
    public void cleanUp() {

        testContext.reset();

        log.info("Test context clean up completed");
    }

    @Before
    public void setUp() {

        log.info("### server port = " + serverPort);
        testContext.setQuotationSrvUrl("http://localhost:" + serverPort);
    }


}
