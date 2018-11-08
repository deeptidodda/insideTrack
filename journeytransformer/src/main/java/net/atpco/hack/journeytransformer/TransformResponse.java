package net.atpco.hack.journeytransformer;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.google.common.collect.Range;

import lombok.SneakyThrows;
import net.atpco.ash.vo.Defaults;
import net.atpco.ash.vo.Flights;
import net.atpco.engine.common.itinerary.Itinerary;
import net.atpco.engine.common.itinerary.ItineraryLeg;
import net.atpco.engine.common.itinerary.JourneyFlights;
import net.atpco.engine.common.pricing.Journey;
import net.atpco.journey.schedule.FareComponentResponse;

public class TransformResponse {

	@SneakyThrows
	public void transform(FareComponentResponse response, String outFileName) throws IOException {
		int count = 0;
		try (PrintStream os = new PrintStream(Files.newOutputStream(Paths.get(outFileName)), true)) {
			os.println("DPTR_TM1,DPTR_TM2,DPTR_TM3,DPTR_TM4,DPTR_TM5,DPTR_TM6,DPTR_TM7,DPTR_TM8," +
		"ARRV_TM1,ARRV_TM2,ARRV_TM3,ARRV_TM4,ARRV_TM5,ARRV_TM6,ARRV_TM7,ARRV_TM8," +
		"FLT_DATE1,FLT_DATE2,FLT_DATE3,FLT_DATE4,FLT_DATE5,FLT_DATE6,FLT_DATE7,FLT_DATE8," +
		"ORAC1,ORAC2,ORAC3,ORAC4,ORAC5,ORAC6,ORAC7,ORAC8," +
		"DSTC1,DSTC2,DSTC3,DSTC4,DSTC5,DSTC6,DSTC7,DSTC8," +
		"NUM_CONNECTIONS,LAST_ARRIVAL,TOTAL_DUR_MIN,TOTAL_CONNECTION_TIME_MIN,MAX_CONNECTION_TIME_MINUTES,DEPARTURE_DOW,ARRIVAL_DOW,INCLUDE");
	
			List<Journey> journeys = response.getJourneys();
			for (Journey journey : journeys) {
				JourneyFlights jf = journey.getJourneyFlights();
				for (Itinerary itinerary : jf.getItineraires()) {
					// include only AF flights
					Range<Integer> range = jf.getRange(itinerary);
					for (int index = range.lowerEndpoint(); index < range.upperEndpoint(); index++) {
						Flights flights = jf.get(index);
						if (flights.containsCarrier("AF") >= 0) {
							// include this one
							exportFlights(itinerary, flights, os, count++);
						}
					}
				}
			}
		}
	}

	private void exportFlights(Itinerary itinerary, Flights flights, PrintStream os, int count) {
		StringBuilder sb = new StringBuilder();
		
		// departure times (8 fields)
		for (int index = 0; index < 8; index++) {
			sb.append(getDepartureTime(itinerary, flights, index) + ",");
		}
		
		// arrival times (8 fields)
		for (int index = 0; index < 8; index++) {
			sb.append(getArrivalTime(itinerary, flights, index) + ",");
		}
		
		// departure dates (8 fields)
		for (int index = 0; index < 8; index++) {
			sb.append(getDepartureDate(itinerary, flights, index) + ",");
		}
		
		// origin airport (8 fields)
		for (int index = 0; index < 8; index++) {
			sb.append(getDepartureAirport(itinerary, flights, index) + ",");
		}
		
		// destination airport (8 fields)
		for (int index = 0; index < 8; index++) {
			sb.append(getArrivalAirport(itinerary, flights, index) + ",");
		}
		
		sb.append(itinerary.getNoOfLegs()-1 + ",");
		sb.append(itinerary.getLastLeg().getFlightDetails().getArrivalMinutesOfDay() + ",");
		sb.append(getDuration(itinerary) + ",");
		sb.append(getTotalConnectionTime(itinerary) + ",");
		sb.append(getMaxConnectionTime(itinerary) + ",");
		sb.append(getDepartureDayOfWeek(itinerary) + ",");
		sb.append(getArrivalDayOfWeek(itinerary) + ",");
		sb.append(count%1000 == 0? "TRUE" : "FALSE");
		
		os.println(sb.toString());
	}
	
	private String getDepartureDayOfWeek(Itinerary itinerary) {
		DayOfWeek dayOfWeek = itinerary.getFirstLeg().getDepartureRange().toLocalDateRange().getStartLocalDate().getDayOfWeek();
		return String.valueOf(dayOfWeek.getValue());
	}

