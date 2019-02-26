package net.atpco.hack.journeytransformer.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import net.atpco.engine.common.cache.QueryCacheConfiguration;
import net.atpco.engine.common.cache.QueryVersionConfiguration;
import net.atpco.engine.common.configuration.LocationEngineConfiguration;
import net.atpco.hack.journeytransformer.client.ItineraryGenerator;
import net.atpco.hack.journeytransformer.client.SalesDataReader;
import net.atpco.journey.client.JourneyClientHelper;
import net.atpco.journey.configuration.JourneyClientCommonConfiguration;
import net.atpco.journey.configuration.JourneyClientConfiguration;
import net.atpco.journey.configuration.JourneyEngineKryoConfiguration;
import net.atpco.journey.request.ItineraryBuddy;
import net.atpco.journey.request.ItineraryBuddy.BuddyField;
import net.atpco.journey.request.ItineraryFilterOption;
import net.atpco.pricing.version.QueryVersionHelper;
import net.atpco.version.common.VersionQuery;

@Configuration
@PropertySource(ignoreResourceNotFound = true, value = { "file:/opt/pricing/engine/properties/journey.properties",
		"file:/opt/pricing/engine/properties/redis.properties",
		"file:/opt/pricing/engine/properties/metrics.properties",
		"file:/opt/pricing/engine/properties/engine-neo4j.properties" })
@Import({JourneyClientConfiguration.class, QueryCacheConfiguration.class, LocationEngineConfiguration.class, QueryVersionConfiguration.class, JourneyEngineKryoConfiguration.class,JourneyClientCommonConfiguration.class})
public class TransformerConfiguration {
	
	@Autowired JourneyClientHelper journeyClientHelper;
	@Autowired QueryVersionHelper versionHelper;
	@Autowired Environment env;
	@Autowired QueryCacheConfiguration loc;
	
	@Bean
	public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
	    ObjectMapper mapper = new ObjectMapper();
	    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	    MappingJackson2HttpMessageConverter converter = 
	        new MappingJackson2HttpMessageConverter(mapper);
	    return converter;
	}
	
	@Bean
	public SalesDataReader salesDataReader(){
		Long version = versionHelper.getLiveVersion();

		if(version == null) {
			version = 1L;
		}
		VersionQuery.set(version);
		loc.cacheManager().tpmMileage();
		loc.cacheManager().mileage();
		return new SalesDataReader(null, journeyClientHelper, versionHelper, buildItineraryBuddy(-1, -1, -1, -1, -1, -1, null));
	}
	
	@Bean
	public ItineraryGenerator itineraryGenerator(){
		return new ItineraryGenerator(journeyClientHelper, versionHelper,  buildItineraryBuddy(-1, -1, -1, -1, -1, -1, null));
	}
	
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurerAdapter() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**");
			}
		};
	}

	private ItineraryBuddy buildItineraryBuddy(int maxConnectTime, int maxOneWayItineraries, int maxRoundTripItineraries,
			int maxTraversalRoutes, int routeTraversalRepeatDepth, int routeTraversalDepth, ItineraryFilterOption[] itineraryFilterOptions) {
		ItineraryBuddy buddy = new ItineraryBuddy()
				.setMaxConnectionTime(getProperty(BuddyField.MAX_CONNECTION_TIME.getPropertyKey(), maxConnectTime, BuddyField.MAX_CONNECTION_TIME.getDefaultValue()))
			    .setMaxOneWayItineraries(getProperty(BuddyField.MAX_ONEWAY_ITINERARIES.getPropertyKey(), maxOneWayItineraries, BuddyField.MAX_ONEWAY_ITINERARIES.getDefaultValue()))
			    .setMaxRoundTripItineraries(getProperty(BuddyField.MAX_RT_ITINERARIES.getPropertyKey(), maxRoundTripItineraries, BuddyField.MAX_RT_ITINERARIES.getDefaultValue()))
				.setMaxTraversalRoutes(getProperty(BuddyField.MAX_TRAVERSAL_ROUTES.getPropertyKey(), maxTraversalRoutes, BuddyField.MAX_TRAVERSAL_ROUTES.getDefaultValue()))
				.setTraversalDepth(getProperty(BuddyField.MAX_TRAVERSAL_DEPTH.getPropertyKey(), routeTraversalRepeatDepth, BuddyField.MAX_TRAVERSAL_DEPTH.getDefaultValue()))
				.setRepeatTraversalDepth(getProperty(BuddyField.REPEAT_TRAVERSAL_DEPTH.getPropertyKey(), routeTraversalDepth, BuddyField.REPEAT_TRAVERSAL_DEPTH.getDefaultValue()))
				.setMirror(env.getProperty(BuddyField.RT_MIRROR.getPropertyKey(), Boolean.class, true))
				.setItineraryFilterOptions(itineraryFilterOptions)
				;

		return buddy;
	}
	
	private String getProperty(String name, int current, String defaultValue) {
		if (current > 0) return ""+current;
		return env.getProperty(name, defaultValue);
	}
	
}
