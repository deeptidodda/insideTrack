package net.atpco.hack.journeytransformer.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.atpco.loader.Workable;

@AllArgsConstructor
@Setter
@Getter
public class SalesData implements Workable{

	private String[] departureTimes;
	private String[] arrivalTimes;
	private String[] flightDates;
	private String[] marketingCarriers;
	private String[] marketingFlightNumbers;
	private String ticketingDate;
	private String[] flightPath;

}
