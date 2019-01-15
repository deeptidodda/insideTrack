package net.atpco.hack.journeytransformer.client;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.BiPredicate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.atpco.ash.vo.DateRange;
import net.atpco.ash.vo.Flights;
import net.atpco.ash.vo.LocalDateRange;
import net.atpco.engine.common.itinerary.Itinerary;
import net.atpco.hack.journeytransformer.TransformResponse;
import net.atpco.journey.client.JourneyClientHelper;
import net.atpco.journey.request.CityAirportFCType;
import net.atpco.journey.request.ItineraryBuddy;
import net.atpco.journey.request.JourneyQuery;
import net.atpco.journey.schedule.FareComponentResponse;
import net.atpco.pricing.version.QueryVersionHelper;
import net.atpco.version.common.VersionQuery;

@Slf4j
@RequiredArgsConstructor
public class ItineraryGenerator {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

	public static final String OUTPUT_DIR = "/opt/atpco/hack2018/controlled1";

	private final JourneyClientHelper journeyClientHelper;
	private final QueryVersionHelper versionHelper;
	private final ItineraryBuddy buddy;
	
	private final TransformResponse transformResponse = new TransformResponse();
	
	public void generate(ItineraryGeneratorRequest req) throws IOException {
	
		for (int i = 0; i < req.getNumIterations(); i++) {
			
			LocalDate requestDate = req.getBeginningDate().plus(i*req.getIncrementUnit(), req.getUnits());
			DateRange travelStartDateRange = new LocalDateRange(requestDate, requestDate).toDateRange();
		
			Long version = versionHelper.getLiveVersion();
			VersionQuery.set(version);
			
			JourneyQuery query = new JourneyQuery(req.getOrigin(), req.getDestination(), travelStartDateRange, null, "OW", true, buddy, CityAirportFCType.AIRPORT, version);
			log.info("Sending journey query {}", new ObjectMapper().writeValueAsString(query));
		
			FareComponentResponse response = journeyClientHelper.getJourneys(query);
			log.info("Received journey response {}", new ObjectMapper().writeValueAsString(response));
			
			new File(OUTPUT_DIR).mkdirs();
			final String outFileName = OUTPUT_DIR + "/" + req.getOrigin() + "-" + req.getDestination() + "-" +  DATE_FORMATTER.format(requestDate) +  ".csv";
			log.info("Writing response to {}", outFileName);
			transformResponse.transform(null, response, outFileName, buildFilter(req.getCarrier()));
		}
	}
	
	public BiPredicate<Itinerary, Flights> buildFilter(String carrier) {
		return (itinerary, flights) -> flights.isSameCarrier(carrier);
	}
}
