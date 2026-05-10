package com.qs.biblioteca;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Base para todos os testes que precisam de MongoDB.
 * Inicia o container uma vez e sobrepõe a URI via @DynamicPropertySource,
 * garantindo que nenhum teste conecte ao banco de produção.
 */
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
