package com.example.webapi;

import java.util.Collections;

import com.example.security.CustomPermissionEvaluator;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@SpringBootApplication
public class WebapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebapiApplication.class, args);
	}

	@Bean
	CommandLineRunner initThings(ThingRepository repository) {
		return args -> {
			try {
				SecurityContextHolder.getContext().setAuthentication(
						new UsernamePasswordAuthenticationToken("system", null,
								Collections.singletonList(CustomPermissionEvaluator.SYSTEM_ROLE)));

				for (int i = 0; i < 10; i++) {
					Thing thing = new Thing();
					thing.setName("Thing " + i);
					repository.save(thing);
				}
			} finally {
				SecurityContextHolder.clearContext();
			}
		};
	}

	@Bean
	UserDetailsService userDetailsService() {
		UserDetails user = User.withDefaultPasswordEncoder()
				.username("user")
				.password("password")
				.roles("USER")
				.build();

		UserDetails admin = User.withDefaultPasswordEncoder()
				.username("admin")
				.password("password")
				.roles("ADMIN", "USER")
				.build();

		return new InMemoryUserDetailsManager(user, admin);
	}
}