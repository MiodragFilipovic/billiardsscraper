package com.poolstats.billiardsscraper.common.service.impl;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.htmlunit.html.HtmlTableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.poolstats.billiardsscraper.common.entity.Player;
import com.poolstats.billiardsscraper.common.entity.PlayerTournamentStats;
import com.poolstats.billiardsscraper.common.entity.Tournament;
import com.poolstats.billiardsscraper.common.repo.PlayerRepo;
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

        @Autowired
        private PlayerRepo playerRepo;

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

        // ── Rating history reconstruction ─────────────────────────────────────────

        /**
         * Reconstructs finalRating per tournament for one player.
         *
         * Algorithm (working backwards by date DESC):
         *   finalRating[mostRecent] = player.currentRating
         *   finalRating[n-1]        = finalRating[n] - ratingChange[n]
         */
        @Override
        @Transactional
        public String recalcRatingHistoryForPlayer(Long playerId) {
                Optional<Player> playerOpt = playerRepo.findById(playerId);
                if (!playerOpt.isPresent()) {
                        log.warn("Player not found with id: {}", playerId);
                        return "Player not found with id: " + playerId;
                }
                Player player = playerOpt.get();

                List<PlayerTournamentStats> stats = statsRepo.findByPlayerId(playerId);
                if (stats.isEmpty()) {
                        log.info("No stats found for player {}", player.getFullName());
                        return "No stats found for player: " + player.getFullName();
                }

                // Sort descending by tournament date (most recent first), fallback to id
                stats.sort(Comparator
                        .<PlayerTournamentStats, LocalDate>comparing(
                                s -> s.getTournament().getDate(),
                                Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(s -> s.getTournament().getId(), Comparator.reverseOrder()));

                // Walk backwards: most recent = current rating, each older -= next ratingChange
                double runningRating = player.getRating();
                for (PlayerTournamentStats stat : stats) {
                        stat.setFinalRating(runningRating);
                        double change = stat.getRatingChange() != null ? stat.getRatingChange() : 0.0;
                        runningRating -= change;
                }

                statsRepo.saveAll(stats);
                log.info("Recalculated rating history for player {} — {} tournaments", player.getFullName(), stats.size());
                return String.format("Rating history fixed for %s (%d tournaments)", player.getFullName(), stats.size());
        }

        @Override
        @Transactional
        public String recalcRatingHistoryForAllPlayers() {
                List<Player> allPlayers = playerRepo.findAll();
                log.info("Starting rating history recalc for {} players", allPlayers.size());
                int done = 0;
                for (Player player : allPlayers) {
                        try {
                                recalcRatingHistoryForPlayer(player.getId());
                                done++;
                        } catch (Exception e) {
                                log.error("Failed to recalc for player {}: {}", player.getFullName(), e.getMessage());
                        }
                }
                String result = String.format("Rating history recalculated for %d / %d players", done, allPlayers.size());
                log.info(result);
                return result;
        }
}
