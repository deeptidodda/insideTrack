package net.atpco.hack.journeytransformer.client;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
public class ItineraryGenerator {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

	public static final String OUTPUT_DIR = "/opt/atpco/hack2018/controlled1";

	private final JourneyClientHelper journeyClientHelper;
	private final QueryVersionHelper versionHelper;
	private final ItineraryBuddy buddy;
	
	private final TransformResponse transformResponse = new TransformResponse();
	
//	final String origin = "IAD";
//	final String destination = "LHR";
//	final String carrier = "BA";
//	final LocalDate beginningDate = LocalDate.of(2019, 1, 21);
//	final int numWeeks = 2;
//	
	final String origin = "LAX";
	final String destination = "BCN";
	final String carrier = "IB";
	final LocalDate beginningDate = LocalDate.of(2019, 1, 21);
	final int numWeeks = 45;
	
	public void generate() throws IOException {
	
		for (int i = 0; i < numWeeks; i++) {
			
			LocalDate requestDate = beginningDate.plusDays(i*7);
			DateRange travelStartDateRange = new LocalDateRange(requestDate, requestDate).toDateRange();
		
			Long version = versionHelper.getLiveVersion();
			VersionQuery.set(version);
			JourneyQuery query = new JourneyQuery(origin, destination, travelStartDateRange, null, "OW", true, buddy, CityAirportFCType.AIRPORT, version);
		
			FareComponentResponse response = journeyClientHelper.getJourneys(query);
			
			new File(OUTPUT_DIR).mkdirs();
			transformResponse.transform(null, response, OUTPUT_DIR + "/" + origin + "-" + destination + "-" +  DATE_FORMATTER.format(requestDate) +  ".csv", this::filter);
		}
	}
	
	public boolean filter(Itinerary itinerary, Flights flights) {
		return flights.isSameCarrier(carrier);
	}
}
