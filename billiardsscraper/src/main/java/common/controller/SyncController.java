package common.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import common.service.ScraperService;

@RestController
@RequestMapping("/api/players")
public class SyncController {

	@Autowired
	private ScraperService scraperService;

	@GetMapping("/update")
	public String updatePlayers() {
		try {
			scraperService.syncPlayersFromWebsite();
			return "Players updated successfully";
		} catch (Exception e) {
			e.printStackTrace();
			return "Failed to update players";
		}
	}
}
