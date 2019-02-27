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
import net.atpco.engine.common.cache.QueryCacheConfiguration;
import net.atpco.journey.client.JourneyClientHelper;
import net.atpco.journey.request.ItineraryBuddy;
import net.atpco.pricing.version.QueryVersionHelper;
import net.atpco.version.common.VersionQuery;

@RequiredArgsConstructor
@RequestMapping("/transform")
public class TransformerController {
	
	private final QueryVersionHelper versionHelper;
	private final ItineraryGenerator itineraryGenerator;
	private final QueryCacheConfiguration loc;
	private final JourneyClientHelper journeyClientHelper;
	private final ItineraryBuddy buddy;
	
	@RequestMapping("/ping")
	public String ping() {
		return "PONG";
	}

	@RequestMapping(value = "/js", method = { RequestMethod.POST})
	public void transformSalesData(@RequestBody ItineraryGeneratorRequest request) {
		Long version = versionHelper.getLiveVersion();

		if(version == null) {
			version = 1L;
		}
		VersionQuery.set(version);
		loc.cacheManager().tpmMileage();
		loc.cacheManager().mileage();
		SalesDataReader dataReader = new SalesDataReader(null, journeyClientHelper, versionHelper, buddy);
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
