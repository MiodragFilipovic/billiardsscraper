package com.poolstats.billiardsscraper.common.controller;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.poolstats.billiardsscraper.common.dto.H2HRecord;
import com.poolstats.billiardsscraper.common.entity.Match;
import com.poolstats.billiardsscraper.common.entity.Player;
import com.poolstats.billiardsscraper.common.entity.PlayerTournamentStats;
import com.poolstats.billiardsscraper.common.repo.MatchRepo;
import com.poolstats.billiardsscraper.common.repo.PlayerRepo;
import com.poolstats.billiardsscraper.common.repo.PlayerTournamentStatsRepo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/players")
@Api(tags = "Players", description = "Endpoints for retrieving player data")
public class PlayerController {

	@Autowired
	private PlayerRepo playerRepo;

	@Autowired
	private PlayerTournamentStatsRepo statsRepo;

	@Autowired
	private MatchRepo matchRepo;

        @GetMapping
        @ApiOperation(value = "Get players with pagination, optional search and sort")
        public Page<Player> getAllPlayers(
                        @RequestParam(required = false) String search,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(defaultValue = "rating") String sort) {

                Sort sortObj;
                switch (sort) {
                        case "tournamentsWins": sortObj = Sort.by(Sort.Direction.DESC, "tournamentsWins"); break;
                        case "wins":            sortObj = Sort.by(Sort.Direction.DESC, "wins");            break;
                        case "losses":          sortObj = Sort.by(Sort.Direction.DESC, "losses");          break;
                        default:                sortObj = Sort.by(Sort.Direction.DESC, "rating");          break;
                }

                Pageable pageable = PageRequest.of(page, size, sortObj);
                if (search != null && !search.isBlank()) {
                        return playerRepo.findByFullNameContainingIgnoreCase(search, pageable);
                }
                return playerRepo.findAll(pageable);
        }

	@GetMapping("/{id}")
	@ApiOperation(value = "Get player by ID")
	public ResponseEntity<Player> getPlayerById(@PathVariable Long id) {
		return playerRepo.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/{id}/stats")
	@ApiOperation(value = "Get tournament stats for a player")
	public List<PlayerTournamentStats> getPlayerStats(@PathVariable Long id) {
		return statsRepo.findByPlayerId(id);
	}

	/**
	 * Returns top 20 eligible doubles partners for a player.
	 *
	 * Pair rules:
	 *  1. combined rating ≤ 1150
	 *  2. at least one player in the pair must be C** or lower (rating ≤ 535)
	 *
	 * Optional filter: clubIds (comma-separated list of club DB IDs)
	 * Sorted by: partner rating DESC (strongest valid pair first).
	 */
	@GetMapping("/{id}/eligible-partners")
	@ApiOperation(value = "Get eligible doubles partners",
		notes = "Returns top 20 players this player can pair with: sum ≤ 1150 and at least one must be C** or lower (rating ≤ 535). Optional ?clubIds=1,2,3 filter.")
	public ResponseEntity<?> getEligiblePartners(
			@PathVariable Long id,
			@RequestParam(required = false) String clubIds) {
		return playerRepo.findById(id).map(player -> {
			final double MAX_SUM = 1150.0;
			final double C2_MAX  = 535.0;
			double myRating = player.getRating();
			boolean iAmLow  = myRating <= C2_MAX;

			// Parse comma-separated club IDs
			final java.util.Set<Long> clubFilter = new java.util.HashSet<>();
			if (clubIds != null && !clubIds.isBlank()) {
				for (String part : clubIds.split(",")) {
					try { clubFilter.add(Long.parseLong(part.trim())); } catch (NumberFormatException ignored) {}
				}
			}

			List<Map<String, Object>> result = playerRepo.findAll().stream()
				.filter(p -> !p.getId().equals(id))
				.filter(p -> p.getRating() > 0)
				// Club filter (if provided)
				.filter(p -> clubFilter.isEmpty()
					|| (p.getClub() != null && clubFilter.contains(p.getClub().getId())))
				// Rule 1: sum ≤ 1150
				.filter(p -> myRating + p.getRating() <= MAX_SUM)
				// Rule 2: at least one is C2 or lower
				.filter(p -> iAmLow || p.getRating() <= C2_MAX)
				.sorted(Comparator.comparingDouble(Player::getRating).reversed())
				.limit(20)
				.map(p -> {
					Map<String, Object> m = new java.util.LinkedHashMap<>();
					m.put("id",             p.getId());
					m.put("fullName",       p.getFullName());
					m.put("rating",         p.getRating());
					m.put("clubId",         p.getClub() != null ? p.getClub().getId()   : null);
					m.put("club",           p.getClub() != null ? p.getClub().getName() : null);
					m.put("imageURL",       p.getImageURL());
					m.put("combinedRating", myRating + p.getRating());
					m.put("isLowPlayer",    p.getRating() <= C2_MAX);
					return m;
				})
				.collect(Collectors.toList());

			return ResponseEntity.ok(result);
		}).orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/{id}/h2h")
	@ApiOperation(value = "Get head-to-head statistics against all opponents")
	public List<H2HRecord> getH2HStats(@PathVariable Long id) {
		List<Match> matches = matchRepo.findAllMatchesForPlayer(id);
		Map<Long, H2HRecord> h2h = new HashMap<>();

		for (Match m : matches) {
			boolean isPlayer1 = m.getPlayer1().getId().equals(id);
			Player opponent = isPlayer1 ? m.getPlayer2() : m.getPlayer1();
			boolean won = m.getWinner().getId().equals(id);

			String clubName = opponent.getClub() != null ? opponent.getClub().getName() : null;

			h2h.computeIfAbsent(opponent.getId(),
					k -> new H2HRecord(opponent.getId(), opponent.getFullName(), clubName))
					.addMatch(won);
		}

		return h2h.values().stream()
				.sorted(Comparator.comparingInt(H2HRecord::getTotalMatches).reversed())
				.collect(Collectors.toList());
	}
}
