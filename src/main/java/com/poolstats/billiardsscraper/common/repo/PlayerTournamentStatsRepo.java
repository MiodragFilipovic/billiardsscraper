package com.poolstats.billiardsscraper.common.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poolstats.billiardsscraper.common.entity.PlayerTournamentStats;

public interface PlayerTournamentStatsRepo extends JpaRepository<PlayerTournamentStats, Long> {

	Optional<PlayerTournamentStats> findByPlayerIdAndTournamentId(Long playerId, Long tournamentId);

	List<PlayerTournamentStats> findByTournamentId(Long tournamentId);

	List<PlayerTournamentStats> findByPlayerId(Long playerId);

	List<PlayerTournamentStats> findByTournamentIdOrderByPositionAsc(Long tournamentId);

	List<PlayerTournamentStats> findByPlayerIdAndTournamentIdOrderByIdAsc(Long playerId, Long tournamentId);
}
