package com.poolstats.billiardsscraper.common.service.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
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
import org.htmlunit.html.HtmlTableRow;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.poolstats.billiardsscraper.common.entity.Club;
import com.poolstats.billiardsscraper.common.entity.Match;
import com.poolstats.billiardsscraper.common.entity.Player;
import com.poolstats.billiardsscraper.common.entity.Tournament;
import com.poolstats.billiardsscraper.common.repo.ClubRepo;
import com.poolstats.billiardsscraper.common.repo.PlayerRepo;
import com.poolstats.billiardsscraper.common.repo.TournamentRepo;
import com.poolstats.billiardsscraper.common.service.ClubService;
import com.poolstats.billiardsscraper.common.service.MatchService;
import com.poolstats.billiardsscraper.common.service.PlayerService;
import com.poolstats.billiardsscraper.common.service.ScraperService;
import com.poolstats.billiardsscraper.common.service.TournamentService;

@Service
public class ScraperServiceImpl implements ScraperService {

	public static final String[] TOURNAMENT_YEARS = { "2024", "2023", "2022", "2021", "2020", "2019", "2018", "2017", "2016" };

	public static final String INDEPENDEND_CLUB_EXTERNAL_ID = "0";

	@Autowired
	private PlayerService playerService;
	@Autowired
	private PlayerRepo playerRepo;
	@Autowired
	private ClubService clubService;
	@Autowired
	private ClubRepo clubRepo;
	@Autowired
	private TournamentService tournamentService;
	@Autowired
	private MatchService matchService;
	@Autowired
	private TournamentRepo tournamentRepo;
	@Autowired
	private RestTemplate restTemplate;

	@Override
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
		Optional<Club> existingIndependentClub = clubRepo.findByExternalId("0");

		if (existingIndependentClub.isPresent()) {
			Club independentClub = existingIndependentClub.get();
			independentClub.setExternalId("0");
			independentClub.setName("Samostalni igrači");
			clubService.saveClub(independentClub);
		} else {
			Club independentClub = new Club();
			independentClub.setExternalId("0");
			independentClub.setName("Samostalni igrači");
			clubService.saveClub(independentClub);
		}

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

	@Override
	public void saveTournamentWithData(Element tournamentElement, Club club, String year) {
		// Izdvojimo sve potrebne informacije iz HTML-a
		String externalLinkWithoutBaseURL = tournamentElement.select("h4 a").attr("href");
		String externalId = externalLinkWithoutBaseURL.substring(externalLinkWithoutBaseURL.lastIndexOf("=") + 1);
		String name = tournamentElement.select("h4 a").text();

		String dateText = tournamentElement.select("td span").first().text(); // Dobijamo tekst datuma u formatu "dan.mesec"
		LocalDate tournamentDate = parseTournamentDate(dateText, year);

		String externalLink = "https://bilijar.club/" + externalLinkWithoutBaseURL;

		// Proverimo da li turnir već postoji u bazi na osnovu externalId-a
		Optional<Tournament> existingTournamentOptional = tournamentRepo.findByExternalId(externalId);

		if (existingTournamentOptional.isPresent()) {
			// Ako turnir već postoji, ažuriramo njegove informacije
			Tournament existingTournament = existingTournamentOptional.get();

			existingTournament.setExternalLink(externalLink);
			existingTournament.setName(name);
			existingTournament.setDate(tournamentDate);
			existingTournament.setClub(club);
			tournamentService.saveTournament(existingTournament);
		} else {
			// Ako turnir ne postoji, kreiramo novi
			Tournament newTournament = new Tournament();
			newTournament.setExternalId(externalId);
			newTournament.setExternalLink(externalLink);
			newTournament.setName(name);
			newTournament.setDate(tournamentDate);
			newTournament.setClub(club);
			tournamentService.saveTournament(newTournament);
		}

	}

