package com.poolstats.billiardsscraper.common.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poolstats.billiardsscraper.common.entity.Player;
import com.poolstats.billiardsscraper.common.entity.Team;
import com.poolstats.billiardsscraper.common.repo.TeamRepo;
import com.poolstats.billiardsscraper.common.service.TeamService;

/**
 * Service implementation for managing team data and operations.
 */
@Service
public class TeamServiceImpl implements TeamService {

	@Autowired
	private TeamRepo teamRepo;

	@Override
	public void saveTeam(Team team) {
		teamRepo.save(team);
	}

	/**
	 * Creates or retrieves a team by parsing player names from team name string.
	 *
	 * @param playerNames the team name containing player names
	 * @return the saved team
	 */
	@Override
	public Team getTeamByName(String playerNames) {
		Team team = new Team();
		team.setPlayer1(getFirstPlayerFromTeamName(playerNames));
		team.setPlayer2(getSecondPlayerFromTeamName(playerNames));
		return teamRepo.save(team);
	}

	/**
	 * Extracts the first player from a team name string.
	 * TODO: Implement player name parsing logic.
	 *
	 * @param playerNames the team name containing player names
	 * @return the first player, or null if not found
	 */
	@Override
	public Player getFirstPlayerFromTeamName(String playerNames) {
		return null;
	}

	/**
	 * Extracts the second player from a team name string.
	 * TODO: Implement player name parsing logic.
	 *
	 * @param playerNames the team name containing player names
	 * @return the second player, or null if not found
	 */
	@Override
	public Player getSecondPlayerFromTeamName(String playerNames) {
		return null;
	}
}

