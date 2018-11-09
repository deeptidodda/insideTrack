package net.atpco.hack.journeytransformer.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.atpco.journey.schedule.FareComponentResponse;

@AllArgsConstructor
@Setter
@Getter
public class JourneyTransformerRequest {

	private FareComponentResponse response;
	private SalesData salesData;
}