	private LocalDate parseTournamentDate(String dateText, String year) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MMM.yyyy", Locale.ENGLISH);
		String fullDateText = dateText + "." + year; // Dodajemo godinu da bismo dobili puni datum
		return LocalDate.parse(fullDateText, formatter);
	}

	@Override
	public void syncAllTournamentsFromWebsite() {
		List<Club> clubs = clubRepo.findAll();

		for (Club club : clubs) {
			if (club.getExternalId().equals(INDEPENDEND_CLUB_EXTERNAL_ID)) {
				continue;
			}
			for (String year : TOURNAMENT_YEARS) {

				boolean endOfYear = false;
				int pagiantionCount = 0;

				while (!endOfYear) {
					MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
					body.add("getresult", String.valueOf(pagiantionCount));
					body.add("club", club.getExternalId());
					body.add("year", year);

					String result = restTemplate.postForObject("https://bilijar.club/fetch.php", body, String.class);

					if (result != null && !result.isEmpty()) {
						Document doc = Jsoup.parse("<table>" + result + "</table>");
						Elements tournaments = doc.select("tr"); // selektujemo sve elemente <tr>

						for (Element tournament : tournaments) {
							saveTournamentWithData(tournament, club, year);
						}
						pagiantionCount = pagiantionCount + 10;
					} else {
						endOfYear = true;
					}
				}
			}
		}
	}

	@Override
	public void saveMatchWithData(HtmlTableRow matchElement, int orderNumber, Tournament tournament) {
		Match newMatch = new Match();

		if (!matchElement.getCells().get(4).getTextContent().trim().equals(">>")) {
			return;
		}

		newMatch.setTournament(tournament);

		try {
			newMatch.setOrderNumber(Integer.parseInt(matchElement.getCells().get(0).getTextContent().trim()));
			newMatch.setDate(getMatchDateTimeFromCell(matchElement.getCells().get(1).getTextContent().trim(), tournament.getDate().getYear()));

			Player player1 = getPlayerByName(matchElement.getCells().get(3).getTextContent().trim());
			Player player2 = getPlayerByName(matchElement.getCells().get(7).getTextContent().trim());
			newMatch.setPlayer1(player1);
			newMatch.setPlayer2(player2);
			newMatch.setResult1(Integer.parseInt((matchElement.getCells().get(4).getTextContent().trim())));
			newMatch.setResult2(Integer.parseInt((matchElement.getCells().get(5).getTextContent().trim())));
			newMatch.setHandikap((matchElement.getCells().get(6).getTextContent().trim()));
			if (newMatch.getResult1() > newMatch.getResult2()) {
				newMatch.setWinner(player1);
			} else {
				newMatch.setWinner(player2);
			}

		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		matchService.saveMatch(newMatch);

	}

	private Player getPlayerByName(String fullName) {
		Optional<Player> playerOptional = playerRepo.findByFullName(fullName);
		if (playerOptional.isPresent()) {
			return playerOptional.get();
		}
		return null;
	}

	public LocalDateTime getMatchDateTimeFromCell(String cellContent, int tournamentYear) {
		// Formatiranje godine turnira prema uzorku "yyyy"
		String formattedYear = String.valueOf(tournamentYear);

		// Formatiranje datuma i vremena prema uzorku "dd.MM. HH:mm"
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

		// Podjela sadržaja ćelije po crticama ("-")
		String[] parts = cellContent.split(" - ");

		// Dobijanje datuma i vremena iz podijeljenih dijelova
		String datePart = parts[0]; // "10.12."
		String timePart = parts[1]; // "11:21"

		// Spajanje datuma i vremena sa godinom turnira
		String cellContentWithYear = datePart + formattedYear + " " + timePart;

		// Parsiranje teksta u LocalDateTime
		LocalDateTime dateTime = LocalDateTime.parse(cellContentWithYear, formatter);

		return dateTime;
	}
	@Override
	public void syncAllMatchesFromWebsite() {
		try {
			WebClient webClient = new WebClient(BrowserVersion.CHROME);
			webClient.getOptions().setJavaScriptEnabled(true); // enable javascript
			webClient.getOptions().setThrowExceptionOnScriptError(false); // even if there is error in js continue
//					webClient.waitForBackgroundJavaScript(500); // important! wait until javascript finishes rendering

			List<Tournament> tournaments = tournamentRepo.findAll();

//			for (Tournament tournament : tournaments) {
//				HtmlPage page = webClient.getPage("https://bilijar.club/tournament.php?ID=" + tournament.getExternalId());
//
//				List<HtmlTableRow> matches = page.getByXPath("//tr[contains(@class, 'size-11')]");
//				System.out.println(tournament.getName());
//				int orderNumberMatch = 0;
//				for (HtmlTableRow matchElement : matches) {
//					orderNumberMatch++;
//					saveMatchWithData(matchElement, orderNumberMatch, tournament);
//				}
//
//			}

			Tournament tournament = tournamentRepo.findByExternalId("2657").get();

			HtmlPage page = webClient.getPage("https://bilijar.club/tournament.php?ID=" + tournament.getExternalId());

			List<HtmlTableRow> matches = page.getByXPath("//tr[contains(@class, 'size-11')]");
			System.out.println(tournament.getName());
			int orderNumberMatch = 0;
			for (HtmlTableRow matchElement : matches) {
				orderNumberMatch++;
				saveMatchWithData(matchElement, orderNumberMatch, tournament);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}