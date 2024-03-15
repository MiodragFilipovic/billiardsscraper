package com.poolstats.billiardsscraper.common.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poolstats.billiardsscraper.common.service.ScraperService;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

	@Autowired
	private ScraperService scraperService;

	@GetMapping("/players")
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
	public String syncAllMatches() {
		try {
			scraperService.syncAllMatchesFromWebsite();
			return "Tournaments updated successfully";
		} catch (Exception e) {
			e.printStackTrace();
			return "Failed to update tournaments";
		}
	}
}
