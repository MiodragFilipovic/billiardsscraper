package com.poolstats.billiardsscraper.common.service.impl;

import java.util.List;
import java.util.Optional;

import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlBold;
import org.htmlunit.html.HtmlDivision;
import org.htmlunit.html.HtmlHeading4;
import org.htmlunit.html.HtmlImage;
import org.htmlunit.html.HtmlParagraph;
import org.htmlunit.html.HtmlSpan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poolstats.billiardsscraper.common.entity.Club;
import com.poolstats.billiardsscraper.common.entity.Player;
import com.poolstats.billiardsscraper.common.repo.PlayerRepo;
import com.poolstats.billiardsscraper.common.service.PlayerService;

/**
 * Service implementation for managing player data and scraping operations.
 */
@Service
public class PlayerServiceImpl implements PlayerService {

	@Autowired
	private PlayerRepo playerRepo;

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

			String fullName = lastName + " " + firstName;

			Optional<Player> existingPlayerOptional = playerRepo.findByFullName(fullName);

			String[] playerURLParts = playerUrl.split("ID=");
			String externalId = playerURLParts.length > 1 ? playerURLParts[1] : "";

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
		Optional<Player> playerOptional = playerRepo.findByFullName(fullName.trim());
		if (playerOptional.isPresent()) {
			return playerOptional.get();
		}

		System.out.println("Nije pronadjen igrac: " + fullName);
		return null;
	}
}

