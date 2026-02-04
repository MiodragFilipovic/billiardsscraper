package com.poolstats.billiardsscraper.common.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poolstats.billiardsscraper.common.service.ScraperService;

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
}
