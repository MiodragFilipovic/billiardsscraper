package com.poolstats.billiardsscraper.common.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlunit.BrowserVersion;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlDivision;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlParagraph;
import org.htmlunit.html.HtmlTableRow;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.poolstats.billiardsscraper.common.entity.Club;
import com.poolstats.billiardsscraper.common.entity.Player;
import com.poolstats.billiardsscraper.common.entity.Tournament;
import com.poolstats.billiardsscraper.common.repo.ClubRepo;
import com.poolstats.billiardsscraper.common.repo.PlayerRepo;
import com.poolstats.billiardsscraper.common.repo.TournamentRepo;
import com.poolstats.billiardsscraper.common.service.ClubService;
import com.poolstats.billiardsscraper.common.service.MatchService;
import com.poolstats.billiardsscraper.common.service.PlayerService;
import com.poolstats.billiardsscraper.common.service.PlayerTournamentStatsService;
import com.poolstats.billiardsscraper.common.service.ScraperService;
import com.poolstats.billiardsscraper.common.service.TournamentService;

/**
 * Service implementation for orchestrating web scraping operations.
 * Delegates domain-specific logic to appropriate services.
 */
@Service
public class ScraperServiceImpl implements ScraperService {

	private static final Logger log = LoggerFactory.getLogger(ScraperServiceImpl.class);

	public static final String[] TOURNAMENT_YEARS = { "2026","2025", "2024", "2023", "2022", "2021", "2020", "2019", "2018", "2017", "2016" };

	public static final String INDEPENDEND_CLUB_EXTERNAL_ID = "0";

	private static final Pattern WINS_PATTERN   = Pattern.compile("Pobeda\\s*(\\d+)");
	private static final Pattern LOSSES_PATTERN = Pattern.compile("Poraza\\s*(\\d+)");

	@Autowired
	private PlayerService playerService;

	@Autowired
	private ClubService clubService;

	@Autowired
	private ClubRepo clubRepo;

	@Autowired
	private PlayerRepo playerRepo;

	@Autowired
	private TournamentService tournamentService;

	@Autowired
	private MatchService matchService;

	@Autowired
	private PlayerTournamentStatsService playerTournamentStatsService;

	@Autowired
	private TournamentRepo tournamentRepo;

	@Autowired
	private RestTemplate restTemplate;

	private WebClient buildWebClient() {
		WebClient webClient = new WebClient(BrowserVersion.CHROME);
		webClient.getOptions().setJavaScriptEnabled(true);
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setPrintContentOnFailingStatusCode(false);
		webClient.getOptions().setCssEnabled(false);
		webClient.setIncorrectnessListener((message, origin) -> { /* suppress */ });
		webClient.setJavaScriptErrorListener(new SilentJavaScriptErrorListener());
		return webClient;
	}

	/**
	 * Synchronizes all players from the website for all clubs.
	 * Scrapes player data and delegates to PlayerService for processing.
	 */
	@Override
	public void syncPlayersFromWebsite() {
		log.info("=== Starting player synchronization ===");
		long startTime = System.currentTimeMillis();
		int totalPlayersProcessed = 0;

		try {
			WebClient webClient = buildWebClient();

			List<Club> clubs = clubRepo.findAll();
			log.info("Found {} clubs to process", clubs.size());

			for (Club club : clubs) {
				log.info("Processing club: {} (ID: {})", club.getName(), club.getExternalId());

				HtmlPage page = webClient.getPage("https://bilijar.club/poklubovima.php?Club_ID=" + club.getExternalId());
				List<HtmlDivision> players = page.getByXPath("//div[contains(@class, 'col-sm-6 col-md-3 col-xs-6')]");

				log.info("  Found {} players for club: {}", players.size(), club.getName());

				int orderNumber = 0;
				for (HtmlDivision playerElement : players) {
					orderNumber++;
					playerService.savePlayerWithData(playerElement, orderNumber, club);
					totalPlayersProcessed++;

					if (orderNumber % 10 == 0) {
						log.debug("  Processed {}/{} players for club: {}", orderNumber, players.size(), club.getName());
					}
				}
				log.info("  Completed club: {} - processed {} players", club.getName(), players.size());
			}

		} catch (IOException e) {
			log.error("Error during player synchronization", e);
			e.printStackTrace();
		}

		long endTime = System.currentTimeMillis();
		long executionTime = endTime - startTime;
		double executionTimeInSeconds = executionTime / 1000.0;
		log.info("=== Player synchronization completed ===");
		log.info("Total players processed: {}", totalPlayersProcessed);
		log.info("Execution time: {} seconds ({} minutes)",
			String.format("%.2f", executionTimeInSeconds),
			String.format("%.2f", executionTimeInSeconds / 60.0));
	}

