package com.poolstats.billiardsscraper;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import org.xml.sax.SAXException;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import java.text.ParseException;
import org.geotools.referencing.GeodeticCalculator;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BilijarScraperApplication {

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, TransformerException, ParseException, FactoryException, TransformException {
        File inputFile = new File("C:\\dev\\billiardsscraper\\billiardsscraper\\src\\main\\java\\com\\poolstats\\billiardsscraper\\Beograd_Beocin_Novi_Sad.gpx");
        File outputFile = new File("output_with_speed.gpx");

        // Parse GPX XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(inputFile);
        doc.getDocumentElement().normalize();

        // Get all trackpoints
        NodeList trkptNodes = doc.getElementsByTagName("trkpt");

        // Iterate over trackpoints and calculate speed
        for (int i = 0; i < trkptNodes.getLength() - 1; i++) {
            Element point1 = (Element) trkptNodes.item(i);
            Element point2 = (Element) trkptNodes.item(i + 1);

            // Extract lat/lon
            double lat1 = Double.parseDouble(point1.getAttribute("lat"));
            double lon1 = Double.parseDouble(point1.getAttribute("lon"));
            double lat2 = Double.parseDouble(point2.getAttribute("lat"));
            double lon2 = Double.parseDouble(point2.getAttribute("lon"));

            // Extract time
            String time1Str = point1.getElementsByTagName("time").item(0).getTextContent();
            String time2Str = point2.getElementsByTagName("time").item(0).getTextContent();

            // Calculate distance in kilometers
            double distance = calculateDistance(lat1, lon1, lat2, lon2);

            // Calculate time difference in hours
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            Date time1 = sdf.parse(time1Str);
            Date time2 = sdf.parse(time2Str);
            double timeDifference = (time2.getTime() - time1.getTime()) / (1000.0 * 3600.0);

            // Calculate speed in km/h
            double speed = distance / timeDifference;

            // Add speed element to the second point
            Element speedElement = doc.createElement("speed");
            speedElement.appendChild(doc.createTextNode(String.format("%.2f", speed)));
            point2.appendChild(speedElement);
        }

        // Write the output to a new GPX file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(outputFile);
        transformer.transform(source, result);

        System.out.println("GPX file with speeds generated: " + outputFile.getAbsolutePath());
    }

    // Method to calculate distance between two GPS coordinates (Haversine formula or GeodeticCalculator from GeoTools)
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) throws FactoryException, TransformException {
        GeodeticCalculator calc = new GeodeticCalculator();
        calc.setStartingGeographicPoint(lon1, lat1);
        calc.setDestinationGeographicPoint(lon2, lat2);
        return calc.getOrthodromicDistance() / 1000.0;  // distance in kilometers
    }
}
