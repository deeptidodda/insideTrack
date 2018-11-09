package net.atpco.hack.journeytransformer.client;

import java.util.Date;

import net.atpco.ash.io.CSVFileReader;
import net.atpco.ash.vo.DateRange;
import net.atpco.hack.journeytransformer.vo.SalesData;
import net.atpco.journey.client.JourneyClientHelper;
import net.atpco.journey.request.CityAirportFCType;
import net.atpco.journey.request.ItineraryBuddy;
import net.atpco.journey.request.JourneyQuery;
import net.atpco.journey.schedule.FareComponentResponse;
import net.atpco.loader.Loader;
import net.atpco.pricing.version.QueryVersionHelper;

public class SalesDataReader extends CSVFileReader<SalesData>{

	private final JourneyClientHelper journeyClientHelper;
	private final QueryVersionHelper versionHelper;
	private final ItineraryBuddy buddy;

	public SalesDataReader(Loader loader, JourneyClientHelper journeyClientHelper, QueryVersionHelper versionHelper, ItineraryBuddy buddy) {
		super(loader);
		this.journeyClientHelper = journeyClientHelper;
		this.versionHelper = versionHelper;
		this.buddy = buddy;
	}
	
	@Override
	public SalesData process(String[] tokens) {

		String origin= "";
		String destination = "";
		
		Date travelStart = new Date();
		DateRange travelStartDateRange = new DateRange(travelStart, travelStart);
		
		Date ticketingDate = new Date();
		
		long version = versionHelper.getSubsVersionFromTime(ticketingDate.getTime());

		JourneyQuery query = new JourneyQuery(origin, destination, travelStartDateRange, null, "OW", true, buddy, CityAirportFCType.AIRPORT, version);
		
		FareComponentResponse response = journeyClientHelper.getJourneys(query);
		
		return null;
	}
	

}
