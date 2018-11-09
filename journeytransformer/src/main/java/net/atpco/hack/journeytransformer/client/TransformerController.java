package net.atpco.hack.journeytransformer.client;

import java.nio.file.Paths;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/transform")
public class TransformerController {

	
	private final SalesDataReader dataReader;
	
	@RequestMapping("/ping")
	public String ping() {
		return "PONG";
	}

	@RequestMapping("/js")
	public void transformSalesData() {

		dataReader.read(Paths.get("/hack/insideTrack/itinerary.csv"));
	}

}
