package net.atpco.hack.journeytransformer;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.Range;

import lombok.SneakyThrows;
import net.atpco.ash.vo.Defaults;
import net.atpco.ash.vo.Flight;
import net.atpco.ash.vo.Flights;
import net.atpco.engine.common.itinerary.Itinerary;
import net.atpco.engine.common.itinerary.ItineraryLeg;
import net.atpco.engine.common.itinerary.JourneyFlights;
import net.atpco.engine.common.pricing.Journey;
import net.atpco.journey.schedule.FareComponentResponse;

public class TransformResponse {

	public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

	enum Alliance {
		SKYTEAM,
		ONEWORLD
	};
	
	public static String[] SKYTEAM_ALLIANCE_CARRIERS = {
			"SU", "AR", "AM", "UX", "AF", 
			"AZ", "CI", "MU", "CZ", "OK",
			"DL", "GA", "KQ", "KL", "KE", 
			"ME", "SV", "RO" ,"VN", "MF"
	};

	public static String[] ONEWORLD_ALLIANCE_CARRIERS = {
			"AA", "BA", "CX",
			"AY", "IB", "JL",
			"JJ", "LA", "MH", "QF",
			"QR", "RJ", "S7",
			"UL"
	};

	@SneakyThrows
	public void transform(FareComponentResponse response, String outFileName, 
			BiPredicate<Itinerary, Flights> exportFilter,
			BiPredicate<Itinerary, Flights> markInclude) throws IOException {
		
		List<Journey> journeys = response.getJourneys();
		long minDuration =  getMinTotalDuration(journeys, exportFilter);
		int cnt = 0 ;
		for (Journey journey : journeys) {
		
		
		try (PrintStream os = new PrintStream(Files.newOutputStream(Paths.get(outFileName + cnt++), StandardOpenOption.CREATE, StandardOpenOption.APPEND), true)) {
			
			os.println("DPTR_TM1,DPTR_TM2,DPTR_TM3,DPTR_TM4,DPTR_TM5,DPTR_TM6,DPTR_TM7,DPTR_TM8," +
					"ARRV_TM1,ARRV_TM2,ARRV_TM3,ARRV_TM4,ARRV_TM5,ARRV_TM6,ARRV_TM7,ARRV_TM8," +
					"FLT_DATE1,FLT_DATE2,FLT_DATE3,FLT_DATE4,FLT_DATE5,FLT_DATE6,FLT_DATE7,FLT_DATE8," +
					"ARRV_DAY1,ARRV_DAY2,ARRV_DAY3,ARRV_DAY4,ARRV_DAY5,ARRV_DAY6,ARRV_DAY7,ARRV_DAY8," +
					"ORAC1,ORAC2,ORAC3,ORAC4,ORAC5,ORAC6,ORAC7,ORAC8," +
					"DSTC1,DSTC2,DSTC3,DSTC4,DSTC5,DSTC6,DSTC7,DSTC8," +
					"MCXR1,MCXR2,MCXR3,MCXR4,MCXR5,MCXR6,MCXR7,MCXR8," +
					"NUM_CONNECTIONS,LAST_ARRIVAL,TOTAL_DUR_MIN,TOTAL_CONNECTION_TIME_MIN," +
					"MAX_CONNECTION_TIME_MINUTES,DEPARTURE_DOW,ARRIVAL_DOW,FLIGHT_CHANGE,RELATIVE_DURATION,INCLUDE");

			
				JourneyFlights jf = journey.getJourneyFlights();
				for (Itinerary itinerary : jf.getItineraires()) {
					Range<Integer> range = jf.getRange(itinerary);
					for (int index = range.lowerEndpoint(); index < range.upperEndpoint(); index++) {
						Flights flights = jf.get(index);
						if (exportFilter.test(itinerary, flights)) {
							// include this one
							exportFlights(itinerary, flights, os, markInclude, minDuration);
						}
					}
				}
			}
		}
	}
	
