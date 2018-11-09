package net.atpco.hack.journeytransformer.client;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.SneakyThrows;
import net.atpco.ash.io.CSVFileReader;
import net.atpco.ash.vo.DateRange;
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

public class SalesDataReader extends CSVFileReader<SalesData>{
	
	public static final String OUTPUT_DIR = "/dev/hack2018/output";

	private final JourneyClientHelper journeyClientHelper;
	private final QueryVersionHelper versionHelper;
	private final ItineraryBuddy buddy;
	
	private final TransformResponse transformResponse = new TransformResponse();
	
	private int lineNumber = 0;

	private DateFormat srcDf = new SimpleDateFormat("yyyy-MM-dd");
	
	private DateFormat tkDf = new SimpleDateFormat("ddMMMyy");

	public SalesDataReader(Loader loader, JourneyClientHelper journeyClientHelper, QueryVersionHelper versionHelper, ItineraryBuddy buddy) {
		super(loader, true);
		this.journeyClientHelper = journeyClientHelper;
		this.versionHelper = versionHelper;
		this.buddy = buddy;
	}

	@SneakyThrows
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
			Date ticketingDate = tkDf.parse(data.getTicketingDate());

			Long version = versionHelper.getSubsVersionFromTime(ticketingDate.getTime());

			if(version == null) {
				version = 1L;
			}
			VersionQuery.set(version);
			JourneyQuery query = new JourneyQuery(origin, destination, travelStartDateRange, null, "OW", true, buddy, CityAirportFCType.AIRPORT, version);

			FareComponentResponse response = journeyClientHelper.getJourneys(query);
			
			transformResponse.transform(data, response, OUTPUT_DIR + "/" + origin + "-" + destination + "-" + lineNumber++ +  ".csv");
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

}
