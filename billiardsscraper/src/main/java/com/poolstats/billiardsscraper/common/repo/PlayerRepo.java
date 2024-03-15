package com.poolstats.billiardsscraper.common.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poolstats.billiardsscraper.common.entity.Player;

public interface PlayerRepo extends JpaRepository<Player, Long> {

	Optional<Player> findByFullName(String fullName);

}