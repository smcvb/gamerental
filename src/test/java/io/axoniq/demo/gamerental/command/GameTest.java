package io.axoniq.demo.gamerental.command;

import io.axoniq.axonserver.connector.AxonServerConnection;
import io.axoniq.axonserver.connector.AxonServerConnectionFactory;
import io.axoniq.axonserver.connector.impl.ServerAddress;
import io.axoniq.demo.gamerental.coreapi.ExceptionStatusCode;
import io.axoniq.demo.gamerental.coreapi.GameRegisteredEvent;
import io.axoniq.demo.gamerental.coreapi.GameRentedEvent;
import io.axoniq.demo.gamerental.coreapi.GameReturnedEvent;
import io.axoniq.demo.gamerental.coreapi.RegisterGameCommand;
import io.axoniq.demo.gamerental.coreapi.RentGameCommand;
import io.axoniq.demo.gamerental.coreapi.RentalCommandException;
import io.axoniq.demo.gamerental.coreapi.ReturnGameCommand;
import jakarta.annotation.Nonnull;
import org.axonframework.axonserver.connector.AxonServerConfiguration;
import org.axonframework.configuration.ApplicationConfigurer;
import org.axonframework.configuration.ComponentRegistry;
import org.axonframework.configuration.ConfigurationEnhancer;
import org.axonframework.test.fixture.AxonTestFixture;
import org.axonframework.test.fixture.MessagesRecordingConfigurationEnhancer;
import org.axonframework.test.server.AxonServerContainer;
import org.axonframework.test.server.AxonServerContainerUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static io.axoniq.demo.gamerental.TestUtils.*;

@SpringBootTest
@Testcontainers
class GameTest {

    private AxonTestFixture fixture;
    @Autowired
    private ApplicationConfigurer configurer;

    @Container
    private static final AxonServerContainer container = new AxonServerContainer().withDevMode(true);
    private static AxonServerConnection connection;

    @BeforeAll
    static void beforeAll() {
        container.start();
        ServerAddress address = new ServerAddress(container.getHost(), container.getGrpcPort());
        connection = AxonServerConnectionFactory.forClient("GameTest")
                                                .routingServers(address)
                                                .build()
                                                .connect("default");
    }

    @AfterAll
    static void afterAll() {
        connection.disconnect();
        container.stop();
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public MessagesRecordingConfigurationEnhancer recordingConfigurationEnhancer() {
            return new MessagesRecordingConfigurationEnhancer();
        }

        @Bean
        public ConfigurationEnhancer serverConfigurationEnhancer() {
            return new ConfigurationEnhancer() {
                @Override
                public void enhance(@Nonnull ComponentRegistry registry) {
                    registry.registerComponent(AxonServerConfiguration.class,
                                               c -> {
                                                   AxonServerConfiguration serverConfig = new AxonServerConfiguration();
                                                   serverConfig.setServers(
                                                           container.getHost() + ":" + container.getGrpcPort());
                                                   return serverConfig;
                                               });
                }

                @Override
                public int order() {
                    return Integer.MIN_VALUE;
                }
            };
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        fixture = AxonTestFixture.with(configurer);
        AxonServerContainerUtils.purgeEventsFromAxonServer(container.getHost(),
                                                           container.getHttpPort(),
                                                           "default",
                                                           AxonServerContainerUtils.DCB_CONTEXT);
    }

    @Test
    void testRegisterGameCommandAppliesGameRegisteredEvent() {
        fixture.given()
               .noPriorActivity()
               .when()
               .command(new RegisterGameCommand(GAME_IDENTIFIER, TITLE, RELEASE_DATE, DESCRIPTION, true, true))
               .then()
               .events(new GameRegisteredEvent(GAME_IDENTIFIER, TITLE, RELEASE_DATE, DESCRIPTION, true, true));
    }

    @Test
    void testRentGameCommandAppliesGameRentedEvent() {
        fixture.given()
               .events(new GameRegisteredEvent(GAME_IDENTIFIER, TITLE, RELEASE_DATE, DESCRIPTION, true, true))
               .when()
               .command(new RentGameCommand(GAME_IDENTIFIER, RENTER))
               .then()
               .events(new GameRentedEvent(GAME_IDENTIFIER, RENTER));
    }

    @Test
    void testRentGameCommandThrowsExceptionForInsufficientStock() {
        fixture.given()
               .events(
                       new GameRegisteredEvent(GAME_IDENTIFIER, TITLE, RELEASE_DATE, DESCRIPTION, true, true),
                       new GameRentedEvent(GAME_IDENTIFIER, RENTER)
               )
               .when()
               .command(new RentGameCommand(GAME_IDENTIFIER, RENTER))
               .then()
               .exception(RentalCommandException.class, ExceptionStatusCode.INSUFFICIENT.getDescription());
    }

    @Test
    void testRentGameCommandThrowsExceptionForToEarlyRenting() {
        Instant futureDate = Instant.now().plus(1, ChronoUnit.DAYS);
        fixture.given()
               .events(new GameRegisteredEvent(GAME_IDENTIFIER, TITLE, futureDate, DESCRIPTION, true, true))
               .when()
               .command(new RentGameCommand(GAME_IDENTIFIER, RENTER))
               .then()
               .exception(RentalCommandException.class, ExceptionStatusCode.UNRELEASED.getDescription());
    }

    @Test
    void testReturnGameCommandAppliesGameReturnedEvent() {
        fixture.given()
               .events(
                       new GameRegisteredEvent(GAME_IDENTIFIER, TITLE, RELEASE_DATE, DESCRIPTION, true, true),
                       new GameRentedEvent(GAME_IDENTIFIER, RENTER)
               )
               .when()
               .command(new ReturnGameCommand(GAME_IDENTIFIER, RENTER))
               .then()
               .events(new GameReturnedEvent(GAME_IDENTIFIER, RENTER));
    }

    @Test
    void testReturnGameCommandThrowsExceptionForReturnerNotMatchingOriginalRenter() {
        fixture.given()
               .events(new GameRegisteredEvent(GAME_IDENTIFIER, TITLE, RELEASE_DATE, DESCRIPTION, true, true))
               .when()
               .command(new ReturnGameCommand(GAME_IDENTIFIER, RENTER))
               .then()
               .exception(RentalCommandException.class, ExceptionStatusCode.DIFFERENT_RETURNER.getDescription());
    }
}
