package common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import common.entity.Player;
import common.repo.PlayerRepo;

@Service
public class PlayerService {
	@Autowired
	private PlayerRepo playerRepo;

	public void savePlayer(Player player) {
		playerRepo.save(player);
	}
}