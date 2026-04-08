package com.poolstats.billiardsscraper.common.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poolstats.billiardsscraper.common.entity.Club;
import com.poolstats.billiardsscraper.common.entity.Player;
import com.poolstats.billiardsscraper.common.entity.Tournament;
import com.poolstats.billiardsscraper.common.repo.ClubRepo;
import com.poolstats.billiardsscraper.common.repo.PlayerRepo;
import com.poolstats.billiardsscraper.common.repo.TournamentRepo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/clubs")
@Api(tags = "Clubs", description = "Endpoints for retrieving club data")
public class ClubController {

	@Autowired
	private ClubRepo clubRepo;

	@Autowired
	private PlayerRepo playerRepo;

	@Autowired
	private TournamentRepo tournamentRepo;

	@GetMapping
	@ApiOperation(value = "Get all clubs")
	public List<Club> getAllClubs() {
		return clubRepo.findAll();
	}

	@GetMapping("/{id}")
	@ApiOperation(value = "Get club by ID")
	public ResponseEntity<Club> getClubById(@PathVariable Long id) {
		return clubRepo.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/{id}/players")
	@ApiOperation(value = "Get players for a club")
	public List<Player> getClubPlayers(@PathVariable Long id) {
		return playerRepo.findByClubId(id);
	}

	@GetMapping("/{id}/tournaments")
	@ApiOperation(value = "Get tournaments for a club")
	public List<Tournament> getClubTournaments(@PathVariable Long id) {
		return tournamentRepo.findByClubId(id);
	}
}

