package com.poolstats.billiardsscraper.common.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.htmlunit.BrowserVersion;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlBold;
import org.htmlunit.html.HtmlDivision;
import org.htmlunit.html.HtmlHeading4;
import org.htmlunit.html.HtmlImage;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlParagraph;
import org.htmlunit.html.HtmlSpan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poolstats.billiardsscraper.common.entity.Club;
import com.poolstats.billiardsscraper.common.entity.Player;
import com.poolstats.billiardsscraper.common.repo.ClubRepo;
import com.poolstats.billiardsscraper.common.repo.PlayerRepo;
import com.poolstats.billiardsscraper.common.service.ClubService;
import com.poolstats.billiardsscraper.common.service.PlayerService;
import com.poolstats.billiardsscraper.common.service.ScraperService;

@Service
public class ScraperServiceImpl implements ScraperService {

	@Autowired
	private PlayerService playerService;
	@Autowired
	private PlayerRepo playerRepo;
	@Autowired
	private ClubService clubService;
	@Autowired
	private ClubRepo clubRepo;

	public void syncPlayersFromWebsite() {
		try {
			WebClient webClient = new WebClient(BrowserVersion.CHROME);
			webClient.getOptions().setJavaScriptEnabled(true); // enable javascript
			webClient.getOptions().setThrowExceptionOnScriptError(false); // even if there is error in js continue
//			webClient.waitForBackgroundJavaScript(500); // important! wait until javascript finishes rendering

			List<Club> clubs = clubRepo.findAll();

			for (Club club : clubs) {
				HtmlPage page = webClient.getPage("https://bilijar.club/poklubovima.php?Club_ID=" + club.getExternalId());

				List<HtmlDivision> players = page.getByXPath("//div[contains(@class, 'col-sm-6 col-md-3 col-xs-6')]");

				int orderNumber = 0;
				for (HtmlDivision playerElement : players) {
					orderNumber++;
					savePlayerWithData(playerElement, orderNumber, club);
				}

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void savePlayerWithData(HtmlDivision playerElement, int orderNumber, Club club) {
		HtmlHeading4 fullNameAndWinsElement = playerElement.getFirstByXPath(".//h4[contains(@class, 'nomargin')]");
		if (fullNameAndWinsElement != null) {

			String fullNameAndWins = fullNameAndWinsElement.asNormalizedText().trim();
			if (fullNameAndWins.contains("Nadja")) {
				System.out.println(fullNameAndWins);
			}

			if (fullNameAndWins.contains("GOST")) {
				System.out.println(fullNameAndWins);
			}

			String[] parts = fullNameAndWins.split("\\s+", 2); // Split po prvom razmaku, maksimalno 2 dela
			String lastName = parts[0].trim();
			String firstNameAndWins = parts.length > 1 ? parts[1].trim() : ""; // Ostatak kao prvi deo
			String tournamentWins = "0";
			String firstName = "";

			StringBuilder tournamentsWinSB = new StringBuilder();
			StringBuilder firstNameSB = new StringBuilder();
			try {

				for (int i = firstNameAndWins.length() - 1; i >= 0; i--) {
					char c = firstNameAndWins.charAt(i);
					if (Character.isDigit(c)) {
						tournamentsWinSB.insert(0, c);
					} else {
						firstNameSB.insert(0, c);
					}
				}
				tournamentWins = tournamentsWinSB.toString();
				firstName = firstNameSB.toString();
			} catch (IndexOutOfBoundsException e) {
				System.out.println(lastName);
				e.printStackTrace();
			}

			// Ekstrakcija ostalih podataka
			String imageUrl = "";
			String playerUrl = "";
			String countryImageUrl = "";
			String rating = "";
			String winLossRatio = "";

			List<HtmlImage> imgElements = playerElement.getByXPath(".//img");
			if (!imgElements.isEmpty()) {
				imageUrl = imgElements.get(0).getAttribute("src");
			}

			List<HtmlAnchor> aElements = playerElement.getByXPath(".//a");
			if (!aElements.isEmpty()) {
				playerUrl = "https://bilijar.club/" + aElements.get(0).getAttribute("href");
			}

			List<HtmlParagraph> statsElements = playerElement.getByXPath(".//p[contains(@class, 'nomargin')]");
			List<HtmlImage> countryImgElements = statsElements.get(0).getByXPath(".//img");
			if (!countryImgElements.isEmpty()) {
				countryImageUrl = countryImgElements.get(0).getAttribute("src");
			}

			List<HtmlSpan> ratingElements = statsElements.get(0).getByXPath(".//span[@class='rejting']");
			if (!ratingElements.isEmpty()) {
				rating = ratingElements.get(0).getTextContent();
			}

			List<HtmlBold> winLossRatioElements = statsElements.get(0).getByXPath(".//b");
			if (!winLossRatioElements.isEmpty()) {
				winLossRatio = winLossRatioElements.get(0).getTextContent();
			}

			String fullName = firstName + " " + lastName;

			// Pretraga igrača u bazi po imenu i prezimenu
			Optional<Player> existingPlayerOptional = playerRepo.findByFullName(fullName);

			String[] playerURLParts = playerUrl.split("ID=");
			String externalId = playerURLParts.length > 1 ? playerURLParts[1] : "";

			// Ako igrač postoji, ažurirajte ga
			if (existingPlayerOptional.isPresent()) {
				Player existingPlayer = existingPlayerOptional.get();

				existingPlayer.setFirstName(firstName);
				existingPlayer.setLastName(lastName);
				existingPlayer.setTournamentsWins(!tournamentWins.isEmpty() ? Integer.parseInt(tournamentWins) : 0);
				existingPlayer.setImageURL(imageUrl);
				existingPlayer.setPlayerUrl(playerUrl);
				existingPlayer.setExternalId(externalId);
				existingPlayer.setFullName(fullName);
				existingPlayer.setCountry(countryImageUrl);
				existingPlayer.setRating(!rating.isEmpty() ? Double.parseDouble(rating) : 0);
				existingPlayer.setClub(club);

				// Ažuriranje igrača u bazi podataka
				playerService.savePlayer(existingPlayer);
			} else {
				// Ako igrač ne postoji, kreirajte novog
				Player newPlayer = new Player();
				newPlayer.setFirstName(firstName);
				newPlayer.setLastName(lastName);
				newPlayer.setExternalId(externalId);
				newPlayer.setFullName(fullName);
				newPlayer.setTournamentsWins(!tournamentWins.isEmpty() ? Integer.parseInt(tournamentWins) : 0);
				newPlayer.setImageURL(imageUrl);
				newPlayer.setPlayerUrl(playerUrl);
				newPlayer.setCountry(countryImageUrl);
				newPlayer.setRating(!rating.isEmpty() ? Double.parseDouble(rating) : 0);
				newPlayer.setClub(club);

				// Snimanje novog igrača u bazi podataka
				playerService.savePlayer(newPlayer);
			}
		}
	}

	@Override
	public void syncClubsFromWebsite() {
		try {
			WebClient webClient = new WebClient(BrowserVersion.CHROME);
			webClient.getOptions().setJavaScriptEnabled(true); // enable javascript
			webClient.getOptions().setThrowExceptionOnScriptError(false); // even if there is error in js continue
//			webClient.waitForBackgroundJavaScript(500); // important! wait until javascript finishes rendering
			HtmlPage page = webClient.getPage("https://bilijar.club/klubovi.php");

			List<HtmlDivision> clubs = page.getByXPath("//div[contains(@class, 'col-sm-6 col-md-3')]");

			int orderNumber = 0;
			for (HtmlDivision clubElement : clubs) {
				orderNumber++;
				saveClubWithData(clubElement, orderNumber);
			}

			createIndependentClub();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void createIndependentClub() {
		Club independentClub = new Club();
		independentClub.setExternalId("0");
		independentClub.setName("Samostalni igrači");
		clubService.saveClub(independentClub);
	}

	@Override
	public void saveClubWithData(HtmlDivision clubElement, int orderNumber) {
		try {
			HtmlAnchor clubLink = clubElement.getFirstByXPath(".//a");
			HtmlImage clubImage = clubElement.getFirstByXPath(".//img");
			HtmlHeading4 clubNameElement = clubElement.getFirstByXPath(".//h4[contains(@class, 'nomargin')]");
			HtmlParagraph clubInfo = clubElement.getFirstByXPath(".//p");

			if (clubLink != null && clubNameElement != null) {
//		            String externalId = extractClubIdFromLink(clubLink.getAttribute("href"));
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

				// Provera da li klub već postoji u bazi podataka
				Optional<Club> existingClubOptional = clubRepo.findByExternalId(externalId);
				if (existingClubOptional.isPresent()) {
					// Ažuriranje postojećeg kluba ako postoji
					Club existingClub = existingClubOptional.get();
					existingClub.setName(name);
					existingClub.setClubInfo(clubInfoText);
					existingClub.setCountryFlagURL(countryFlagURL);
					existingClub.setExternalLink(externalLink);
					existingClub.setImageURL(imageURL);
					clubService.saveClub(existingClub); // Koristimo servis za čuvanje kluba
				} else {
					// Kreiranje novog kluba ako ne postoji
					Club newClub = new Club();
					newClub.setExternalId(externalId);
					newClub.setName(name);
					newClub.setClubInfo(clubInfoText);
					newClub.setCountryFlagURL(countryFlagURL);
					newClub.setExternalLink(externalLink);
					newClub.setImageURL(imageURL);
					clubService.saveClub(newClub); // Koristimo servis za čuvanje kluba
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
