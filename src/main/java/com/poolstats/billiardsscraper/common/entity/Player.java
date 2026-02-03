package com.poolstats.billiardsscraper.common.entity;

import javax.persistence.Column;
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
@Table(name = "player")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Player {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String externalId;

	@Column(nullable = false)
	private String firstName;

	@Column(nullable = false)
	private String lastName;

	@Column(nullable = false)
	private String fullName;

	private int tournamentCount;

	private String country;

	private String licenseImage;

	private double rating;

	private int wins;

	private int losses;

	private double winPercentage;

	private int tournamentsWins;

	private String imageURL;

	private String playerUrl;

	@ManyToOne
	@JoinColumn(name = "club_id")
	private Club club;
}