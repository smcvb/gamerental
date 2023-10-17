module gamerental {
    // Exports 'coreapi' as consciously shared module:
    exports io.axoniq.demo.gamerental.coreapi;
    // Exported for Spring Devtools
    exports io.axoniq.demo.gamerental;

    // Opened modules for reflection:
    opens io.axoniq.demo.gamerental to spring.core, spring.beans, spring.context;
    opens io.axoniq.demo.gamerental.coreapi to com.fasterxml.jackson.databind, org.axonframework.messaging;

    // Required modules:
    // Serialization
    requires java.desktop;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    // Database
    requires jakarta.annotation;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    // Spring
    requires spring.beans;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.core;
    // Axon Framework
    requires org.axonframework.config;
    requires org.axonframework.eventsourcing;
    requires org.axonframework.messaging;
    requires org.axonframework.modelling;
}