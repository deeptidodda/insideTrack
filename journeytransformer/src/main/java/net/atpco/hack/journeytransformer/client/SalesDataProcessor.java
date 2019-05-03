package net.atpco.hack.journeytransformer.client;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.function.BiPredicate;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.atpco.ash.vo.DateRange;
import net.atpco.ash.vo.Flight;
import net.atpco.ash.vo.Flights;
import net.atpco.engine.common.itinerary.Itinerary;
import net.atpco.engine.common.itinerary.ItineraryLeg;
import net.atpco.hack.journeytransformer.TransformResponse;
import net.atpco.hack.journeytransformer.vo.SalesData;
import net.atpco.journey.client.JourneyClientHelper;
import net.atpco.journey.request.CityAirportFCType;
import net.atpco.journey.request.ItineraryBuddy;
import net.atpco.journey.request.JourneyQuery;
import net.atpco.journey.schedule.FareComponentResponse;
import net.atpco.loader.Loader;
import net.atpco.pricing.version.QueryVersionHelper;
import net.atpco.version.common.VersionQuery;

@Slf4j
public class SalesDataProcessor {
	
	public static final String OUTPUT_DIR = "/opt/hack2018/output";
	private BlockingQueue<SalesData> blockingQueue ;

	private final JourneyClientHelper journeyClientHelper;
	private final QueryVersionHelper versionHelper;
	private final ItineraryBuddy buddy;
	private final ItineraryGeneratorRequest request;
	
	private final TransformResponse transformResponse = new TransformResponse();
	
	private int lineNumber = 0;

	private DateFormat srcDf = new SimpleDateFormat("yyyy-MM-dd");
	
	private DateFormat tkDf = new SimpleDateFormat("ddMMMyy");

	public SalesDataProcessor(Loader loader, JourneyClientHelper journeyClientHelper, QueryVersionHelper versionHelper, ItineraryBuddy buddy, ItineraryGeneratorRequest request ,BlockingQueue<SalesData> blockingQueue) {
//		super(loader, true);
		this.journeyClientHelper = journeyClientHelper;
		this.versionHelper = versionHelper;
		this.buddy = buddy;
		this.request = request;
		this.blockingQueue = blockingQueue;
	}
	public void update(Map map) {
        System.out.println(map.get("oldCachedValue"));
        System.out.println(map.get("newCachedValue"));
    }

	@SneakyThrows
	public SalesData process() {
		try {
			/*if(tokens[1].trim().isEmpty()) return null;


			SalesData data = new SalesData(tokens[1].split(";"), tokens[2].split(";"),tokens[3].split(";"), tokens[6].split(";"), tokens[7].split(";"), tokens[8], tokens[9].split(";"));*/
			while (true) {

				SalesData data = blockingQueue.take();
				String[] flightPath= data.getFlightPath();

				String origin = flightPath[0];
				String destination = flightPath[flightPath.length-1];

				if(request.getOrigin() != null) {
					if(!origin.equalsIgnoreCase(request.getOrigin())) {
						continue;
					}
				}
				if(request.getDestination() != null) {
					if(!destination.equalsIgnoreCase(request.getDestination())) {
						continue;
					}
				}

				if(request.getCarriers() != null) {
					if(!Arrays.equals(data.getMarketingCarriers(), request.getCarriers())) {
						continue;
					}
				}

				Date stdate = srcDf.parse(data.getFlightDates()[0]);
				DateRange travelStartDateRange = new DateRange(stdate, stdate);
				Date ticketingDate = tkDf.parse(data.getTicketingDate());

				Long version = versionHelper.getSubsVersionFromTime(ticketingDate.getTime());

				if(version == null) {
					version = 1L;
				}
				VersionQuery.set(version);

				
				JourneyQuery query = new JourneyQuery(origin, destination, travelStartDateRange, null, "OW", true, buddy, CityAirportFCType.AIRPORT, version);

				FareComponentResponse response = journeyClientHelper.getJourneys(query);

				transformResponse.transform(response, OUTPUT_DIR + "/" + origin + "-" + destination + "-" + lineNumber++ +  ".csv", this::containsAirFrance, generateSalesDataMatchPred(data));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}
	
	public boolean containsAirFrance(Itinerary itinerary, Flights flights) {
		//return flights.containsCarrier("AF") >= 0;
		return true;
	}
	
	public BiPredicate<Itinerary, Flights> generateSalesDataMatchPred(SalesData data) {
		return (itinerary, flights) -> isSalesDataMatch(itinerary, flights, data);
	}

	private boolean isSalesDataMatch(Itinerary itinerary, Flights flights, SalesData salesData) {
		
		String[] flightPath = getFlightPath(itinerary);
		String[] salesDataFlightPath = salesData.getFlightPath();

		if (!Arrays.equals(flightPath, salesDataFlightPath)) {
			
			return false;
		}
		String[] carrierCodes = flights.getCarrierCodes();
		String[] marketingCarriers = salesData.getMarketingCarriers();
		
		if (!Arrays.equals(carrierCodes, marketingCarriers)) {
			return false;
		}
		/*if (!Arrays.equals(getFlightNumbers(flights), salesData.getMarketingFlightNumbers())) {
			return false;
		}*/

		log.info("Match Found!");
		return true;
	}

	private String[] getFlightNumbers(Flights flights) {
		String[] flightNums = new String[flights.size()];
		for (int index = 0; index < flights.size(); index++) {
			Flight flt = flights.get(index);
			flightNums[index] = String.valueOf(flt.getFlightNo());
		}
		return flightNums;
	}

	private String[] getFlightPath(Itinerary itinerary) {
		String[] path = new String[itinerary.getNoOfLegs()+1];
		for (int index = 0; index < itinerary.getNoOfLegs(); index++) {
			ItineraryLeg leg = itinerary.getItineraryLeg(index);
			path[index] = leg.getSegment().getOriginAirport();
		}
		path[itinerary.getNoOfLegs()] = itinerary.getLastLeg().getSegment().getDestinationAirport();
		return path;
	}

}
