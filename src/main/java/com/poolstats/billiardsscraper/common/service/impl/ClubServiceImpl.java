package com.poolstats.billiardsscraper.common.service.impl;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlDivision;
import org.htmlunit.html.HtmlHeading4;
import org.htmlunit.html.HtmlImage;
import org.htmlunit.html.HtmlParagraph;
import org.htmlunit.html.HtmlSpan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poolstats.billiardsscraper.common.entity.Club;
import com.poolstats.billiardsscraper.common.repo.ClubRepo;
import com.poolstats.billiardsscraper.common.service.ClubService;

/**
 * Service implementation for managing club data and scraping operations.
 */
@Service
public class ClubServiceImpl implements ClubService {

	public static final String INDEPENDEND_CLUB_EXTERNAL_ID = "0";

	private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+)");

	@Autowired
	private ClubRepo clubRepo;

	@Override
	public void saveClub(Club club) {
		clubRepo.save(club);
	}

	/**
	 * Extracts club data from HTML element and saves to database.
	 * Updates existing club or creates new one if not found.
	 *
	 * @param clubElement HTML division containing club information
	 * @param orderNumber position order of the club
	 */
	@Override
	public void saveClubWithData(HtmlDivision clubElement, int orderNumber) {
		try {
			HtmlAnchor clubLink = clubElement.getFirstByXPath(".//a");
			HtmlImage clubImage = clubElement.getFirstByXPath(".//img");
			HtmlHeading4 clubNameElement = clubElement.getFirstByXPath(".//h4[contains(@class, 'nomargin')]");
			HtmlParagraph clubInfo = clubElement.getFirstByXPath(".//p");

			if (clubLink != null && clubNameElement != null) {
				String name = clubNameElement.getTextContent().trim();
				String externalLink = "https://bilijar.club/" + clubLink.getAttribute("href");
				String[] parts = externalLink.split("ID=");
				String externalId = parts.length > 1 ? parts[1] : "";

				HtmlImage countryFlagImage = clubInfo.getFirstByXPath(".//img[contains(@src, 'images/flags/')]");

				String countryFlagURL = "";

				if (countryFlagImage != null) {
					countryFlagURL = countryFlagImage.getAttribute("src");
				}

				String imageURL = "";
				String clubInfoText = clubInfo.getTextContent();

				if (clubImage != null) {
					imageURL = clubImage.getAttribute("src");
				}

				// Parse player count from btn-warning span: "339 Igraca"
				Integer playersCount = null;
				HtmlSpan playersSpan = clubElement.getFirstByXPath(".//a[contains(@class,'btn-warning')]//span");
				if (playersSpan != null) {
					Matcher m = NUMBER_PATTERN.matcher(playersSpan.getTextContent());
					if (m.find()) playersCount = Integer.parseInt(m.group(1));
				}

				// Parse tournament count from btn-primary span: "1270 turnira"
				Integer tournamentsCount = null;
				HtmlSpan tournamentsSpan = clubElement.getFirstByXPath(".//a[contains(@class,'btn-primary')]//span");
				if (tournamentsSpan != null) {
					Matcher m = NUMBER_PATTERN.matcher(tournamentsSpan.getTextContent());
					if (m.find()) tournamentsCount = Integer.parseInt(m.group(1));
				}

				Optional<Club> existingClubOptional = clubRepo.findByExternalId(externalId);
				if (existingClubOptional.isPresent()) {
					Club existingClub = existingClubOptional.get();
					existingClub.setName(name);
					existingClub.setClubInfo(clubInfoText);
					existingClub.setCountryFlagURL(countryFlagURL);
					existingClub.setExternalLink(externalLink);
					existingClub.setImageURL(imageURL);
					existingClub.setPlaуersCount(playersCount);
					existingClub.setTournamentsCount(tournamentsCount);
					saveClub(existingClub);
				} else {
					Club newClub = new Club();
					newClub.setExternalId(externalId);
					newClub.setName(name);
					newClub.setClubInfo(clubInfoText);
					newClub.setCountryFlagURL(countryFlagURL);
					newClub.setExternalLink(externalLink);
					newClub.setImageURL(imageURL);
					newClub.setPlaуersCount(playersCount);
					newClub.setTournamentsCount(tournamentsCount);
					saveClub(newClub);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates or updates the independent players club (for players without a club).
	 */
	@Override
	public void createIndependentClub() {
		Optional<Club> existingIndependentClub = clubRepo.findByExternalId(INDEPENDEND_CLUB_EXTERNAL_ID);

		if (existingIndependentClub.isPresent()) {
			Club independentClub = existingIndependentClub.get();
			independentClub.setExternalId(INDEPENDEND_CLUB_EXTERNAL_ID);
			independentClub.setName("Samostalni igrači");
			saveClub(independentClub);
		} else {
			Club independentClub = new Club();
			independentClub.setExternalId(INDEPENDEND_CLUB_EXTERNAL_ID);
			independentClub.setName("Samostalni igrači");
			saveClub(independentClub);
		}
	}
}

