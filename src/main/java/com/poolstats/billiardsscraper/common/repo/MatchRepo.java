package com.poolstats.billiardsscraper.common.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.poolstats.billiardsscraper.common.entity.Match;

public interface MatchRepo extends JpaRepository<Match, Long> {

	List<Match> findByTournamentId(Long tournamentId);

	Page<Match> findByTournamentIdOrderByOrderNumberAsc(Long tournamentId, Pageable pageable);

	List<Match> findByTournamentExternalId(String externalId);

	/** All matches where the given player participated as either player1 or player2. */
	@Query("SELECT m FROM Match m WHERE (m.player1.id = :playerId OR m.player2.id = :playerId) AND m.winner IS NOT NULL AND m.player1 IS NOT NULL AND m.player2 IS NOT NULL")
	List<Match> findAllMatchesForPlayer(@Param("playerId") Long playerId);
}