	private long getMinTotalDuration(List<Journey> journeys, BiPredicate<Itinerary, Flights> exportFilter) {
		long minDuration = Long.MAX_VALUE;
		for (Journey journey : journeys) {
			JourneyFlights jf = journey.getJourneyFlights();
			for (Itinerary itinerary : jf.getItineraires()) {
				Range<Integer> range = jf.getRange(itinerary);
				for (int index = range.lowerEndpoint(); index < range.upperEndpoint(); index++) {
					Flights flights = jf.get(index);
					if (exportFilter.test(itinerary, flights)) {
						long duration = getDurationMinutes(itinerary);
						if (duration < minDuration) {
							minDuration = duration;
						}
					}
				}
			}
		}
		return minDuration;
	}

	private void exportFlights(Itinerary itinerary, Flights flights, PrintStream os, BiPredicate<Itinerary, Flights> markInclude, long minDuration) {
		StringBuilder sb = new StringBuilder();

		// departure times (8 fields)
		for (int index = 0; index < 8; index++) {
			sb.append(getDepartureTime(itinerary, index) + ",");
		}

		// arrival times (8 fields)
		for (int index = 0; index < 8; index++) {
			sb.append(getArrivalTime(itinerary, index) + ",");
		}

		// departure dates (8 fields)
		for (int index = 0; index < 8; index++) {
			sb.append(getDepartureDate(itinerary, index) + ",");
		}

		// arrival days (8 fields)
		for (int index = 0; index < 8; index++) {
			sb.append(getArrivalDay(itinerary, index) + ",");
		}

		// origin airport (8 fields)
		for (int index = 0; index < 8; index++) {
			sb.append(getDepartureAirport(itinerary, index) + ",");
		}

		// destination airport (8 fields)
		for (int index = 0; index < 8; index++) {
			sb.append(getArrivalAirport(itinerary, index) + ",");
		}

		// marketing carriers (8 fields)
		for (int index = 0; index < 8; index++) {
			sb.append(getMarketingCarrier(flights, index) + ",");
		}

		sb.append(itinerary.getNoOfLegs()-1 + ",");
		sb.append(itinerary.getLastLeg().getFlightDetails().getArrivalTime().format(TIME_FORMATTER) + ",");
		final long durationMinutes = getDurationMinutes(itinerary);
		sb.append(durationMinutes + ",");
		sb.append(getTotalConnectionTimeMinutes(itinerary) + ",");
		sb.append(getMaxConnectionTimeMinutes(itinerary) + ",");
		sb.append(getDepartureDayOfWeek(itinerary) + ",");
		sb.append(getArrivalDayOfWeek(itinerary) + ",");
		sb.append(getFlightChangeType(flights) + ",");

		sb.append((int)((double)durationMinutes/(double)minDuration * 100d) + ",");
		
		sb.append(markInclude.test(itinerary, flights)? "TRUE" : "FALSE");

		os.println(sb.toString());
	}

	private String getFlightChangeType(Flights flights) {
		if (flights.isSameCarrier(flights.get(0).getCarrier())) {
			return "ONLINE";
		}

		Alliance alliance = null;
		for (Flight flight : flights) {
			String mktCarrier = flight.getCarrier();
			if (ArrayUtils.contains(SKYTEAM_ALLIANCE_CARRIERS, mktCarrier)) {
				if (alliance != null && alliance != Alliance.SKYTEAM) {
					return "INTERLINE";
				}
				alliance = Alliance.SKYTEAM;
			} else if (ArrayUtils.contains(ONEWORLD_ALLIANCE_CARRIERS, mktCarrier)) {
				if (alliance != null && alliance != Alliance.ONEWORLD) {
					return "INTERLINE";
				}
				alliance = Alliance.ONEWORLD;
			} else {
				return "INTERLINE";
			}
		}

		return "ALLIANCE";
	}

	private String getDepartureDayOfWeek(Itinerary itinerary) {
		DayOfWeek dayOfWeek = itinerary.getFirstLeg().getDepartureRange().toLocalDateRange().getStartLocalDate().getDayOfWeek();
		return String.valueOf(dayOfWeek.getValue());
	}

	private String getArrivalDayOfWeek(Itinerary itinerary) {
		DayOfWeek dayOfWeek = itinerary.getLastLeg().getArrivesDate().toInstant().atZone(Defaults.ZONE_ID).toLocalDate().getDayOfWeek();
		return String.valueOf(dayOfWeek.getValue());
	}

