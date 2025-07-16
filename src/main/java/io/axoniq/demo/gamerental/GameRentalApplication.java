package io.axoniq.demo.gamerental;

import org.axonframework.config.Configuration;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.updates.UpdateChecker;

public class GameRentalApplication {

    public static void main(String[] args) throws InterruptedException {
        Configuration config = DefaultConfigurer.defaultConfiguration()
                                                .buildConfiguration();

        System.out.println("build!");
        UpdateChecker component = config.getComponent(UpdateChecker.class);
        config.start();
        System.out.println("started!");
        config.shutdown();
        System.out.println("shutdowned!");
    }
}