	private String getArrivalDayOfWeek(Itinerary itinerary) {
		DayOfWeek dayOfWeek = itinerary.getLastLeg().getArrivesDate().toInstant().atZone(Defaults.ZONE_ID).toLocalDate().getDayOfWeek();
		return String.valueOf(dayOfWeek.getValue());
	}

	private String getTotalConnectionTime(Itinerary itinerary) {
		long connectionTimeMinutes = 0;
		for (int index = 1; index < itinerary.getNoOfLegs(); index++) {
			ItineraryLeg prevLeg = itinerary.getItineraryLeg(index-1);
			LocalDateTime previousArrival = LocalDateTime.of(prevLeg.getArrivesDate().toInstant().atZone(Defaults.ZONE_ID).toLocalDate(), prevLeg.getFlightDetails().getArrivalTime());
			ItineraryLeg currLeg = itinerary.getItineraryLeg(index);
			LocalDateTime departure = LocalDateTime.of(currLeg.getDepartureRange().toLocalDateRange().getStartLocalDate(), currLeg.getFlightDetails().getDepartureTime());
			connectionTimeMinutes += ChronoUnit.MINUTES.between(previousArrival,  departure);
		}
		
		return String.valueOf(connectionTimeMinutes);
	}

	private String getMaxConnectionTime(Itinerary itinerary) {
		long maxConnectionTimeMinutes = 0;
		for (int index = 1; index < itinerary.getNoOfLegs(); index++) {
			ItineraryLeg prevLeg = itinerary.getItineraryLeg(index-1);
			LocalDateTime previousArrival = LocalDateTime.of(prevLeg.getArrivesDate().toInstant().atZone(Defaults.ZONE_ID).toLocalDate(), prevLeg.getFlightDetails().getArrivalTime());
			ItineraryLeg currLeg = itinerary.getItineraryLeg(index);
			LocalDateTime departure = LocalDateTime.of(currLeg.getDepartureRange().toLocalDateRange().getStartLocalDate(), currLeg.getFlightDetails().getDepartureTime());
			long connectionTimeMinutes = ChronoUnit.MINUTES.between(previousArrival,  departure);
			if (connectionTimeMinutes > maxConnectionTimeMinutes) {
				maxConnectionTimeMinutes = connectionTimeMinutes;
			}
		}
		
		return String.valueOf(maxConnectionTimeMinutes);
	}

	private String getDuration(Itinerary itinerary) {
		LocalDateTime depart = LocalDateTime.of(itinerary.getFirstLeg().getDepartureRange().toLocalDateRange().getStartLocalDate(), itinerary.getFirstLeg().getFlightDetails().getDepartureTime());
		LocalDateTime arrival = LocalDateTime.of(itinerary.getLastLeg().getArrivesDate().toInstant().atZone(Defaults.ZONE_ID).toLocalDate(), itinerary.getLastLeg().getFlightDetails().getArrivalTime());
		return String.valueOf(ChronoUnit.MINUTES.between(depart,  arrival));
	}

	private String getDepartureAirport(Itinerary itinerary, Flights flights, int index) {
		if (index < itinerary.getNoOfLegs()) {
			return itinerary.getItineraryLeg(index).getSegment().getOriginAirport();
		}
		return "";
	}

	private String getArrivalAirport(Itinerary itinerary, Flights flights, int index) {
		if (index < itinerary.getNoOfLegs()) {
			return itinerary.getItineraryLeg(index).getSegment().getDestinationAirport();
		}
		return "";
	}

	private String getDepartureTime(Itinerary itinerary, Flights flights, int index) {
		if (index < itinerary.getNoOfLegs()) {
			return String.valueOf(itinerary.getItineraryLeg(index).getFlightDetails().getDepartureMinutesOfDay());
		}
		return "";
	}
	
	private String getDepartureDate(Itinerary itinerary, Flights flights, int index) {
		if (index < itinerary.getNoOfLegs()) {
			return String.valueOf(itinerary.getItineraryLeg(index).getFlightDetails().getDateRange().getStartLocalDate());
		}
		return "";
	}
	
	private String getArrivalTime(Itinerary itinerary, Flights flights, int index) {
		if (index < itinerary.getNoOfLegs()) {
			return String.valueOf(itinerary.getItineraryLeg(index).getFlightDetails().getArrivalMinutesOfDay());
		}
		return "";
	}
	
}
