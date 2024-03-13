package com.poolstats.billiardsscraper.common.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "club")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Club {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String externalId;

	private String externalLink;

	@Column(nullable = false)
	private String name;

	private String clubInfo;

	private String countryFlagURL;

	private int plaуers;

	private int tournaments;

	private String imageURL;

	@OneToMany(mappedBy = "club")
	private List<Player> players;
}