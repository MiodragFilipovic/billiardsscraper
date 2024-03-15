package com.poolstats.billiardsscraper.common.service;

import org.springframework.stereotype.Service;

import com.poolstats.billiardsscraper.common.entity.Tournament;

@Service
public interface TournamentService {

	void saveTournament(Tournament tournament);

}