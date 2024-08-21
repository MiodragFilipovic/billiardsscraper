package com.poolstats.billiardsscraper.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestConfig {

	@Bean
	public RestTemplate restTemplate() {
		// Kreiranje RestTemplate objekta
		RestTemplate restTemplate = new RestTemplate();

		// Postavljanje timeout vrijednosti za uspostavljanje veze i čitanje odgovora
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(50000); // 5 sekundi timeout za uspostavljanje veze
		requestFactory.setReadTimeout(100000); // 10 sekundi timeout za čitanje odgovora
		restTemplate.setRequestFactory(requestFactory);

		return restTemplate;
	}
}
