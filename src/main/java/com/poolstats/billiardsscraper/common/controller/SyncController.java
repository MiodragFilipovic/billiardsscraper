package com.poolstats.billiardsscraper.common.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poolstats.billiardsscraper.common.service.ScraperService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/api/sync")
@Api(tags = "Sync Controller", description = "Endpoints for syncing data from external billiards websites")
public class SyncController {

	@Autowired
	private ScraperService scraperService;

	@GetMapping("/players")
	@ApiOperation(value = "Sync Players", notes = "Fetches and syncs all player data from the external website")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Players updated successfully"),
			@ApiResponse(code = 500, message = "Failed to update players")
	})
	public String sync() {
		try {
			scraperService.syncPlayersFromWebsite();
			return "Players updated successfully";
		} catch (Exception e) {
			e.printStackTrace();
			return "Failed to update players";
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
			scraperService.syncClubsFromWebsite();
			return "Clubs updated successfully";
		} catch (Exception e) {
			e.printStackTrace();
			return "Failed to update clubs";
		}
	}

	@GetMapping("/allTournaments")
	@ApiOperation(value = "Sync All Tournaments", notes = "Fetches and syncs all tournament data from the external website")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Tournaments updated successfully"),
			@ApiResponse(code = 500, message = "Failed to update tournaments")
	})
	public String syncAllTournaments() {
		try {
			scraperService.syncAllTournamentsFromWebsite();
			return "Tournaments updated successfully";
		} catch (Exception e) {
			e.printStackTrace();
			return "Failed to update tournaments";
		}
	}

	@GetMapping("/allMatches")
	@ApiOperation(value = "Sync All Matches", notes = "Fetches and syncs all match data from the external website")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Matches updated successfully"),
			@ApiResponse(code = 500, message = "Failed to update matches")
	})
	public String syncAllMatches() {
		try {
			scraperService.syncAllMatchesFromWebsite();
			return "Matches updated successfully";
		} catch (Exception e) {
			e.printStackTrace();
			return "Failed to update matches";
		}
	}
}
