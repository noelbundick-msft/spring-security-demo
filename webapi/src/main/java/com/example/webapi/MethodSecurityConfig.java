package com.example.webapi;

import java.util.List;

import com.example.security.CompositePermissionEvaluator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@Configuration
@ComponentScan("com.example.security")
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig {

  @Bean
  static MethodSecurityExpressionHandler methodSecurityExpressionHandler(List<PermissionEvaluator> evaluators) {
    DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
    CompositePermissionEvaluator eval = new CompositePermissionEvaluator(evaluators);
    handler.setPermissionEvaluator(eval);
    return handler;
  }
}