	/**
	 * Synchronizes all clubs from the website.
	 * Scrapes club data and delegates to ClubService for processing.
	 */
	@Override
	public void syncClubsFromWebsite() {
		log.info("=== Starting club synchronization ===");
		long startTime = System.currentTimeMillis();

		try {
			WebClient webClient = buildWebClient();
			

			log.info("Fetching clubs from website...");
			HtmlPage page = webClient.getPage("https://bilijar.club/klubovi.php");
			List<HtmlDivision> clubs = page.getByXPath("//div[contains(@class, 'col-sm-6 col-md-3')]");

			log.info("Found {} clubs to process", clubs.size());

			int orderNumber = 0;
			for (HtmlDivision clubElement : clubs) {
				orderNumber++;
				clubService.saveClubWithData(clubElement, orderNumber);

				if (orderNumber % 5 == 0) {
					log.debug("Processed {}/{} clubs", orderNumber, clubs.size());
				}
			}

			log.info("Creating independent club for players without clubs...");
			clubService.createIndependentClub();

		} catch (IOException e) {
			log.error("Error during club synchronization", e);
			e.printStackTrace();
		}

		long endTime = System.currentTimeMillis();
		long executionTime = endTime - startTime;
		double executionTimeInSeconds = executionTime / 1000.0;
		log.info("=== Club synchronization completed ===");
		log.info("Execution time: {} seconds ({} minutes)",
			String.format("%.2f", executionTimeInSeconds),
			String.format("%.2f", executionTimeInSeconds / 60.0));
	}

	/**
	 * Synchronizes all tournaments from the website for all clubs and years.
	 * Scrapes tournament data and delegates to TournamentService for processing.
	 */
	@Override
	public void syncAllTournamentsFromWebsite() {
		log.info("=== Starting tournament synchronization for all years ===");
		long startTime = System.currentTimeMillis();
		int totalTournamentsProcessed = 0;

		List<Club> clubs = clubRepo.findAll();
		log.info("Processing tournaments for {} clubs", clubs.size());

		for (Club club : clubs) {
			if (club.getExternalId().equals(INDEPENDEND_CLUB_EXTERNAL_ID)) {
				log.debug("Skipping independent club");
				continue;
			}

			log.info("Processing tournaments for club: {} (ID: {})", club.getName(), club.getExternalId());
			int clubTournamentCount = 0;

			for (String year : TOURNAMENT_YEARS) {
				log.debug("  Fetching tournaments for year: {}", year);
				boolean endOfYear = false;
				int pagiantionCount = 0;

				while (!endOfYear) {
					MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
					body.add("getresult", String.valueOf(pagiantionCount));
					body.add("club", club.getExternalId());
					body.add("year", year);

					String result = restTemplate.postForObject("https://bilijar.club/fetch.php", body, String.class);

					if (result != null && !result.isEmpty()) {
						Document doc = Jsoup.parse("<table>" + result + "</table>");
						Elements tournaments = doc.select("tr");

						for (Element tournament : tournaments) {
							tournamentService.saveTournamentWithData(tournament, club, year);
							totalTournamentsProcessed++;
							clubTournamentCount++;
						}

						log.debug("    Processed {} tournaments from pagination offset {}", tournaments.size(), pagiantionCount);
						pagiantionCount = pagiantionCount + 10;
					} else {
						endOfYear = true;
					}
				}
			}
			log.info("  Completed club: {} - processed {} tournaments", club.getName(), clubTournamentCount);
		}

		long endTime = System.currentTimeMillis();
		long executionTime = endTime - startTime;
		double executionTimeInSeconds = executionTime / 1000.0;
		log.info("=== Tournament synchronization completed ===");
		log.info("Total tournaments processed: {}", totalTournamentsProcessed);
		log.info("Execution time: {} seconds ({} minutes)",
			String.format("%.2f", executionTimeInSeconds),
			String.format("%.2f", executionTimeInSeconds / 60.0));
	}

