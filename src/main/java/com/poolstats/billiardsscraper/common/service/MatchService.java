package com.poolstats.billiardsscraper.common.service;

import java.time.LocalDateTime;

import org.htmlunit.html.HtmlTableRow;
import org.springframework.stereotype.Service;

import com.poolstats.billiardsscraper.common.entity.Match;
import com.poolstats.billiardsscraper.common.entity.Tournament;

@Service
public interface MatchService {

	void saveMatch(Match match);

	void saveMatchWithData(HtmlTableRow matchElement, int orderNumber, Tournament tournament);

	LocalDateTime getMatchDateTimeFromCell(String cellContent, int tournamentYear);

}