	private String getTotalConnectionTimeMinutes(Itinerary itinerary) {
		long connectionTimeMinutes = 0;
		for (int index = 0; index < itinerary.getNoOfLegs()-1; index++) {
			long connectionTime = calcConnectTimeMinutes(itinerary, index);
			connectionTimeMinutes += connectionTime;
		}

		return String.valueOf(connectionTimeMinutes);
	}

	private String getMaxConnectionTimeMinutes(Itinerary itinerary) {
		long maxConnectionTimeMinutes = 0;
		for (int index = 0; index < itinerary.getNoOfLegs()-1; index++) {
			long connectionTimeMinutes = calcConnectTimeMinutes(itinerary, index);
			if (connectionTimeMinutes > maxConnectionTimeMinutes) {
				maxConnectionTimeMinutes = connectionTimeMinutes;
			}
		}

		return String.valueOf(maxConnectionTimeMinutes);
	}

	private long getDurationMinutes(Itinerary itinerary) {
		
		long totalMinutes = itinerary.getItineraryLeg(0).getFlightDetails().getElapsedLocalTime().toSecondOfDay()/60;
		for (int index = 1; index < itinerary.getNoOfLegs(); index++) {
			totalMinutes += calcConnectTimeMinutes(itinerary, index-1);
			totalMinutes += itinerary.getItineraryLeg(index).getFlightDetails().getElapsedLocalTime().toSecondOfDay()/60;
		}

		return totalMinutes;
	}

	private long calcConnectTimeMinutes(Itinerary itinerary, int connectionIndex) {
		ItineraryLeg prevLeg = itinerary.getItineraryLeg(connectionIndex);
		LocalDateTime previousArrival = LocalDateTime.of(prevLeg.getArrivesDate().toInstant().atZone(Defaults.ZONE_ID).toLocalDate(), prevLeg.getFlightDetails().getArrivalTime());
		ItineraryLeg currLeg = itinerary.getItineraryLeg(connectionIndex+1);
		LocalDateTime departure = LocalDateTime.of(currLeg.getDepartureRange().toLocalDateRange().getStartLocalDate(), currLeg.getFlightDetails().getDepartureTime());
		long connectionTime = ChronoUnit.MINUTES.between(previousArrival,  departure);
		return connectionTime;
	}

	private String getDepartureAirport(Itinerary itinerary, int index) {
		if (index < itinerary.getNoOfLegs()) {
			return itinerary.getItineraryLeg(index).getSegment().getOriginAirport();
		}
		return "";
	}

	private String getArrivalAirport(Itinerary itinerary, int index) {
		if (index < itinerary.getNoOfLegs()) {
			return itinerary.getItineraryLeg(index).getSegment().getDestinationAirport();
		}
		return "";
	}

	private String getMarketingCarrier(Flights flights, int index) {
		if (index < flights.size()) {
			return flights.get(index).getCarrier();
		}
		return "";
	}

	private String getDepartureTime(Itinerary itinerary, int index) {
		if (index < itinerary.getNoOfLegs()) {
			return itinerary.getItineraryLeg(index).getFlightDetails().getDepartureTime().format(TIME_FORMATTER);
		}
		return "";
	}

	private String getDepartureDate(Itinerary itinerary, int index) {
		if (index < itinerary.getNoOfLegs()) {
			if(itinerary.getItineraryLeg(index).getDepartureRange() != null) {
				return String.valueOf(itinerary.getItineraryLeg(index).getDepartureRange().getStartLocalDate());
			}
		}
		return "";
	}

	private String getArrivalTime(Itinerary itinerary, int index) {
		if (index < itinerary.getNoOfLegs()) {
			return itinerary.getItineraryLeg(index).getFlightDetails().getArrivalTime().format(TIME_FORMATTER);
		}
		return "";
	}

	private String getArrivalDay(Itinerary itinerary, int index) {
		if (index < itinerary.getNoOfLegs()) {
			return String.valueOf(itinerary.getItineraryLeg(index).getFlightDetails().getArrivalDay().getAmount());
		}
		return "";
	}

}