	/**
	 * Synchronizes all matches from the website for all tournaments.
	 * Scrapes match data and delegates to MatchService for processing.
	 */
	@Override
	public void syncAllMatchesFromWebsite() {
		log.info("=== Starting match synchronization for all tournaments ===");
		long startTime = System.currentTimeMillis();
		int totalMatchesProcessed = 0;

		try {
			WebClient webClient = buildWebClient();
			

			List<Tournament> tournaments = tournamentRepo.findAll();
			log.info("Found {} tournaments to process", tournaments.size());

			int tournamentCounter = 0;
			for (Tournament tournament : tournaments) {
				tournamentCounter++;
				log.info("[{}/{}] Processing tournament: {} (ID: {})",
					tournamentCounter, tournaments.size(), tournament.getName(), tournament.getExternalId());

				HtmlPage page = webClient.getPage("https://bilijar.club/tournament.php?ID=" + tournament.getExternalId());

				tournamentService.updateTournamentData(tournament, page);

				List<HtmlTableRow> matches = page.getByXPath("//tr[contains(@class, 'size-11')]");
				log.info("  Found {} matches for tournament: {}", matches.size(), tournament.getName());

				int orderNumberMatch = 0;
				for (HtmlTableRow matchElement : matches) {
					orderNumberMatch++;
					matchService.saveMatchWithData(matchElement, orderNumberMatch, tournament);
					totalMatchesProcessed++;

					if (orderNumberMatch % 20 == 0) {
						log.debug("  Processed {}/{} matches", orderNumberMatch, matches.size());
					}
				}
				log.info("  Completed tournament: {} - processed {} matches", tournament.getName(), matches.size());
			}

		} catch (IOException e) {
			log.error("Error during match synchronization", e);
			e.printStackTrace();
		}

		long endTime = System.currentTimeMillis();
		long executionTime = endTime - startTime;
		double executionTimeInMinutes = executionTime / 60000.0;
		log.info("=== Match synchronization completed ===");
		log.info("Total matches processed: {}", totalMatchesProcessed);
		log.info("Execution time: {:.2f} minutes", executionTimeInMinutes);
	}

	/**
	 * Synchronizes tournaments for a specific year only.
	 *
	 * @param year the year to sync tournaments for
	 */
	@Override
	public void syncTournamentsForYear(String year) {
		log.info("=== Starting tournament synchronization for year: {} ===", year);
		long startTime = System.currentTimeMillis();
		int totalTournamentsProcessed = 0;

		List<Club> clubs = clubRepo.findAll();
		log.info("Processing tournaments for {} clubs", clubs.size());

		for (Club club : clubs) {
			if (club.getExternalId().equals(INDEPENDEND_CLUB_EXTERNAL_ID)) {
				continue;
			}

			log.info("Processing tournaments for club: {} (ID: {})", club.getName(), club.getExternalId());
			int clubTournamentCount = 0;

			boolean endOfYear = false;
			int pagiantionCount = 0;

			while (!endOfYear) {
				MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
				body.add("getresult", String.valueOf(pagiantionCount));
				body.add("club", club.getExternalId());
				body.add("year", year);

				String result = restTemplate.postForObject("https://bilijar.club/fetch.php", body, String.class);

				if (result != null && !result.isEmpty()) {
					Document doc = Jsoup.parse("<table>" + result + "</table>");
					Elements tournaments = doc.select("tr");

					for (Element tournament : tournaments) {
						tournamentService.saveTournamentWithData(tournament, club, year);
						totalTournamentsProcessed++;
						clubTournamentCount++;
					}

					log.debug("  Processed {} tournaments from pagination offset {}", tournaments.size(), pagiantionCount);
					pagiantionCount = pagiantionCount + 10;
				} else {
					endOfYear = true;
				}
			}
			log.info("  Completed club: {} - processed {} tournaments", club.getName(), clubTournamentCount);
		}

		long endTime = System.currentTimeMillis();
		long executionTime = endTime - startTime;
		double executionTimeInMinutes = executionTime / 60000.0;
		log.info("=== Tournament synchronization for year {} completed ===", year);
		log.info("Total tournaments processed: {}", totalTournamentsProcessed);
		log.info("Execution time: {:.2f} minutes", executionTimeInMinutes);
	}

