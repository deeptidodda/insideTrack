package net.atpco.hack.journeytransformer.client;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.atpco.engine.common.cache.QueryCacheConfiguration;
import net.atpco.hack.journeytransformer.vo.SalesData;
import net.atpco.hack.salesdata.SalesDataReader;
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
	private final BlockingQueue<SalesData> blockingQueue = new LinkedBlockingQueue<>();
	ExecutorService service = Executors.newFixedThreadPool(2);
	
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
		SalesDataReader reader = SalesDataReader.getInstance(blockingQueue);
		SalesDataProcessor dataReader = new SalesDataProcessor(null, journeyClientHelper, versionHelper, buddy, request,blockingQueue);
		
		service.submit(new Runnable() {
			@Override
			public void run() {
				try {
					reader.readSalesData();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		service.submit(new Runnable() {
			@Override
			public void run() {
				dataReader.process();
			}
		});
		
//		service.awaitTermination(3600, TimeUnit.HOURS);
//		dataReader.read(Paths.get("/hack/insideTrack/data/itinerary.csv"));
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
