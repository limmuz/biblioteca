package com.qs.biblioteca;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class BaseMongoTest {

    static final MongoDBContainer mongoDBContainer =
            new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

    static {
        mongoDBContainer.start();
    }

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getConnectionString);
    }
}