	/**
	 * Synchronizes only the most recent tournaments (current year).
	 */
	@Override
	public void syncLatestTournaments() {
		String currentYear = String.valueOf(java.time.Year.now().getValue());
		log.info("=== Syncing latest tournaments for current year: {} ===", currentYear);
		syncTournamentsForYear(currentYear);
	}

	/**
	 * Synchronizes matches only for the most recent tournament.
	 */
	@Override
	public void syncMatchesForLatestTournament() {
		log.info("=== Starting match synchronization for latest tournament ===");
		long startTime = System.currentTimeMillis();
		Tournament latestTournament = null;

		try {
			WebClient webClient = buildWebClient();
			

			latestTournament = tournamentRepo.findTopByOrderByDateDesc();

			if (latestTournament == null) {
				log.warn("No tournaments found in database");
				return;
			}

			log.info("Processing latest tournament: {} (Date: {}, ID: {})",
				latestTournament.getName(), latestTournament.getDate(), latestTournament.getExternalId());

			HtmlPage page = webClient.getPage("https://bilijar.club/tournament.php?ID=" + latestTournament.getExternalId());

			tournamentService.updateTournamentData(latestTournament, page);

			List<HtmlTableRow> matches = page.getByXPath("//tr[contains(@class, 'size-11')]");
			log.info("Found {} matches for tournament: {}", matches.size(), latestTournament.getName());

			int orderNumberMatch = 0;
			for (HtmlTableRow matchElement : matches) {
				orderNumberMatch++;
				matchService.saveMatchWithData(matchElement, orderNumberMatch, latestTournament);

				if (orderNumberMatch % 10 == 0) {
					log.debug("Processed {}/{} matches", orderNumberMatch, matches.size());
				}
			}
			log.info("Completed processing {} matches", matches.size());

		} catch (IOException e) {
			log.error("Error during match synchronization for latest tournament", e);
			e.printStackTrace();
		}

		long endTime = System.currentTimeMillis();
		long executionTime = endTime - startTime;
		double executionTimeInSeconds = executionTime / 1000.0;
		log.info("=== Match synchronization for latest tournament completed ===");
		log.info("Execution time: {} seconds ({} minutes)",
			String.format("%.2f", executionTimeInSeconds),
			String.format("%.2f", executionTimeInSeconds / 60.0));

		if (latestTournament != null) {
			log.info("Now syncing player tournament stats...");
			syncPlayerStatsForTournament(latestTournament.getExternalId());
		}
	}

	/**
	 * Synchronizes matches for a specific tournament by external ID.
	 *
	 * @param tournamentExternalId the external ID of the tournament
	 */
	@Override
	public void syncMatchesForTournament(String tournamentExternalId) {
		log.info("=== Starting match synchronization for tournament with external ID: {} ===", tournamentExternalId);
		long startTime = System.currentTimeMillis();
		int totalMatchesProcessed = 0;

		try {
			WebClient webClient = buildWebClient();
			

			Tournament tournament = tournamentRepo.findByExternalId(tournamentExternalId)
				.orElse(null);

			if (tournament == null) {
				log.warn("Tournament with external ID '{}' not found in database", tournamentExternalId);
				return;
			}

			log.info("Processing tournament: {} (Date: {}, ID: {})",
				tournament.getName(), tournament.getDate(), tournament.getExternalId());

			HtmlPage page = webClient.getPage("https://bilijar.club/tournament.php?ID=" + tournament.getExternalId());

			tournamentService.updateTournamentData(tournament, page);

			List<HtmlTableRow> matches = page.getByXPath("//tr[contains(@class, 'size-11')]");
			log.info("Found {} matches for tournament: {}", matches.size(), tournament.getName());

			int orderNumberMatch = 0;
			for (HtmlTableRow matchElement : matches) {
				orderNumberMatch++;
				matchService.saveMatchWithData(matchElement, orderNumberMatch, tournament);
				totalMatchesProcessed++;

				if (orderNumberMatch % 10 == 0) {
					log.debug("Processed {}/{} matches", orderNumberMatch, matches.size());
				}
			}
			log.info("Completed processing {} matches for tournament: {}", matches.size(), tournament.getName());

		} catch (IOException e) {
			log.error("Error during match synchronization for tournament: {}", tournamentExternalId, e);
			e.printStackTrace();
		}

		long endTime = System.currentTimeMillis();
		long executionTime = endTime - startTime;
		double executionTimeInSeconds = executionTime / 1000.0;
		log.info("=== Match synchronization for tournament completed ===");
		log.info("Total matches processed: {}", totalMatchesProcessed);
		log.info("Execution time: {} seconds ({} minutes)",
			String.format("%.2f", executionTimeInSeconds),
			String.format("%.2f", executionTimeInSeconds / 60.0));

		log.info("Now syncing player tournament stats...");
		syncPlayerStatsForTournament(tournamentExternalId);
	}

