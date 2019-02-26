package net.atpco.hack.journeytransformer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.integration.IntegrationAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication(exclude={MongoAutoConfiguration.class, MongoDataAutoConfiguration.class, EmbeddedMongoAutoConfiguration.class,
		JmxAutoConfiguration.class, SpringApplicationAdminJmxAutoConfiguration.class, 
		IntegrationAutoConfiguration.class})
@PropertySource(ignoreResourceNotFound = true, value = { "file:/opt/pricing/engine/properties/journey_inside_track.properties",
		"file:/opt/pricing/engine/properties/redis.properties",
		"file:/opt/pricing/engine/properties/metrics.properties",
		"file:/opt/pricing/engine/properties/engine-neo4j.properties" })
public class TransformerApp {

	public static void main(String[] args) {
		SpringApplication.run(TransformerApp.class, args);
	}
}
