package com.poolstats.billiardsscraper.common.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poolstats.billiardsscraper.common.entity.Team;
import com.poolstats.billiardsscraper.common.repo.TeamRepo;
import com.poolstats.billiardsscraper.common.service.TeamService;

@Service
public class TeamServiceImpl implements TeamService {

	@Autowired
	private TeamRepo teamRepo;

	@Override
	public void saveTeam(Team team) {
		teamRepo.save(team);

	}
}