package com.poolstats.billiardsscraper.common.service;

import org.springframework.stereotype.Service;

import com.poolstats.billiardsscraper.common.entity.Player;
import com.poolstats.billiardsscraper.common.entity.Team;

@Service
public interface TeamService {

	void saveTeam(Team team);

	Team getTeamByName(String playerNames);

	Player getFirstPlayerFromTeamName(String playerNames);

	Player getSecondPlayerFromTeamName(String playerNames);

}