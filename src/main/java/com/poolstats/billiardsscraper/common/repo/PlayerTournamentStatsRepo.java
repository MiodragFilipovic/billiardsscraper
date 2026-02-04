package com.poolstats.billiardsscraper.common.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poolstats.billiardsscraper.common.entity.PlayerTournamentStats;

public interface PlayerTournamentStatsRepo extends JpaRepository<PlayerTournamentStats, Long> {

	Optional<PlayerTournamentStats> findByPlayerIdAndTournamentId(Long playerId, Long tournamentId);
}
