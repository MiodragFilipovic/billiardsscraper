package com.poolstats.billiardsscraper.common.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poolstats.billiardsscraper.common.entity.Tournament;

public interface TournamentRepo extends JpaRepository<Tournament, Long> {

	Optional<Tournament> findByExternalId(String externalId);
}