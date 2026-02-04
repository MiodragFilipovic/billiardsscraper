package com.poolstats.billiardsscraper.common.service.impl;

import java.util.Optional;

import org.htmlunit.html.HtmlTableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poolstats.billiardsscraper.common.entity.Player;
import com.poolstats.billiardsscraper.common.entity.PlayerTournamentStats;
import com.poolstats.billiardsscraper.common.entity.Tournament;
import com.poolstats.billiardsscraper.common.repo.PlayerTournamentStatsRepo;
import com.poolstats.billiardsscraper.common.service.PlayerService;
import com.poolstats.billiardsscraper.common.service.PlayerTournamentStatsService;

@Service
public class PlayerTournamentStatsServiceImpl implements PlayerTournamentStatsService {

	private static final Logger log = LoggerFactory.getLogger(PlayerTournamentStatsServiceImpl.class);

	@Autowired
	private PlayerTournamentStatsRepo statsRepo;

	@Autowired
	private PlayerService playerService;

	@Override
	public void saveStatsFromTableRow(HtmlTableRow row, Tournament tournament) {
		try {
			int position = Integer.parseInt(row.getCell(0).getTextContent().trim());
			String playerName = row.getCell(1).getTextContent().trim();

			playerName = playerName.replaceAll("★", "").trim();

			Player player = playerService.getPlayerByName(playerName);
			if (player == null) {
				log.warn("Player not found: {}", playerName);
				return;
			}

			int points = parseInteger(row.getCell(3).getTextContent());
			int gamesWon = parseInteger(row.getCell(4).getTextContent());
			int gamesLost = parseInteger(row.getCell(5).getTextContent());
			int matchesWon = parseInteger(row.getCell(7).getTextContent());
			int matchesLost = parseInteger(row.getCell(8).getTextContent());

			double gameWinPercentage = parsePercentage(row.getCell(6).getTextContent());
			double matchWinPercentage = parsePercentage(row.getCell(9).getTextContent());

			String ratingCell = row.getCell(10).getTextContent().trim();
			double ratingChange = parseRatingChange(ratingCell);

			String finalRatingCell = row.getCell(11).getTextContent().trim();
			double finalRating = parseFinalRating(finalRatingCell);

			Optional<PlayerTournamentStats> existingStats =
				statsRepo.findByPlayerIdAndTournamentId(player.getId(), tournament.getId());

			PlayerTournamentStats stats;
			if (existingStats.isPresent()) {
				stats = existingStats.get();
			} else {
				stats = new PlayerTournamentStats();
				stats.setPlayer(player);
				stats.setTournament(tournament);
			}

			stats.setPosition(position);
			stats.setPoints(points);
			stats.setGamesWon(gamesWon);
			stats.setGamesLost(gamesLost);
			stats.setGameWinPercentage(gameWinPercentage);
			stats.setMatchesWon(matchesWon);
			stats.setMatchesLost(matchesLost);
			stats.setMatchWinPercentage(matchWinPercentage);
			stats.setRatingChange(ratingChange);
			stats.setFinalRating(finalRating);

			statsRepo.save(stats);

			log.debug("Saved tournament stats for player: {} (position: {}, rating change: {})",
				playerName, position, ratingChange);

		} catch (Exception e) {
			log.error("Failed to save tournament stats: {}", e.getMessage(), e);
		}
	}

	private int parseInteger(String text) {
		try {
			return Integer.parseInt(text.trim());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private double parsePercentage(String text) {
		try {
			String cleaned = text.replaceAll("[^0-9.]", "").trim();
			if (cleaned.isEmpty()) return 0.0;
			return Double.parseDouble(cleaned);
		} catch (NumberFormatException e) {
			return 0.0;
		}
	}

	private double parseRatingChange(String text) {
		try {
			String cleaned = text.replaceAll("[^0-9.\\-]", "").trim();
			if (cleaned.isEmpty()) return 0.0;
			return Double.parseDouble(cleaned);
		} catch (NumberFormatException e) {
			return 0.0;
		}
	}

	private double parseFinalRating(String text) {
		try {
			String cleaned = text.replaceAll("[^0-9.]", "").trim();
			if (cleaned.isEmpty()) return 0.0;
			return Double.parseDouble(cleaned);
		} catch (NumberFormatException e) {
			return 0.0;
		}
	}
}
