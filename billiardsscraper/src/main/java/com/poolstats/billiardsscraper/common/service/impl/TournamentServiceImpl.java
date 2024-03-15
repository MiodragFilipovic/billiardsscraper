package com.poolstats.billiardsscraper.common.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poolstats.billiardsscraper.common.entity.Tournament;
import com.poolstats.billiardsscraper.common.repo.TournamentRepo;
import com.poolstats.billiardsscraper.common.service.TournamentService;

@Service
public class TournamentServiceImpl implements TournamentService {

	@Autowired
	private TournamentRepo tournamentRepo;

	public void saveTournament(Tournament tournament) {
		tournamentRepo.save(tournament);
	}
}