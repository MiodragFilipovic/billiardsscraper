package com.poolstats.billiardsscraper.common.dto;

/**
 * DTO representing head-to-head statistics between a player and one specific opponent.
 */
public class H2HRecord {

    private Long opponentId;
    private String opponentName;
    private String opponentClub;
    private int totalMatches;
    private int wins;
    private int losses;
    private double winPercentage;

    public H2HRecord(Long opponentId, String opponentName, String opponentClub) {
        this.opponentId = opponentId;
        this.opponentName = opponentName;
        this.opponentClub = opponentClub;
    }

    public void addMatch(boolean won) {
        totalMatches++;
        if (won) wins++;
        else losses++;
        winPercentage = totalMatches > 0 ? (wins * 100.0 / totalMatches) : 0;
    }

    public Long getOpponentId() { return opponentId; }
    public String getOpponentName() { return opponentName; }
    public String getOpponentClub() { return opponentClub; }
    public int getTotalMatches() { return totalMatches; }
    public int getWins() { return wins; }
    public int getLosses() { return losses; }
    public double getWinPercentage() { return winPercentage; }
}

