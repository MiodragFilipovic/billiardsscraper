package com.poolstats.billiardsscraper.common.service;

import org.htmlunit.html.HtmlTableRow;
import org.springframework.stereotype.Service;

import com.poolstats.billiardsscraper.common.entity.Tournament;

@Service
public interface PlayerTournamentStatsService {

	void saveStatsFromTableRow(HtmlTableRow row, Tournament tournament);
}