	@Override
	public void syncPlayerStatsForTournament(String tournamentExternalId) {
		log.info("=== Starting player stats synchronization for tournament: {} ===", tournamentExternalId);
		long startTime = System.currentTimeMillis();
		int totalStatsProcessed = 0;

		try {
			WebClient webClient = buildWebClient();
			

			Tournament tournament = tournamentRepo.findByExternalId(tournamentExternalId)
				.orElse(null);

			if (tournament == null) {
				log.warn("Tournament with external ID '{}' not found in database", tournamentExternalId);
				return;
			}

			log.info("Processing player stats for tournament: {} (Date: {}, ID: {})",
				tournament.getName(), tournament.getDate(), tournament.getExternalId());

			HtmlPage page = webClient.getPage("https://bilijar.club/tournament.php?ID=" + tournament.getExternalId());

			List<HtmlTableRow> statsRows = page.getByXPath("//table[@id='results2']/tbody/tr[position()>1]");
			log.info("Found {} player stats rows", statsRows.size());

			for (HtmlTableRow row : statsRows) {
				playerTournamentStatsService.saveStatsFromTableRow(row, tournament);
				totalStatsProcessed++;

				if (totalStatsProcessed % 5 == 0) {
					log.debug("Processed {}/{} player stats", totalStatsProcessed, statsRows.size());
				}
			}

			log.info("Completed processing {} player stats", totalStatsProcessed);

		} catch (IOException e) {
			log.error("Error during player stats synchronization for tournament: {}", tournamentExternalId, e);
			e.printStackTrace();
		}

		long endTime = System.currentTimeMillis();
		long executionTime = endTime - startTime;
		double executionTimeInSeconds = executionTime / 1000.0;
		log.info("=== Player stats synchronization completed ===");
		log.info("Total stats processed: {}", totalStatsProcessed);
		log.info("Execution time: {} seconds ({} minutes)",
			String.format("%.2f", executionTimeInSeconds),
			String.format("%.2f", executionTimeInSeconds / 60.0));
	}

	/**
	 * Synchronizes matches and player stats for all tournaments that exist in the database.
	 * Iterates through every tournament in DB and calls syncMatchesForTournament for each.
	 */
	@Override
	public void syncMatchesForAllDbTournaments() {
		log.info("=== Starting match synchronization for all tournaments in DB ===");
		long startTime = System.currentTimeMillis();

		List<Tournament> tournaments = tournamentRepo.findAllByOrderByDateDesc();
		log.info("Found {} tournaments in database to process", tournaments.size());

		int counter = 0;
		for (Tournament tournament : tournaments) {
			counter++;
			log.info("[{}/{}] Processing tournament: {} (ExternalId: {})",
				counter, tournaments.size(), tournament.getName(), tournament.getExternalId());
			try {
				syncMatchesForTournament(tournament.getExternalId());
			} catch (Exception e) {
				log.error("Failed to sync matches for tournament {} ({}): {}",
					tournament.getName(), tournament.getExternalId(), e.getMessage());
			}
		}

		long endTime = System.currentTimeMillis();
		double executionTimeInMinutes = (endTime - startTime) / 60000.0;
		log.info("=== Match synchronization for all DB tournaments completed ===");
		log.info("Processed {} tournaments. Execution time: {} minutes",
			counter, String.format("%.2f", executionTimeInMinutes));
	}

