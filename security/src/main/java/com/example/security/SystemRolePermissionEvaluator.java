package com.example.security;

import java.io.Serializable;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class SystemRolePermissionEvaluator implements PermissionEvaluator {
  public static GrantedAuthority SYSTEM_ROLE = new SimpleGrantedAuthority("ROLE_SYSTEM");

  @Override
  public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
    return hasSystemRole(authentication);
  }

  @Override
  public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
      Object permission) {
    return hasSystemRole(authentication);
  }

  public boolean hasSystemRole(Authentication authentication) {
    return authentication != null && authentication.getAuthorities().contains(SYSTEM_ROLE);
  }

}
