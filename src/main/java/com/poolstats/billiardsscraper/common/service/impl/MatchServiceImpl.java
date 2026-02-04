package com.poolstats.billiardsscraper.common.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlTableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poolstats.billiardsscraper.common.entity.Match;
import com.poolstats.billiardsscraper.common.entity.Player;
import com.poolstats.billiardsscraper.common.entity.Team;
import com.poolstats.billiardsscraper.common.entity.Tournament;
import com.poolstats.billiardsscraper.common.repo.MatchRepo;
import com.poolstats.billiardsscraper.common.service.MatchService;
import com.poolstats.billiardsscraper.common.service.PlayerService;
import com.poolstats.billiardsscraper.common.service.TeamService;

/**
 * Service implementation for managing match data and scraping operations.
 */
@Service
public class MatchServiceImpl implements MatchService {

	private static final Logger log = LoggerFactory.getLogger(MatchServiceImpl.class);

	@Autowired
	private MatchRepo matchRepo;

	@Autowired
	private PlayerService playerService;

	@Autowired
	private TeamService teamService;

	@Override
	public void saveMatch(Match match) {
		matchRepo.save(match);
	}

	/**
	 * Extracts match data from HTML table row and saves to database.
	 * Handles both single and team tournaments.
	 *
	 * @param matchElement HTML table row containing match information
	 * @param orderNumber position order of the match
	 * @param tournament the tournament this match belongs to
	 */
	@Override
	public void saveMatchWithData(HtmlTableRow matchElement, int orderNumber, Tournament tournament) {
		Match newMatch = new Match();

		if (matchElement.getCells().get(4).getTextContent().trim().equals(">>")) {
			return;
		}

		newMatch.setTournament(tournament);

		try {
			newMatch.setOrderNumber(Integer.parseInt(matchElement.getCells().get(0).getTextContent().trim()));
			newMatch.setDate(getMatchDateTimeFromCell(matchElement.getCells().get(1).getTextContent().trim(), tournament.getDate().getYear()));

			Player player1;
			Player player2;
			Team team1;
			Team team2;
			if (tournament.getSingle()) {
				player1 = playerService.getPlayerByName(matchElement.getCells().get(3).getTextContent().trim());
				player2 = playerService.getPlayerByName(matchElement.getCells().get(7).getTextContent().trim());
				newMatch.setPlayer1(player1);
				newMatch.setPlayer2(player2);
			} else {
				team1 = teamService.getTeamByName(matchElement.getCells().get(3).getTextContent().trim());
				team2 = teamService.getTeamByName(matchElement.getCells().get(7).getTextContent().trim());

				String teamname1 = matchElement.getCells().get(7).getVisibleText();
				String teamname2 = matchElement.getCells().get(7).getTextContent();
			}

			try {
				newMatch.setResult1(Integer.parseInt((matchElement.getCells().get(4).getTextContent().trim())));
			} catch (NumberFormatException e) {
				newMatch.setResult1(-1);
			}

			try {
				newMatch.setResult2(Integer.parseInt((matchElement.getCells().get(5).getTextContent().trim())));
			} catch (NumberFormatException e) {
				newMatch.setResult2(-1);
			}

			newMatch.setHandikap((matchElement.getCells().get(6).getTextContent().trim()));

			extractTableNumberAndVideoUrl(matchElement, newMatch);

			extractRatingChange(matchElement, newMatch);

		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}

		saveMatch(newMatch);
	}

