package com.poolstats.billiardsscraper.common.service;

import org.htmlunit.html.HtmlDivision;
import org.htmlunit.html.HtmlTableRow;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import com.poolstats.billiardsscraper.common.entity.Club;
import com.poolstats.billiardsscraper.common.entity.Tournament;

@Service
public interface ScraperService {

	void savePlayerWithData(HtmlDivision playerElement, int orderNumber, Club club);

	void saveClubWithData(HtmlDivision clubElement, int orderNumber);

	void saveTournamentWithData(Element tournament, Club club, String year);

	void saveMatchWithData(HtmlTableRow matchElement, int orderNumber, Tournament tournament);

	void syncPlayersFromWebsite();

	void syncClubsFromWebsite();

	void syncAllTournamentsFromWebsite();

	void syncAllMatchesFromWebsite();

}
