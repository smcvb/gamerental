package io.axoniq.demo.gamerental.command;

import io.axoniq.demo.gamerental.coreapi.GameRegisteredEvent;
import io.axoniq.demo.gamerental.coreapi.GameRentedEvent;
import io.axoniq.demo.gamerental.coreapi.GameReturnedEvent;
import io.axoniq.demo.gamerental.coreapi.RegisterGameCommand;
import io.axoniq.demo.gamerental.coreapi.RentGameCommand;
import io.axoniq.demo.gamerental.coreapi.ReturnGameCommand;
import org.axonframework.commandhandling.GenericCommandResultMessage;
import org.axonframework.eventhandling.gateway.EventAppender;
import org.axonframework.eventsourcing.CriteriaResolver;
import org.axonframework.eventsourcing.EventSourcedEntityFactory;
import org.axonframework.eventsourcing.configuration.EventSourcedEntityModule;
import org.axonframework.eventstreaming.EventCriteria;
import org.axonframework.eventstreaming.Tag;
import org.axonframework.messaging.MessageStream;
import org.axonframework.messaging.MessageType;
import org.axonframework.messaging.QualifiedName;
import org.axonframework.modelling.command.EntityIdResolver;
import org.axonframework.modelling.configuration.EntityMetamodelConfigurationBuilder;
import org.axonframework.modelling.configuration.EntityModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameConfiguration {

    private static final GenericCommandResultMessage<Object> VOID_COMMAND_RESULT =
            new GenericCommandResultMessage<>(new MessageType(Void.class), null);

    @Bean
    public EntityModule<String, Game> gameModule() {
        return EventSourcedEntityModule.annotated(String.class, Game.class);
//        return EventSourcedEntityModule.declarative(String.class, Game.class)
//                                       .messagingModel(messagingModel())
//                                       .entityFactory(config -> gameEntityFactory())
//                                       .criteriaResolver(config -> gameCriteriaResolver())
//                                       .entityIdResolver(config -> gameIdResolver());
    }

    private static EntityMetamodelConfigurationBuilder<Game> messagingModel() {
        return (config, builder) ->
                builder.creationalCommandHandler(
                               new QualifiedName(RegisterGameCommand.class),
                               (command, context) -> {
                                   Game.handle(
                                           ((RegisterGameCommand) command.getPayload()),
                                           EventAppender.forContext(context, config)
                                   );
                                   return MessageStream.just(VOID_COMMAND_RESULT);
                               }
                       )
                       .instanceCommandHandler(
                               new QualifiedName(RentGameCommand.class),
                               (command, entity, context) -> {
                                   entity.handle(((RentGameCommand) command.getPayload()),
                                                 EventAppender.forContext(context, config));
                                   return MessageStream.just(VOID_COMMAND_RESULT);
                               }
                       )
                       .instanceCommandHandler(
                               new QualifiedName(ReturnGameCommand.class),
                               (command, entity, context) -> {
                                   entity.handle(((ReturnGameCommand) command.getPayload()),
                                                 EventAppender.forContext(context, config));
                                   return MessageStream.just(VOID_COMMAND_RESULT);
                               }
                       )
                       .entityEvolver((entity, event, context) -> {
                           Object eventPayload = event.getPayload();
                           if (eventPayload instanceof GameRegisteredEvent) {
                               return entity;
                           } else if (eventPayload instanceof GameReturnedEvent gameReturnedEvent) {
                               return entity.on(gameReturnedEvent);
                           } else if (eventPayload instanceof GameRentedEvent gameRentedEvent) {
                               return entity.on(gameRentedEvent);
                           }
                           throw new RuntimeException("Unknown event...");
                       })
                       .build();
    }

    private static EventSourcedEntityFactory<String, Game> gameEntityFactory() {
        return EventSourcedEntityFactory.fromEventMessage(
                (id, event) -> new Game(((GameRegisteredEvent) event.getPayload()))
        );
    }

    private static CriteriaResolver<String> gameCriteriaResolver() {
        return (identifier, context) -> EventCriteria.havingTags(Tag.of("gameId", identifier));
    }

    private static EntityIdResolver<String> gameIdResolver() {
        return (message, context) -> {
            Object payload = message.getPayload();
            if (payload instanceof RegisterGameCommand registerGameCommand) {
                return registerGameCommand.gameIdentifier();
            } else if (payload instanceof ReturnGameCommand returnGameCommand) {
                return returnGameCommand.gameIdentifier();
            } else if (payload instanceof RentGameCommand rentGameCommand) {
                return rentGameCommand.gameIdentifier();
            }
            throw new RuntimeException(
                    "Cannot command message of type [" + message.type() + "]"
            );
        };
    }
}
