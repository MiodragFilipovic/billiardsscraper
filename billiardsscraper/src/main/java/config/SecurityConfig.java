package config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		System.out.println("Configuring");
		http.authorizeRequests().antMatchers("/api/players/**").permitAll() // Dozvolite pristup bez autentifikacije za sve endpointe pod /api/players
				.anyRequest().authenticated(); // Ostali zahtevi zahtevaju autentifikaciju
	}
}
