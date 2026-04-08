package com.poolstats.billiardsscraper.common.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.poolstats.billiardsscraper.common.entity.Match;
import com.poolstats.billiardsscraper.common.entity.PlayerTournamentStats;
import com.poolstats.billiardsscraper.common.entity.Tournament;
import com.poolstats.billiardsscraper.common.repo.MatchRepo;
import com.poolstats.billiardsscraper.common.repo.PlayerTournamentStatsRepo;
import com.poolstats.billiardsscraper.common.repo.TournamentRepo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/tournaments")
@Api(tags = "Tournaments", description = "Endpoints for retrieving tournament data")
public class TournamentController {

	@Autowired
	private TournamentRepo tournamentRepo;

	@Autowired
	private MatchRepo matchRepo;

	@Autowired
	private PlayerTournamentStatsRepo statsRepo;

	@GetMapping
	@ApiOperation(value = "Get tournaments with pagination and optional search")
	public Page<Tournament> getAllTournaments(
			@RequestParam(required = false) String search,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		Pageable pageable = PageRequest.of(page, size);
		if (search != null && !search.isBlank()) {
			return tournamentRepo.searchByName(search, pageable);
		}
		return tournamentRepo.findAllByOrderByDateDesc(pageable);
	}

	@GetMapping("/{id}")
	@ApiOperation(value = "Get tournament by ID")
	public ResponseEntity<Tournament> getTournamentById(@PathVariable Long id) {
		return tournamentRepo.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/{id}/matches")
	@ApiOperation(value = "Get matches for a tournament with pagination")
	public Page<Match> getTournamentMatches(
			@PathVariable Long id,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "25") int size) {
		Pageable pageable = PageRequest.of(page, size);
		return matchRepo.findByTournamentIdOrderByOrderNumberAsc(id, pageable);
	}

	@GetMapping("/{id}/matches/all")
	@ApiOperation(value = "Get all matches for a tournament without pagination")
	public List<Match> getAllTournamentMatches(@PathVariable Long id) {
		return matchRepo.findByTournamentId(id);
	}

	@GetMapping("/{id}/stats")
	@ApiOperation(value = "Get player standings for a tournament")
	public List<PlayerTournamentStats> getTournamentStats(@PathVariable Long id) {
		return statsRepo.findByTournamentIdOrderByPositionAsc(id);
	}
}
