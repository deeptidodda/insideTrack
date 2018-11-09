package net.atpco.hack.journeytransformer.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.atpco.loader.Workable;

@AllArgsConstructor
@Setter
@Getter
public class SalesData implements Workable{

	private String[] flightPath;
	private String[] flightDates;
	private String[] arrivalTimes;
	private String[] departureTimes;
	private String[] marketingCarriers;
	private String[] marketingFlightNumbers;
	
}
