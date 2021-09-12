package bdd.setups;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;

@Slf4j
public class TestContainersSetup {

    private static final int MONGODB_PORT = 27017;
    private static final String MONGODB_IMAGE = "mongo";
    private static final Logger MONGODB_LOGGER = LoggerFactory.getLogger("container.MongoDB");
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer(MONGODB_IMAGE).withExposedPorts(MONGODB_PORT);

    public static void initTestContainers(ConfigurableEnvironment configEnv) {
        mongoDBContainer.start();
        mongoDBContainer.followOutput(new Slf4jLogConsumer(MONGODB_LOGGER));
    }

    public static String getMongoDBContainerUri() {
        return mongoDBContainer.getReplicaSetUrl();
    }
}
