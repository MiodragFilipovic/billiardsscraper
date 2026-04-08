package com.poolstats.billiardsscraper.common.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.poolstats.billiardsscraper.common.entity.Player;

public interface PlayerRepo extends JpaRepository<Player, Long> {

	Optional<Player> findByFullName(String fullName);

	List<Player> findAllByFullName(String fullName);

	List<Player> findByClubId(Long clubId);

	Page<Player> findByFullNameContainingIgnoreCase(String fullName, Pageable pageable);

	Page<Player> findAllByOrderByRatingDesc(Pageable pageable);
}

