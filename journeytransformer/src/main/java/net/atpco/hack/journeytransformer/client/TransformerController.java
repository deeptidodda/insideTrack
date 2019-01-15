package net.atpco.hack.journeytransformer.client;

import java.io.IOException;
import java.nio.file.Paths;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/transform")
public class TransformerController {
	
	private final SalesDataReader dataReader;
	private final ItineraryGenerator itineraryGenerator;
	
	@RequestMapping("/ping")
	public String ping() {
		return "PONG";
	}

	@RequestMapping("/js")
	public void transformSalesData() {

		dataReader.read(Paths.get("/hack/insideTrack/data/itinerary.csv"));
	}

	@RequestMapping("/generate")
	public void generateItineraries() throws IOException {

		itineraryGenerator.generate();
	}

}
