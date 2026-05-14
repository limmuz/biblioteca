package com.qs.biblioteca;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

public abstract class BaseMongoTest {

    private static final MongoDBContainer MONGO = criarMongo();

    @SuppressWarnings("java:S108")
    private static MongoDBContainer criarMongo() {
        try {
            if (DockerClientFactory.instance().isDockerAvailable()) {
                MongoDBContainer c = new MongoDBContainer(DockerImageName.parse("mongo:7.0"));
                c.start();
                return c;
            }
        } catch (Exception ignored) {}
        return null;
    }

    @BeforeAll
    static void skipIfDockerUnavailable() {
        assumeTrue(MONGO != null, "Docker não disponível — testes ignorados");
    }

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        if (MONGO != null) {
            registry.add("spring.data.mongodb.uri", MONGO::getConnectionString);
        }
    }
}
