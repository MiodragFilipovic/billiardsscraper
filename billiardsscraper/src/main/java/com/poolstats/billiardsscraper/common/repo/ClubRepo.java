package com.poolstats.billiardsscraper.common.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poolstats.billiardsscraper.common.entity.Club;

public interface ClubRepo extends JpaRepository<Club, Long> {

	Optional<Club> findByExternalId(String externalId);
}