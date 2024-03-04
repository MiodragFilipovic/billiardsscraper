package common.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import common.entity.Player;

public interface PlayerRepo extends JpaRepository<Player, Long> {
}