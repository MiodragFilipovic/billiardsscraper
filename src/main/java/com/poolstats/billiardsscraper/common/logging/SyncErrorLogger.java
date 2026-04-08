package com.poolstats.billiardsscraper.common.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Centralised logger for sync-time business errors (missing entities, duplicates, etc.).
 * All messages are written to the dedicated "sync-errors" logger which is routed
 * to logs/sync-errors.log via logback-spring.xml.
 */
@Component
public class SyncErrorLogger {

    private static final Logger syncLog = LoggerFactory.getLogger("SYNC_ERRORS");

    /** Player was not found in DB by name. */
    public void playerNotFound(String playerName, String context) {
        syncLog.error("[PLAYER_NOT_FOUND] name='{}' context='{}'", playerName, context);
    }

    /** Multiple players share the same full name. */
    public void duplicatePlayers(String playerName, int count, String context) {
        syncLog.error("[DUPLICATE_PLAYER] name='{}' count={} context='{}'", playerName, count, context);
    }

    /** Tournament was not found in DB. */
    public void tournamentNotFound(String externalId) {
        syncLog.error("[TOURNAMENT_NOT_FOUND] externalId='{}'", externalId);
    }

    /** Generic data-quality warning during sync. */
    public void warn(String message) {
        syncLog.warn("[SYNC_WARN] {}", message);
    }

    /** Generic error during sync. */
    public void error(String message) {
        syncLog.error("[SYNC_ERROR] {}", message);
    }
}