	/**
	 * Extracts table number and video URL from the match HTML row.
	 * Table number format: "# 2" means table 2
	 * Video URL is extracted from YouTube link if present.
	 *
	 * @param matchElement the HTML table row
	 * @param match the match entity to populate
	 */
	private void extractTableNumberAndVideoUrl(HtmlTableRow matchElement, Match match) {
		try {
			int lastCellIndex = matchElement.getCells().size() - 1;
			if (lastCellIndex >= 0) {
				String lastCellText = matchElement.getCells().get(lastCellIndex).getTextContent().trim();

				if (lastCellText.contains("#")) {
					try {
						String afterHash = lastCellText.substring(lastCellText.indexOf("#") + 1).trim();
						String tableNumberStr = afterHash.split("[^0-9]")[0].trim();

						if (!tableNumberStr.isEmpty()) {
							int tableNumber = Integer.parseInt(tableNumberStr);
							match.setTableNumber(tableNumber);
							log.debug("Extracted table number: {} from text: '{}'", tableNumber, lastCellText);
						} else {
							match.setTableNumber(0);
							log.debug("No table number found in text: '{}'", lastCellText);
						}
					} catch (NumberFormatException e) {
						match.setTableNumber(0);
						log.debug("Failed to parse table number from: '{}'", lastCellText);
					}
				} else {
					match.setTableNumber(0);
					log.debug("No '#' symbol found in last cell: '{}'", lastCellText);
				}

				var anchors = matchElement.getCells().get(lastCellIndex).getElementsByTagName("a");
				for (int i = 0; i < anchors.getLength(); i++) {
					HtmlAnchor anchor = (HtmlAnchor) anchors.item(i);
					String href = anchor.getHrefAttribute();
					if (href != null && href.contains("youtube.com/watch?v=")) {
						match.setVideoLink(href);
						log.debug("Extracted video URL: {}", href);
						break;
					}
				}
			}
		} catch (Exception e) {
			log.warn("Failed to extract table number or video URL: {}", e.getMessage());
			match.setTableNumber(0);
			match.setVideoLink(null);
		}
	}

	/**
	 * Extracts rating change from the expanded match details row.
	 * The rating is found in the next sibling row with display:none containing "Rejting: X.XX"
	 *
	 * @param matchElement the main match row
	 * @param match the match entity to populate
	 */
	private void extractRatingChange(HtmlTableRow matchElement, Match match) {
		try {
			org.htmlunit.html.DomNode nextNode = matchElement.getNextSibling();
			int attempts = 0;

			while (nextNode != null && attempts < 3) {
				if (nextNode instanceof HtmlTableRow) {
					HtmlTableRow expandedRow = (HtmlTableRow) nextNode;
					String rowHtml = expandedRow.asXml();

					if (rowHtml.contains("Rejting:")) {
						int ratingIndex = rowHtml.indexOf("Rejting:");
						if (ratingIndex != -1) {
							String afterRating = rowHtml.substring(ratingIndex + 8).trim();
							String ratingStr = afterRating.split("<")[0].trim();

							try {
								double rating = Double.parseDouble(ratingStr);
								match.setRating1(rating);
								match.setRating2(-rating);
								log.debug("Extracted rating change: player1={}, player2={}", rating, -rating);
								return;
							} catch (NumberFormatException e) {
								log.debug("Could not parse rating: {}", ratingStr);
							}
						}
					}
					attempts++;
				}
				nextNode = nextNode.getNextSibling();
			}

			match.setRating1(0.0);
			match.setRating2(0.0);
		} catch (Exception e) {
			log.warn("Failed to extract rating change: {}", e.getMessage());
			match.setRating1(0.0);
			match.setRating2(0.0);
		}
	}

	/**
	 * Parses match date and time from cell content.
	 * Expected format: "dd.MM. - HH:mm"
	 *
	 * @param cellContent the cell content containing date and time
	 * @param tournamentYear the year of the tournament
	 * @return parsed LocalDateTime
	 */
	@Override
	public LocalDateTime getMatchDateTimeFromCell(String cellContent, int tournamentYear) {
		String formattedYear = String.valueOf(tournamentYear);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
		String[] parts = cellContent.split(" - ");
		String datePart = parts[0];
		String timePart = parts[1];
		String cellContentWithYear = datePart + formattedYear + " " + timePart;
		LocalDateTime dateTime = LocalDateTime.parse(cellContentWithYear, formatter);

		return dateTime;
	}
}
