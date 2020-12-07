package io.axoniq.demo.gamerental.query;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

@Profile("query")
interface GameViewRepository extends JpaRepository<GameView, String> {

}
