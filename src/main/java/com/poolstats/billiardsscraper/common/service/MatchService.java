package com.poolstats.billiardsscraper.common.service;

import org.springframework.stereotype.Service;

import com.poolstats.billiardsscraper.common.entity.Match;

@Service
public interface MatchService {
	void saveMatch(Match match);

}