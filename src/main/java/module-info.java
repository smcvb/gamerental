module gamerental {
    // Exports 'coreapi' as consciously shared module:
    exports io.axoniq.demo.gamerental.coreapi;

    // Opened modules for reflection:
    opens io.axoniq.demo.gamerental to spring.core, spring.beans, spring.context;
    opens io.axoniq.demo.gamerental.coreapi to com.fasterxml.jackson.databind, org.axonframework.messaging;
    opens io.axoniq.demo.gamerental.command to spring.core, org.axonframework.messaging;
    opens io.axoniq.demo.gamerental.query to spring.core, org.axonframework.messaging, org.hibernate.orm.core;
    opens io.axoniq.demo.gamerental.controller to spring.beans, spring.core, com.fasterxml.jackson.databind;

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
    // Logging
    requires org.slf4j;
    // Reactor
    requires reactor.core;
    // Spring
    requires spring.aop;
    requires spring.beans;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.core;
    requires spring.data.commons;
    requires spring.data.jpa;
    requires spring.tx;
    requires spring.web;
    // Axon Framework
    requires org.axonframework.config;
    requires org.axonframework.eventsourcing;
    requires org.axonframework.messaging;
    requires org.axonframework.modelling;
    requires org.axonframework.spring;
    requires org.axonframework.extensions.reactor;
}