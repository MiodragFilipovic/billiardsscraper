package com.poolstats.billiardsscraper.common.service;

import org.htmlunit.html.HtmlTableRow;
import org.springframework.stereotype.Service;

import com.poolstats.billiardsscraper.common.entity.Tournament;

@Service
public interface PlayerTournamentStatsService {

        void saveStatsFromTableRow(HtmlTableRow row, Tournament tournament);

        /**
         * Reconstructs the finalRating timeline for a single player.
         * Starts from the player's current rating at the most recent tournament
         * and works backwards: finalRating[prev] = finalRating[next] - ratingChange[next]
         */
        String recalcRatingHistoryForPlayer(Long playerId);

        /**
         * Runs recalcRatingHistoryForPlayer for every player in the database.
         */
        String recalcRatingHistoryForAllPlayers();
}
