package com.poolstats.billiardsscraper.common.service;

import org.htmlunit.html.HtmlDivision;
import org.springframework.stereotype.Service;

import com.poolstats.billiardsscraper.common.entity.Club;

@Service
public interface ScraperService {

	void savePlayerWithData(HtmlDivision playerElement, int orderNumber, Club club);

	void saveClubWithData(HtmlDivision clubElement, int orderNumber);

	void syncPlayersFromWebsite();

	void syncClubsFromWebsite();

}
