package com.poolstats.billiardsscraper.common.service;

import org.springframework.stereotype.Service;

@Service
public interface ScraperService {

	void syncPlayersFromWebsite();

	void syncClubsFromWebsite();

	void syncAllTournamentsFromWebsite();

	void syncTournamentsForYear(String year);

	void syncLatestTournaments();

	void syncAllMatchesFromWebsite();

	void syncMatchesForLatestTournament();

	void syncMatchesForTournament(String tournamentExternalId);

	void syncPlayerStatsForTournament(String tournamentExternalId);

}
