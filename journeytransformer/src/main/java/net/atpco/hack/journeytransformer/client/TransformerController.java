package net.atpco.hack.journeytransformer.client;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/batch")
public class TransformerController {

	@RequestMapping("/ping")
	public String ping() {
		return "PONG";
	}
	
	

}
