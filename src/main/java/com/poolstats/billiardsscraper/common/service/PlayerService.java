package com.poolstats.billiardsscraper.common.service;

import org.htmlunit.html.HtmlDivision;
import org.springframework.stereotype.Service;

import com.poolstats.billiardsscraper.common.entity.Club;
import com.poolstats.billiardsscraper.common.entity.Player;

@Service
public interface PlayerService {

	void savePlayer(Player player);

	void savePlayerWithData(HtmlDivision playerElement, int orderNumber, Club club);

	Player getPlayerByName(String fullName);

}