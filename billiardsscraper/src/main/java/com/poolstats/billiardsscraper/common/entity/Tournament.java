package com.poolstats.billiardsscraper.common.entity;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tournament")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tournament {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String externalId;

	private String externalLink;

	private LocalDate date;

	@Column(nullable = false)
	private String name;

	@ManyToOne
	@JoinColumn(name = "club_id")
	private Club club;

	@OneToMany(mappedBy = "tournament")
	private List<Match> matches;

}