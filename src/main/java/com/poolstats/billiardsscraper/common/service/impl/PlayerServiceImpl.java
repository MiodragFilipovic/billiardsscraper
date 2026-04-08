package com.poolstats.billiardsscraper.common.service.impl;

import java.util.List;
import java.util.Optional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlDivision;
import org.htmlunit.html.HtmlHeading4;
import org.htmlunit.html.HtmlImage;
import org.htmlunit.html.HtmlParagraph;
import org.htmlunit.html.HtmlSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poolstats.billiardsscraper.common.entity.Club;
import com.poolstats.billiardsscraper.common.entity.Player;
import com.poolstats.billiardsscraper.common.logging.SyncErrorLogger;
import com.poolstats.billiardsscraper.common.repo.PlayerRepo;
import com.poolstats.billiardsscraper.common.service.PlayerService;

/**
 * Service implementation for managing player data and scraping operations.
 */
@Service
public class PlayerServiceImpl implements PlayerService {

        private static final Logger log = LoggerFactory.getLogger(PlayerServiceImpl.class);

        private static final Pattern WINS_PATTERN   = Pattern.compile("Pobeda\\s*(\\d+)");
        private static final Pattern LOSSES_PATTERN = Pattern.compile("Poraza\\s*(\\d+)");

	@Autowired
	private PlayerRepo playerRepo;

	@Autowired
	private SyncErrorLogger syncErrorLogger;

	@Override
	public void savePlayer(Player player) {
		playerRepo.save(player);
	}

	/**
	 * Extracts player data from HTML element and saves to database.
	 * Updates existing player or creates new one if not found.
	 *
	 * @param playerElement HTML division containing player information
	 * @param orderNumber position order of the player
	 * @param club the club this player belongs to
	 */
	@Override
	public void savePlayerWithData(HtmlDivision playerElement, int orderNumber, Club club) {
		HtmlHeading4 fullNameAndWinsElement = playerElement.getFirstByXPath(".//h4[contains(@class, 'nomargin')]");
		if (fullNameAndWinsElement != null) {

			String fullNameAndWins = fullNameAndWinsElement.asNormalizedText().trim();

			String[] parts = fullNameAndWins.split("\\s+", 2);
			String lastName = parts[0].trim();
			String firstNameAndWins = parts.length > 1 ? parts[1].trim() : "";
			String tournamentWins = "0";
			String firstName = "";

			StringBuilder tournamentsWinSB = new StringBuilder();
			StringBuilder firstNameSB = new StringBuilder();

			try {
				String modifiedString = firstNameAndWins.replace("\n", " ");

				for (int i = modifiedString.length() - 1; i >= 0; i--) {
					char c = modifiedString.charAt(i);
					if (Character.isDigit(c)) {
						tournamentsWinSB.insert(0, c);
					} else {
						firstNameSB.insert(0, c);
					}
				}
				tournamentWins = tournamentsWinSB.toString();
				firstName = firstNameSB.toString().trim();
			} catch (IndexOutOfBoundsException e) {
				System.out.println(lastName);
				e.printStackTrace();
			}

                        String imageUrl = "";
                        String playerUrl = "";
                        String countryImageUrl = "";
                        String rating = "";

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

                        // Parse wins and losses from <p>Pobeda X<br>Poraza Y</p>
                        int parsedWins = 0;
                        int parsedLosses = 0;
                        List<HtmlParagraph> allParagraphs = playerElement.getByXPath(".//p");
                        for (HtmlParagraph para : allParagraphs) {
                                String paraText = para.asNormalizedText();
                                if (paraText.contains("Pobeda") || paraText.contains("Poraza")) {
                                        Matcher winMatcher  = WINS_PATTERN.matcher(paraText);
                                        Matcher lossMatcher = LOSSES_PATTERN.matcher(paraText);
                                        if (winMatcher.find())  parsedWins   = Integer.parseInt(winMatcher.group(1));
                                        if (lossMatcher.find()) parsedLosses = Integer.parseInt(lossMatcher.group(1));
                                        log.debug("Parsed wins={} losses={} from: {}", parsedWins, parsedLosses, paraText.trim());
                                        break;
                                }
                        }
                        double parsedWinPct = (parsedWins + parsedLosses > 0)
                                ? (double) parsedWins / (parsedWins + parsedLosses) * 100.0
                                : 0.0;

			String fullName = lastName + " " + firstName;

			List<Player> existingPlayers = playerRepo.findAllByFullName(fullName);

			String[] playerURLParts = playerUrl.split("ID=");
			String externalId = playerURLParts.length > 1 ? playerURLParts[1] : "";

			Player existingPlayer = null;
			if (existingPlayers.size() == 1) {
				existingPlayer = existingPlayers.get(0);
			} else if (existingPlayers.size() > 1) {
				// prefer the one that belongs to the same club
				existingPlayer = existingPlayers.stream()
					.filter(p -> p.getClub() != null && p.getClub().getId().equals(club.getId()))
					.findFirst()
					.orElse(existingPlayers.get(0));
				syncErrorLogger.duplicatePlayers(fullName, existingPlayers.size(),
					"club=" + club.getName() + " externalId=" + externalId);
			}

                        if (existingPlayer != null) {

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
                                existingPlayer.setWins(parsedWins);
                                existingPlayer.setLosses(parsedLosses);
                                existingPlayer.setWinPercentage(parsedWinPct);

                                savePlayer(existingPlayer);
                        } else {
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
                                newPlayer.setWins(parsedWins);
                                newPlayer.setLosses(parsedLosses);
                                newPlayer.setWinPercentage(parsedWinPct);

                                savePlayer(newPlayer);
                        }
		}
	}

	/**
	 * Retrieves a player by their full name.
	 *
	 * @param fullName the full name of the player to find
	 * @return the player if found, null otherwise
	 */
	@Override
	public Player getPlayerByName(String fullName) {
		List<Player> players = playerRepo.findAllByFullName(fullName.trim());
		if (players.size() == 1) {
			return players.get(0);
		}
		if (players.size() > 1) {
			syncErrorLogger.duplicatePlayers(fullName.trim(), players.size(), "getPlayerByName");
			return players.get(0);
		}
		syncErrorLogger.playerNotFound(fullName.trim(), "getPlayerByName");
		return null;
	}
}

