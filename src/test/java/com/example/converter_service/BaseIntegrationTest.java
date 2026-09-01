package com.example.converter_service;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTest {

    protected static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("conversions")
                    .withUsername("postgres")
                    .withPassword("postgres");

    protected static final KafkaContainer KAFKA =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    protected static final MinIOContainer MINIO =
            new StableMinIOContainer(DockerImageName.parse("minio/minio:RELEASE.2025-09-07T16-13-09Z"))
                    .withUserName("minioadmin")
                    .withPassword("minioadmin")
                    .withStartupTimeout(Duration.ofSeconds(60));

    @BeforeAll
    static void startContainers() {
        POSTGRES.start();
        KAFKA.start();
        MINIO.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);

        registry.add("minio.endpoint", MINIO::getS3URL);
        registry.add("minio.access-key", MINIO::getUserName);
        registry.add("minio.secret-key", MINIO::getPassword);
        registry.add("minio.bucket", () -> "conversions");
        registry.add("spring.kafka.admin.auto-create", () -> "false");

        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.liquibase.drop-first", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    private static class StableMinIOContainer extends MinIOContainer {
        public StableMinIOContainer(DockerImageName dockerImageName) {
            super(dockerImageName);
        }

        @Override
        public WaitStrategy getWaitStrategy() {
            return Wait.forHttp("/minio/health/live").forPort(9000);
        }
    }
}