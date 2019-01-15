package net.atpco.hack.journeytransformer.client;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;

import lombok.Data;

@Data
public class ItineraryGeneratorRequest {
	private String origin;
	private String destination;
	private String[] carriers;
	
	@JsonDeserialize(using=LocalDateDeserializer.class)
	private LocalDate beginningDate;
	
	private ChronoUnit units;
	private int incrementUnit;
	private int numIterations;
}

