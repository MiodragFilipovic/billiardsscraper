package com.poolstats.billiardsscraper.common.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "player_tournament_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerTournamentStats {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "player_id")
	private Player player;

	@ManyToOne
	@JoinColumn(name = "tournament_id")
	private Tournament tournament;

	private Integer position;

	private Integer points;

	private Integer gamesWon;

	private Integer gamesLost;

	private Double gameWinPercentage;

	private Integer matchesWon;

	private Integer matchesLost;

	private Double matchWinPercentage;

	private Double ratingChange;

	private Double finalRating;
}
