package com.poolstats.billiardsscraper.common.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlTable;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poolstats.billiardsscraper.common.entity.Club;
import com.poolstats.billiardsscraper.common.entity.Tournament;
import com.poolstats.billiardsscraper.common.repo.TournamentRepo;
import com.poolstats.billiardsscraper.common.service.TournamentService;

/**
 * Service implementation for managing tournament data and scraping operations.
 */
@Service
public class TournamentServiceImpl implements TournamentService {

	@Autowired
	private TournamentRepo tournamentRepo;

	@Override
	public void saveTournament(Tournament tournament) {
		tournamentRepo.save(tournament);
	}

	/**
	 * Extracts tournament data from HTML element and saves to database.
	 * Updates existing tournament or creates new one if not found.
	 *
	 * @param tournamentElement HTML element containing tournament information
	 * @param club the club this tournament belongs to
	 * @param year the year of the tournament
	 */
	@Override
	public void saveTournamentWithData(Element tournamentElement, Club club, String year) {
		String externalLinkWithoutBaseURL = tournamentElement.select("h4 a").attr("href");
		String externalId = externalLinkWithoutBaseURL.substring(externalLinkWithoutBaseURL.lastIndexOf("=") + 1);
		String name = tournamentElement.select("h4 a").text();

		String dateText = tournamentElement.select("td span").first().text();
		LocalDate tournamentDate = parseTournamentDate(dateText, year);

		String externalLink = "https://bilijar.club/" + externalLinkWithoutBaseURL;

		Optional<Tournament> existingTournamentOptional = tournamentRepo.findByExternalId(externalId);

		if (existingTournamentOptional.isPresent()) {
			Tournament existingTournament = existingTournamentOptional.get();
			existingTournament.setExternalLink(externalLink);
			existingTournament.setName(name);
			existingTournament.setDate(tournamentDate);
			existingTournament.setClub(club);
			saveTournament(existingTournament);
		} else {
			Tournament newTournament = new Tournament();
			newTournament.setExternalId(externalId);
			newTournament.setExternalLink(externalLink);
			newTournament.setName(name);
			newTournament.setDate(tournamentDate);
			newTournament.setClub(club);
			saveTournament(newTournament);
		}
	}

	/**
	 * Parses tournament date from text format.
	 * Expected format: "dd.MMM" (e.g., "15.Dec")
	 *
	 * @param dateText the date text to parse
	 * @param year the year to append
	 * @return parsed LocalDate
	 */
	@Override
	public LocalDate parseTournamentDate(String dateText, String year) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MMM.yyyy", Locale.ENGLISH);
		String fullDateText = dateText + "." + year;
		return LocalDate.parse(fullDateText, formatter);
	}

	/**
	 * Updates tournament with additional data from the tournament page.
	 * Extracts schema and tournament type (single or team).
	 *
	 * @param tournament the tournament to update
	 * @param page the HTML page containing tournament details
	 */
	@Override
	public void updateTournamentData(Tournament tournament, HtmlPage page) {
		HtmlTable tournamentTable = (HtmlTable) page.getElementById("turnir");
		String schema = tournamentTable.getCellAt(1, 1).getTextContent().trim();
		String type = tournamentTable.getCellAt(1, 4).getTextContent().trim();
		tournament.setSchema(schema);
		tournament.setSingle(!type.equals("Parovi"));
	}
}

