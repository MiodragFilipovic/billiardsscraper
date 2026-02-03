package com.poolstats.billiardsscraper.common.entity;

import java.time.LocalDateTime;

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
@Table(name = "match")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Match {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Integer orderNumber;

	private LocalDateTime date;

	@ManyToOne
	@JoinColumn(name = "player1_id")
	private Player player1;

	@ManyToOne
	@JoinColumn(name = "player2_id")
	private Player player2;

	@ManyToOne
	@JoinColumn(name = "team1_id")
	private Team team1;

	@ManyToOne
	@JoinColumn(name = "team2_id")
	private Team team2;

	private Integer result1;

	private Integer result2;

	private String handikap;

	@ManyToOne
	private Player winner;

	private int tableNumber;

	private String videoLink;

	@ManyToOne
	@JoinColumn(name = "tournament_id")
	private Tournament tournament;
}
