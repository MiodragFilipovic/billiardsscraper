package com.poolstats.billiardsscraper.common.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poolstats.billiardsscraper.common.entity.Club;
import com.poolstats.billiardsscraper.common.repo.ClubRepo;
import com.poolstats.billiardsscraper.common.service.ClubService;

@Service
public class ClubServiceImpl implements ClubService {

	@Autowired
	private ClubRepo clubRepo;

	@Override
	public void saveClub(Club club) {
		clubRepo.save(club);

	}
}