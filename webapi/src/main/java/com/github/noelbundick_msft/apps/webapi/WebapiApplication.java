package com.github.noelbundick_msft.apps.webapi;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import com.github.noelbundick_msft.security.CustomPermissionEvaluator;

@ComponentScan("com.github.noelbundick_msft.security")
@EnableGlobalMethodSecurity(prePostEnabled = true)
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
								AuthorityUtils.createAuthorityList("ROLE_ADMIN")));

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
	static MethodSecurityExpressionHandler methodSecurityExpressionHandler(CustomPermissionEvaluator evaluator) {
		DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
		handler.setPermissionEvaluator(evaluator);
		return handler;
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
