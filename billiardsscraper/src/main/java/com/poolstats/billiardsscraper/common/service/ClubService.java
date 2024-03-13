package com.poolstats.billiardsscraper.common.service;

import org.springframework.stereotype.Service;

import com.poolstats.billiardsscraper.common.entity.Club;

@Service
public interface ClubService {

	void saveClub(Club club);

}