package io.axoniq.demo.gamerental.command;

import io.axoniq.demo.gamerental.coreapi.GameRegisteredEvent;
import io.axoniq.demo.gamerental.coreapi.GameRentedEvent;
import io.axoniq.demo.gamerental.coreapi.GameReturnedEvent;
import io.axoniq.demo.gamerental.coreapi.RegisterGameCommand;
import io.axoniq.demo.gamerental.coreapi.RentGameCommand;
import io.axoniq.demo.gamerental.coreapi.ReturnGameCommand;
import org.axonframework.eventsourcing.CriteriaResolver;
import org.axonframework.eventsourcing.EventSourcedEntityFactory;
import org.axonframework.eventsourcing.configuration.EventSourcedEntityModule;
import org.axonframework.eventsourcing.snapshot.api.SnapshotPolicy;
import org.axonframework.messaging.commandhandling.GenericCommandResultMessage;
import org.axonframework.messaging.core.MessageStream;
import org.axonframework.messaging.core.MessageType;
import org.axonframework.messaging.core.MessageTypeResolver;
import org.axonframework.messaging.eventhandling.gateway.EventAppender;
import org.axonframework.messaging.eventstreaming.EventCriteria;
import org.axonframework.messaging.eventstreaming.Tag;
import org.axonframework.modelling.EntityIdResolver;
import org.axonframework.modelling.configuration.EntityMetamodelConfigurationBuilder;
import org.axonframework.modelling.configuration.EntityModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class GameConfiguration {

    private static final GenericCommandResultMessage VOID_COMMAND_RESULT =
            new GenericCommandResultMessage(new MessageType(Void.class), (Object) null);

    @Bean
    public EntityModule<String, Game> gameModule() {
        // Auto-detected configuration
//        return EventSourcedEntityModule.autodetected(String.class, Game.class);

        // Declarative configuration
        return EventSourcedEntityModule.declarative(String.class, Game.class)
                                       .messagingModel(messagingModel())
                                       .entityFactory(config -> gameEntityFactory())
                                       .criteriaResolver(config -> gameCriteriaResolver())
                                       .entityIdResolver(config -> gameIdResolver())
                                       .snapshotPolicy(config -> SnapshotPolicy.afterEvents(50).or(
                                               SnapshotPolicy.whenSourcingTimeExceeds(Duration.ofSeconds(10))))

                                       .build();
    }

    private static EntityMetamodelConfigurationBuilder<Game> messagingModel() {
        return (config, builder) -> {
            MessageTypeResolver typeResolver = config.getComponent(MessageTypeResolver.class);
            return builder.creationalCommandHandler(
                                  typeResolver.resolveOrThrow(RegisterGameCommand.class).qualifiedName(),
                                  (command, context) -> {
                                      Game.handle(
                                              command.payloadAs(RegisterGameCommand.class),
                                              EventAppender.forContext(context)
                                      );
                                      return MessageStream.just(VOID_COMMAND_RESULT);
                                  }
                          )
                          .instanceCommandHandler(
                                  typeResolver.resolveOrThrow(RentGameCommand.class).qualifiedName(),
                                  (command, entity, context) -> {
                                      entity.handle(
                                              command.payloadAs(RentGameCommand.class),
                                              EventAppender.forContext(context)
                                      );
                                      return MessageStream.just(VOID_COMMAND_RESULT);
                                  }
                          )
                          .instanceCommandHandler(
                                  typeResolver.resolveOrThrow(ReturnGameCommand.class).qualifiedName(),
                                  (command, entity, context) -> {
                                      entity.handle(
                                              command.payloadAs(ReturnGameCommand.class),
                                              EventAppender.forContext(context)
                                      );
                                      return MessageStream.just(VOID_COMMAND_RESULT);
                                  }
                          )
                          .entityEvolver((entity, event, context) -> {
                              String localName = event.type().qualifiedName().localName();
                              return switch (localName) {
                                  case "registered" -> entity;
                                  case "returned" -> entity.on(event.payloadAs(GameReturnedEvent.class));
                                  case "rented" -> entity.on(event.payloadAs(GameRentedEvent.class));
                                  default -> throw new RuntimeException("Unknown event...");
                              };
                          })
                          .build();
        };
    }

    private static EventSourcedEntityFactory<String, Game> gameEntityFactory() {
        return EventSourcedEntityFactory.fromEventMessage(
                (id, event) -> new Game(event.payloadAs(GameRegisteredEvent.class))
        );
    }

    private static CriteriaResolver<String> gameCriteriaResolver() {
        return (identifier, context) -> EventCriteria.havingTags(Tag.of("gameId", identifier));
    }

    private static EntityIdResolver<String> gameIdResolver() {
        return (message, context) -> {
            String localName = message.type().qualifiedName().localName();
            return switch (localName) {
                case "register" -> message.payloadAs(RegisterGameCommand.class).gameIdentifier();
                case "return" -> message.payloadAs(ReturnGameCommand.class).gameIdentifier();
                case "rent" -> message.payloadAs(RentGameCommand.class).gameIdentifier();
                default -> throw new RuntimeException(
                        "Cannot command message of type [" + message.type() + "]"
                );
            };
        };
    }
}
