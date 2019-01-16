package net.atpco.hack.journeytransformer.client;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

	@RequestMapping(value = "/generate", method = { RequestMethod.POST})
	public Map<String, Object> generateItineraries(@RequestBody ItineraryGeneratorRequest request) throws IOException {

		String outputLocation = itineraryGenerator.generate(request);
		
		Map<String, Object> retData = new LinkedHashMap<>();
		retData.put("status", "SUCCESS");
		retData.put("message", "Please find generated data at " + outputLocation);
		return retData;
	}

}
