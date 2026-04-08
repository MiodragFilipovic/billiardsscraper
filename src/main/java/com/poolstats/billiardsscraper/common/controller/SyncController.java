package com.poolstats.billiardsscraper.common.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poolstats.billiardsscraper.common.entity.Match;
import com.poolstats.billiardsscraper.common.entity.PlayerTournamentStats;
import com.poolstats.billiardsscraper.common.repo.MatchRepo;
import com.poolstats.billiardsscraper.common.repo.PlayerTournamentStatsRepo;
import com.poolstats.billiardsscraper.common.service.PlayerTournamentStatsService;
import com.poolstats.billiardsscraper.common.service.ScraperService;
import org.springframework.transaction.annotation.Transactional;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/api/sync")
@Api(tags = "Sync Controller", description = "Endpoints for syncing data from external billiards websites")
public class SyncController {

	private static final Logger log = LoggerFactory.getLogger(SyncController.class);

        @Autowired
        private ScraperService scraperService;

        @Autowired
        private MatchRepo matchRepo;

        @Autowired
        private PlayerTournamentStatsService statsService;

        @Autowired
        private PlayerTournamentStatsRepo statsRepo;

	@GetMapping("/players")
	@ApiOperation(value = "Sync Players", notes = "Fetches and syncs all player data from the external website")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Players updated successfully"),
			@ApiResponse(code = 500, message = "Failed to update players")
	})
	public String syncPlayers() {
		try {
			log.info("Starting player sync via API endpoint");
			scraperService.syncPlayersFromWebsite();
			return "Players updated successfully";
		} catch (Exception e) {
			log.error("Failed to update players", e);
			e.printStackTrace();
			return "Failed to update players: " + e.getMessage();
		}
	}

	@GetMapping("/clubs")
	@ApiOperation(value = "Sync Clubs", notes = "Fetches and syncs all club data from the external website")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Clubs updated successfully"),
			@ApiResponse(code = 500, message = "Failed to update clubs")
	})
	public String syncClubs() {
		try {
			log.info("Starting club sync via API endpoint");
			scraperService.syncClubsFromWebsite();
			return "Clubs updated successfully";
		} catch (Exception e) {
			log.error("Failed to update clubs", e);
			e.printStackTrace();
			return "Failed to update clubs: " + e.getMessage();
		}
	}

	@GetMapping("/tournaments/all")
	@ApiOperation(value = "Sync All Tournaments", notes = "Fetches and syncs all tournament data from the external website for all years")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Tournaments updated successfully"),
			@ApiResponse(code = 500, message = "Failed to update tournaments")
	})
	public String syncAllTournaments() {
		try {
			log.info("Starting all tournaments sync via API endpoint");
			scraperService.syncAllTournamentsFromWebsite();
			return "All tournaments updated successfully";
		} catch (Exception e) {
			log.error("Failed to update tournaments", e);
			e.printStackTrace();
			return "Failed to update tournaments: " + e.getMessage();
		}
	}

	@GetMapping("/tournaments/year/{year}")
	@ApiOperation(value = "Sync Tournaments for Specific Year", notes = "Fetches and syncs tournament data for a specific year")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Tournaments updated successfully"),
			@ApiResponse(code = 500, message = "Failed to update tournaments")
	})
	public String syncTournamentsForYear(
			@ApiParam(value = "Year to sync (e.g., 2026)", required = true)
			@PathVariable String year) {
		try {
			log.info("Starting tournament sync for year {} via API endpoint", year);
			scraperService.syncTournamentsForYear(year);
			return "Tournaments for year " + year + " updated successfully";
		} catch (Exception e) {
			log.error("Failed to update tournaments for year {}", year, e);
			e.printStackTrace();
			return "Failed to update tournaments for year " + year + ": " + e.getMessage();
		}
	}

	@GetMapping("/tournaments/latest")
	@ApiOperation(value = "Sync Latest Tournaments", notes = "Fetches and syncs only the most recent tournaments (current year)")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Latest tournaments updated successfully"),
			@ApiResponse(code = 500, message = "Failed to update latest tournaments")
	})
	public String syncLatestTournaments() {
		try {
			log.info("Starting latest tournaments sync via API endpoint");
			scraperService.syncLatestTournaments();
			return "Latest tournaments updated successfully";
		} catch (Exception e) {
			log.error("Failed to update latest tournaments", e);
			e.printStackTrace();
			return "Failed to update latest tournaments: " + e.getMessage();
		}
	}

	@GetMapping("/matches/all")
	@ApiOperation(value = "Sync All Matches", notes = "Fetches and syncs all match data from the external website")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Matches updated successfully"),
			@ApiResponse(code = 500, message = "Failed to update matches")
	})
	public String syncAllMatches() {
		try {
			log.info("Starting all matches sync via API endpoint");
			scraperService.syncAllMatchesFromWebsite();
			return "All matches updated successfully";
		} catch (Exception e) {
			log.error("Failed to update matches", e);
			e.printStackTrace();
			return "Failed to update matches: " + e.getMessage();
		}
	}

	@GetMapping("/matches/all-db")
	@ApiOperation(value = "Sync Matches for All DB Tournaments", notes = "Fetches and syncs matches (and player stats) for every tournament that exists in the database")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Matches for all DB tournaments updated successfully"),
			@ApiResponse(code = 500, message = "Failed to sync matches for DB tournaments")
	})
	public String syncMatchesForAllDbTournaments() {
		try {
			log.info("Starting match sync for all DB tournaments via API endpoint");
			scraperService.syncMatchesForAllDbTournaments();
			return "Matches for all DB tournaments updated successfully";
		} catch (Exception e) {
			log.error("Failed to sync matches for all DB tournaments", e);
			e.printStackTrace();
			return "Failed to sync matches for all DB tournaments: " + e.getMessage();
		}
	}

	@GetMapping("/matches/latest")
	@ApiOperation(value = "Sync Latest Tournament Matches", notes = "Fetches and syncs matches only for the most recent tournament")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Latest tournament matches updated successfully"),
			@ApiResponse(code = 500, message = "Failed to update latest tournament matches")
	})
	public String syncLatestTournamentMatches() {
		try {
			log.info("Starting latest tournament matches sync via API endpoint");
			scraperService.syncMatchesForLatestTournament();
			return "Latest tournament matches updated successfully";
		} catch (Exception e) {
			log.error("Failed to update latest tournament matches", e);
			e.printStackTrace();
			return "Failed to update latest tournament matches: " + e.getMessage();
		}
	}

	@GetMapping("/matches/tournament/{externalId}")
	@ApiOperation(value = "Sync Matches for Specific Tournament", notes = "Fetches and syncs matches for a tournament by its external ID")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Tournament matches updated successfully"),
			@ApiResponse(code = 404, message = "Tournament not found"),
			@ApiResponse(code = 500, message = "Failed to update tournament matches")
	})
	public String syncTournamentMatches(
			@ApiParam(value = "External ID of the tournament (e.g., 12345)", required = true)
			@PathVariable String externalId) {
		try {
			log.info("Starting match sync for tournament {} via API endpoint", externalId);
			scraperService.syncMatchesForTournament(externalId);
			return "Matches for tournament " + externalId + " updated successfully";
		} catch (Exception e) {
			log.error("Failed to update matches for tournament {}", externalId, e);
			e.printStackTrace();
			return "Failed to update matches for tournament " + externalId + ": " + e.getMessage();
		}
	}

	@GetMapping("/stats/tournament/{externalId}")
	@ApiOperation(value = "Sync Player Stats for Specific Tournament", notes = "Fetches and syncs player tournament statistics by tournament external ID")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Tournament stats updated successfully"),
			@ApiResponse(code = 404, message = "Tournament not found"),
			@ApiResponse(code = 500, message = "Failed to update tournament stats")
	})
	public String syncTournamentStats(
			@ApiParam(value = "External ID of the tournament (e.g., 12345)", required = true)
			@PathVariable String externalId) {
		try {
			log.info("Starting player stats sync for tournament {} via API endpoint", externalId);
			scraperService.syncPlayerStatsForTournament(externalId);
			return "Player stats for tournament " + externalId + " updated successfully";
		} catch (Exception e) {
			log.error("Failed to update player stats for tournament {}", externalId, e);
			e.printStackTrace();
			return "Failed to update player stats for tournament " + externalId + ": " + e.getMessage();
		}
	}

	@GetMapping("/full")
	@ApiOperation(value = "Full Sync", notes = "Performs a complete sync: clubs -> players -> latest tournaments -> latest matches")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Full sync completed successfully"),
			@ApiResponse(code = 500, message = "Full sync failed")
	})
	public String fullSync() {
		try {
			log.info("=== Starting FULL SYNC ===");

			log.info("Step 1/4: Syncing clubs...");
			scraperService.syncClubsFromWebsite();

			log.info("Step 2/4: Syncing players...");
			scraperService.syncPlayersFromWebsite();

			log.info("Step 3/4: Syncing latest tournaments...");
			scraperService.syncLatestTournaments();

			log.info("Step 4/4: Syncing latest tournament matches...");
			scraperService.syncMatchesForLatestTournament();

			log.info("=== FULL SYNC COMPLETED ===");
			return "Full sync completed successfully: clubs -> players -> latest tournaments -> latest matches";
		} catch (Exception e) {
			log.error("Full sync failed", e);
			e.printStackTrace();
			return "Full sync failed: " + e.getMessage();
		}
	}

        @GetMapping("/fix-ratings/player/{playerId}")
        @ApiOperation(value = "Fix Rating History for Player",
                notes = "Reconstructs finalRating per tournament for a single player using ratingChange values (works backwards from current rating)")
        @ApiResponses(value = {
                @ApiResponse(code = 200, message = "Rating history fixed"),
                @ApiResponse(code = 500, message = "Failed to fix rating history")
        })
        public String fixRatingHistoryForPlayer(
                        @ApiParam(value = "Internal DB player ID", required = true)
                        @PathVariable Long playerId) {
                try {
                        log.info("Fixing rating history for player id={}", playerId);
                        return statsService.recalcRatingHistoryForPlayer(playerId);
                } catch (Exception e) {
                        log.error("Failed to fix rating history for player {}", playerId, e);
                        return "Failed: " + e.getMessage();
                }
        }

        @GetMapping("/fix-ratings/all")
        @ApiOperation(value = "Fix Rating History for ALL Players",
                notes = "Reconstructs finalRating per tournament for every player. May take several minutes.")
        @ApiResponses(value = {
                @ApiResponse(code = 200, message = "Rating history fixed for all players"),
                @ApiResponse(code = 500, message = "Failed")
        })
        public String fixRatingHistoryForAllPlayers() {
                try {
                        log.info("Fixing rating history for ALL players");
                        return statsService.recalcRatingHistoryForAllPlayers();
                } catch (Exception e) {
                        log.error("Failed to fix rating history for all players", e);
                        return "Failed: " + e.getMessage();
                }
        }

        @GetMapping("/fix-wins-losses/player/{playerId}")
        @ApiOperation(value = "Fix Wins/Losses for Single Player",
                notes = "Visits the player's individual profile page on bilijar.club and re-scrapes Pobeda/Poraza values.")
        @ApiResponses(value = {
                @ApiResponse(code = 200, message = "Wins/losses updated"),
                @ApiResponse(code = 500, message = "Failed")
        })
        public String fixWinsLossesForPlayer(
                        @ApiParam(value = "Internal DB player ID", required = true)
                        @PathVariable Long playerId) {
                try {
                        log.info("Fix wins/losses for player id={}", playerId);
                        return scraperService.syncWinsLossesForPlayer(playerId);
                } catch (Exception e) {
                        log.error("Failed to fix wins/losses for player {}", playerId, e);
                        return "Failed: " + e.getMessage();
                }
        }

        @GetMapping("/fix-wins-losses/all")
        @ApiOperation(value = "Fix Wins/Losses for ALL Players",
                notes = "Visits each player's individual profile page and re-scrapes Pobeda/Poraza. May take several minutes.")
        @ApiResponses(value = {
                @ApiResponse(code = 200, message = "Wins/losses updated for all players"),
                @ApiResponse(code = 500, message = "Failed")
        })
        public String fixWinsLossesForAllPlayers() {
                try {
                        log.info("Fix wins/losses for ALL players");
                        return scraperService.syncWinsLossesForAllPlayers();
                } catch (Exception e) {
                        log.error("Failed to fix wins/losses for all players", e);
                        return "Failed: " + e.getMessage();
                }
        }

        @GetMapping("/dedup-stats")
        @Transactional
        @ApiOperation(value = "Remove Duplicate Stats",
                notes = "Removes duplicate player_tournament_stats rows, keeping only the one with the highest ID per player+tournament pair.")
        public String dedupStats() {
                try {
                        log.info("=== Starting player_tournament_stats deduplication ===");
                        java.util.List<PlayerTournamentStats> all = statsRepo.findAll();

                        // Group by (playerId, tournamentId)
                        java.util.Map<String, java.util.List<PlayerTournamentStats>> grouped = new java.util.HashMap<>();
                        for (PlayerTournamentStats s : all) {
                                String key = s.getPlayer().getId() + "_" + s.getTournament().getId();
                                grouped.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(s);
                        }

                        int deleted = 0;
                        java.util.List<PlayerTournamentStats> toDelete = new java.util.ArrayList<>();
                        for (java.util.List<PlayerTournamentStats> group : grouped.values()) {
                                if (group.size() <= 1) continue;
                                // Keep highest ID (most recently inserted), delete the rest
                                group.sort(java.util.Comparator.comparingLong(PlayerTournamentStats::getId).reversed());
                                for (int i = 1; i < group.size(); i++) {
                                        toDelete.add(group.get(i));
                                        deleted++;
                                }
                        }
                        statsRepo.deleteAll(toDelete);
                        String result = String.format("Deduplication complete: %d duplicate rows deleted, %d groups processed", deleted, grouped.size());
                        log.info(result);
                        return result;
                } catch (Exception e) {
                        log.error("Deduplication failed", e);
                        return "Deduplication failed: " + e.getMessage();
                }
        }

        @GetMapping("/fix-winners")
	@Transactional
	@ApiOperation(value = "Fix Winners", notes = "Backfills winner field for all existing matches based on result scores")
	public String fixWinners() {
		try {
			log.info("=== Starting winner backfill for all matches ===");
			java.util.List<Match> all = matchRepo.findAll();
			int fixed = 0;
			int skipped = 0;

			for (Match m : all) {
				if (m.getWinner() != null) {
					skipped++;
					continue;
				}
				if (m.getPlayer1() == null || m.getPlayer2() == null) {
					skipped++;
					continue;
				}
				Integer r1 = m.getResult1();
				Integer r2 = m.getResult2();
				if (r1 == null || r2 == null) {
					skipped++;
					continue;
				}

				if (r1 == -1 && r2 >= 0) {
					m.setWinner(m.getPlayer2());
					fixed++;
				} else if (r2 == -1 && r1 >= 0) {
					m.setWinner(m.getPlayer1());
					fixed++;
				} else if (r1 > r2) {
					m.setWinner(m.getPlayer1());
					fixed++;
				} else if (r2 > r1) {
					m.setWinner(m.getPlayer2());
					fixed++;
				} else {
					skipped++;
				}
			}

			matchRepo.saveAll(all);
			log.info("=== Winner backfill completed: fixed={}, skipped={} ===", fixed, skipped);
			return String.format("Winner backfill completed: %d fixed, %d skipped", fixed, skipped);
		} catch (Exception e) {
			log.error("Winner backfill failed", e);
			return "Winner backfill failed: " + e.getMessage();
		}
	}
}
