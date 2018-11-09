package net.atpco.hack.journeytransformer.client;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.atpco.ash.io.CSVFileReader;
import net.atpco.ash.vo.DateRange;
import net.atpco.hack.journeytransformer.vo.JourneyTransformerRequest;
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

	private DateFormat srcDf = new SimpleDateFormat("yyyy-MM-dd");

	public SalesDataReader(Loader loader, JourneyClientHelper journeyClientHelper, QueryVersionHelper versionHelper, ItineraryBuddy buddy) {
		super(loader);
		this.journeyClientHelper = journeyClientHelper;
		this.versionHelper = versionHelper;
		this.buddy = buddy;
	}

	@Override
	public SalesData process(String[] tokens) {
		try {
			if(tokens[1].trim().isEmpty()) return null;


			SalesData data = new SalesData(tokens[1].split(";"), tokens[2].split(";"),tokens[3].split(";"), tokens[6].split(";"), tokens[7].split(";"), tokens[8], tokens[9].split(";"));

			String[] flightPath= tokens[9].split(";");

			String origin = flightPath[0];
			String destination = flightPath[flightPath.length-1];

			Date stdate = srcDf.parse(data.getFlightDates()[0]);
			DateRange travelStartDateRange = new DateRange(stdate, stdate);
			Date ticketingDate = srcDf.parse(data.getTicketingDate());

			long version = versionHelper.getSubsVersionFromTime(ticketingDate.getTime());

			JourneyQuery query = new JourneyQuery(origin, destination, travelStartDateRange, null, "OW", true, buddy, CityAirportFCType.AIRPORT, version);

			FareComponentResponse response = journeyClientHelper.getJourneys(query);
			
			JourneyTransformerRequest request = new JourneyTransformerRequest(response, data);
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}


}
