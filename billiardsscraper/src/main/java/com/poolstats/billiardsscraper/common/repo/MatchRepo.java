package com.poolstats.billiardsscraper.common.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poolstats.billiardsscraper.common.entity.Match;

public interface MatchRepo extends JpaRepository<Match, Long> {

}