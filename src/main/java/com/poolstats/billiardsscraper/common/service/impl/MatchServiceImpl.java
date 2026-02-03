package com.poolstats.billiardsscraper.common.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poolstats.billiardsscraper.common.entity.Match;
import com.poolstats.billiardsscraper.common.repo.MatchRepo;
import com.poolstats.billiardsscraper.common.service.MatchService;

@Service
public class MatchServiceImpl implements MatchService {

	@Autowired
	private MatchRepo matchRepo;

	@Override
	public void saveMatch(Match match) {
		matchRepo.save(match);
	}

}
