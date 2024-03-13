package com.poolstats.billiardsscraper.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

//@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests().anyRequest().permitAll() // Dozvoljava pristup svim endpointima bez autentifikacije
				.and().csrf().disable() // Isključuje CSRF zaštitu
				.headers().frameOptions().disable(); // Isključuje X-Frame-Options zaštita ako koristite ugrađeni Tomcat ili slično
	}
}
