package com.poolstats.billiardsscraper.common.service;

import java.time.LocalDate;

import org.htmlunit.html.HtmlPage;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import com.poolstats.billiardsscraper.common.entity.Club;
import com.poolstats.billiardsscraper.common.entity.Tournament;

@Service
public interface TournamentService {

	void saveTournament(Tournament tournament);

	void saveTournamentWithData(Element tournamentElement, Club club, String year);

	LocalDate parseTournamentDate(String dateText, String year);

	void updateTournamentData(Tournament tournament, HtmlPage page);

}