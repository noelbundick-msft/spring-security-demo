package com.example.webapi;

import com.example.security.CompositePermissionEvaluator;
import com.example.security.AuthZServicePermissionEvaluator;
import com.example.security.SystemRolePermissionEvaluator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@Configuration
@ComponentScan("com.example.security")
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig {

  @Bean
  static MethodSecurityExpressionHandler methodSecurityExpressionHandler(
      SystemRolePermissionEvaluator systemRolePermissionEvaluator,
      AuthZServicePermissionEvaluator customPermissionEvaluator) {
    DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();

    CompositePermissionEvaluator eval = new CompositePermissionEvaluator(
        systemRolePermissionEvaluator,
        customPermissionEvaluator);
    handler.setPermissionEvaluator(eval);

    return handler;
  }
}