	/**
	 * Syncs wins and losses for a single player by visiting their individual profile page.
	 */
	@Override
	public String syncWinsLossesForPlayer(Long playerId) {
		log.info("=== Syncing wins/losses for player id={} ===", playerId);

		Optional<Player> playerOpt = playerRepo.findById(playerId);
		if (playerOpt.isEmpty()) {
			return "Player with id=" + playerId + " not found";
		}

		Player player = playerOpt.get();
		String playerUrl = player.getPlayerUrl();

		if (playerUrl == null || playerUrl.isBlank()) {
			return "Player " + player.getFullName() + " has no playerUrl set";
		}

		try {
			WebClient webClient = buildWebClient();
			

			HtmlPage page = webClient.getPage(playerUrl);

			List<HtmlParagraph> paragraphs = page.getByXPath("//p[contains(., 'Pobeda') or contains(., 'Poraza')]");

			int wins = 0, losses = 0;
			for (HtmlParagraph p : paragraphs) {
				String text = p.asNormalizedText();
				Matcher winMatcher  = WINS_PATTERN.matcher(text);
				Matcher lossMatcher = LOSSES_PATTERN.matcher(text);
				if (winMatcher.find())  wins   = Integer.parseInt(winMatcher.group(1));
				if (lossMatcher.find()) losses = Integer.parseInt(lossMatcher.group(1));
				break;
			}

			double winPct = (wins + losses > 0) ? (double) wins / (wins + losses) * 100.0 : 0.0;

			player.setWins(wins);
			player.setLosses(losses);
			player.setWinPercentage(winPct);
			playerRepo.save(player);

			String msg = String.format("Updated %s: wins=%d, losses=%d, winPct=%.1f%%",
				player.getFullName(), wins, losses, winPct);
			log.info(msg);
			return msg;

		} catch (IOException e) {
			log.error("Error syncing wins/losses for player id={}", playerId, e);
			return "Error: " + e.getMessage();
		}
	}

	/**
	 * Syncs wins and losses for ALL players by visiting each player's individual profile page.
	 */
	@Override
	public String syncWinsLossesForAllPlayers() {
		log.info("=== Syncing wins/losses for ALL players ===");
		long startTime = System.currentTimeMillis();

		List<Player> players = playerRepo.findAll();
		log.info("Found {} players to process", players.size());

		int success = 0, failed = 0, skipped = 0;

		try {
			WebClient webClient = buildWebClient();
			

			for (Player player : players) {
				String playerUrl = player.getPlayerUrl();
				if (playerUrl == null || playerUrl.isBlank()) {
					skipped++;
					continue;
				}

				try {
					HtmlPage page = webClient.getPage(playerUrl);
					List<HtmlParagraph> paragraphs = page.getByXPath("//p[contains(., 'Pobeda') or contains(., 'Poraza')]");

					int wins = 0, losses = 0;
					for (HtmlParagraph p : paragraphs) {
						String text = p.asNormalizedText();
						Matcher winMatcher  = WINS_PATTERN.matcher(text);
						Matcher lossMatcher = LOSSES_PATTERN.matcher(text);
						if (winMatcher.find())  wins   = Integer.parseInt(winMatcher.group(1));
						if (lossMatcher.find()) losses = Integer.parseInt(lossMatcher.group(1));
						break;
					}

					double winPct = (wins + losses > 0) ? (double) wins / (wins + losses) * 100.0 : 0.0;
					player.setWins(wins);
					player.setLosses(losses);
					player.setWinPercentage(winPct);
					playerRepo.save(player);
					success++;

					log.debug("Updated {}: wins={} losses={}", player.getFullName(), wins, losses);

				} catch (Exception e) {
					log.error("Failed to sync player {}: {}", player.getFullName(), e.getMessage());
					failed++;
				}
			}

		} catch (Exception e) {
			log.error("Error initializing WebClient for all-players sync", e);
			return "Failed: " + e.getMessage();
		}

		double elapsed = (System.currentTimeMillis() - startTime) / 1000.0;
		String result = String.format("Done in %.1fs: %d updated, %d failed, %d skipped (no URL)",
			elapsed, success, failed, skipped);
		log.info(result);
		return result;
	}

}
