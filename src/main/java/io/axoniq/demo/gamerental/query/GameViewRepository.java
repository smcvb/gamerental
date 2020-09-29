package io.axoniq.demo.gamerental.query;

import org.springframework.data.jpa.repository.JpaRepository;

interface GameViewRepository extends JpaRepository<GameView, String> {

}
