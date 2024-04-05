package io.axoniq.demo.gamerental;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.axoniq.dataprotection.api.FieldEncryptingSerializer;
import io.axoniq.dataprotection.cryptoengine.CryptoEngine;
import io.axoniq.dataprotection.cryptoengine.jpa.JpaCryptoEngine;
import jakarta.persistence.EntityManagerFactory;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventEncryptionTransformationConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public FieldEncryptingSerializer encryptingSerializer(CryptoEngine cryptoEngine, ObjectMapper objectMapper) {
        Serializer serializer = JacksonSerializer.builder()
                                                 .objectMapper(objectMapper.copy())
                                                 .defaultTyping()
                                                 .lenientDeserialization()
                                                 .build();
        return new FieldEncryptingSerializer(cryptoEngine, serializer, serializer);
    }

    @Bean
    public CryptoEngine cryptoEngine(EntityManagerFactory entityManagerFactory) {
        return new JpaCryptoEngine(entityManagerFactory);
    }
}
