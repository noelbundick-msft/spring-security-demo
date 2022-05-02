package com.example.security;

import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SystemSecurityContext implements AutoCloseable {

  public SystemSecurityContext() {
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken("system", null,
            Collections.singletonList(SystemRolePermissionEvaluator.SYSTEM_ROLE)));
  }

  @Override
  public void close() {
    SecurityContextHolder.clearContext();
  }

  public Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }
}
