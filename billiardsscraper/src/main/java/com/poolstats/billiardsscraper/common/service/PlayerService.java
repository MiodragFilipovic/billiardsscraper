package com.poolstats.billiardsscraper.common.service;

import org.springframework.stereotype.Service;

import com.poolstats.billiardsscraper.common.entity.Player;

@Service
public interface PlayerService {

	void savePlayer(Player player);

}