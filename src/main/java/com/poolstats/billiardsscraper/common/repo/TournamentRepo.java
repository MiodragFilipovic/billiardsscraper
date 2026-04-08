package com.poolstats.billiardsscraper.common.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.poolstats.billiardsscraper.common.entity.Tournament;

public interface TournamentRepo extends JpaRepository<Tournament, Long> {

	Optional<Tournament> findByExternalId(String externalId);

	Tournament findTopByOrderByDateDesc();

	List<Tournament> findByClubId(Long clubId);

	List<Tournament> findAllByOrderByDateDesc();

	Page<Tournament> findAllByOrderByDateDesc(Pageable pageable);

	@Query("SELECT t FROM Tournament t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) ORDER BY t.date DESC")
	Page<Tournament> searchByName(@Param("search") String search, Pageable pageable);
}



