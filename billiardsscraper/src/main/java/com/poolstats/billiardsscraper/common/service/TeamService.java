package com.poolstats.billiardsscraper.common.service;

import org.springframework.stereotype.Service;

import com.poolstats.billiardsscraper.common.entity.Team;

@Service
public interface TeamService {

	void saveTeam(Team team);

}