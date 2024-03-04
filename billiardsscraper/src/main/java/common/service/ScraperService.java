package common.service;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import common.entity.Player;

@Service
public class ScraperService {

	@Autowired
	private PlayerService playerService;

	public void syncPlayersFromWebsite() {
		// Skrejping podataka sa web stranice
		Document doc;
		try {
			doc = Jsoup.connect("https://bilijar.club/sviigraci.php").get();
			Elements players = doc.select(".col-sm-6.col-md-3.col-xs-6");

			for (Element element : players) {
				savePlayerWithData(element);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void savePlayerWithData(Element playerElement) {
		// Ekstrakcija podataka iz HTML-a
		String fullName = playerElement.select("h4.nomargin").text();
		String[] nameParts = fullName.split("\\s+");

		String firstName = nameParts[0];
		String lastName = nameParts.length > 1 ? nameParts[1] : ""; // U slučaju da nema prezimena

		String imageUrl = playerElement.select("img").first().attr("src");
		String playerUrl = playerElement.select("a").first().attr("href");

		Elements statsElements = playerElement.select("p.nomargin");
		String countryImageUrl = statsElements.select("img").first().attr("src");
//		String classImageUrl = statsElements.select("span.klasa img").first().attr("src");
		String rating = statsElements.select("span.rejting").first().text();
		String winLossRatio = statsElements.select("b").first().text();
		// Možete ekstrahovati ostale podatke na isti način

		// Kreiranje objekta Player
		Player player = new Player();
		player.setFirstName(firstName);
		player.setLastName(lastName);
		player.setImageUrl(imageUrl);
		player.setPlayerUrl(playerUrl);
		player.setCountry(countryImageUrl);
		player.setRating(Double.valueOf(0));
		// Postavite ostale podatke na objekat Player

		// Snimanje igrača u bazu podataka
		playerService.savePlayer(player);
	}

}
