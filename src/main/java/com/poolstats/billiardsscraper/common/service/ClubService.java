package com.poolstats.billiardsscraper.common.service;

import org.htmlunit.html.HtmlDivision;
import org.springframework.stereotype.Service;

import com.poolstats.billiardsscraper.common.entity.Club;

@Service
public interface ClubService {

	void saveClub(Club club);

	void saveClubWithData(HtmlDivision clubElement, int orderNumber);

	void createIndependentClub();

}